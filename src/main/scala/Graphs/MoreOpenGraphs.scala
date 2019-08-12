package Graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, UniformFanInShape, scaladsl}
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source, ZipWith}

object MoreOpenGraphs extends App{

  implicit val system = ActorSystem("MoreOpenGraphs")
  implicit val materializer = ActorMaterializer()


  val max3StaticGraph = GraphDSL.create()
  {
    implicit builder =>
      import GraphDSL.Implicits._

      val max1 = builder.add(ZipWith[Int,Int,Int]((a,b) => Math.max(a,b)))
      val max2 = builder.add(ZipWith[Int,Int,Int]((a,b) => Math.max(a,b)))

      max1.out ~> max2.in0

      UniformFanInShape(max2.out, max1.in0, max1.in1,max2.in1)
  }

  val source1 = Source(1 to 10)
  val source2 = Source(1 to 10).map(x => 5)
  val source3 = Source((1 to 10).reverse)

  val maxSink = Sink.foreach[Int](x => println(s"Max is: $x"))

  val max3RunnableGraph = RunnableGraph.fromGraph(
    GraphDSL.create(){ implicit builder =>
      import GraphDSL.Implicits._
      val max3Shape = builder.add(max3StaticGraph)

      source1 ~>max3Shape.in(0)
      source2 ~>max3Shape.in(1)
      source3 ~>max3Shape.in(2)
      max3Shape.out ~> maxSink

      ClosedShape
    }
  )

  max3RunnableGraph.run()
}

