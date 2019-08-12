package Graphs

import Graphs.GraphBasics.input
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, MergePreferred, RunnableGraph, Sink, Source, Zip, ZipWith}
import akka.stream.{ActorMaterializer, ClosedShape, UniformFanInShape}

object GraphCycles extends App{

  implicit val system = ActorSystem("GraphCycles")
  implicit val materializer = ActorMaterializer()

  val graphCycle = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val firstSource = Source.single(1)
    val secondSource = Source.single(1)

    val firstSourceShape = builder.add(firstSource)
    val secondSourceShape = builder.add(secondSource)

    val zipWithShape = builder.add(Zip[Int, Int])
    val mergePreferedShape = builder.add(MergePreferred[(Int, Int)](1))

    val fibonnaciShape = builder.add(Flow[(Int, Int)].map{x => println(x._1) ; (x._1 + x._2 , x._1 )})

    //val fibonnaciShape1 = builder.add(Flow[(Int, Int)].map{x => println(x._1)})

    firstSourceShape ~> zipWithShape.in0
    secondSourceShape ~> zipWithShape.in1
    zipWithShape.out ~> mergePreferedShape ~> fibonnaciShape
    mergePreferedShape.preferred <~ fibonnaciShape

    ClosedShape
  }
  RunnableGraph.fromGraph(graphCycle).run()

}
