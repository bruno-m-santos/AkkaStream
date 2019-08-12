package Graphs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL, Sink, Source}
import akka.stream.{ActorMaterializer, FlowShape, SinkShape, SourceShape}

object OpenGraphs extends App{

  implicit val system = ActorSystem("OpenGraph")
  implicit val materializer = ActorMaterializer()

  val firstSource = Source(1 to 10)
  val secondSource = Source(42 to 1000)

  val sourceGraph = Source.fromGraph(
    GraphDSL.create() {
      implicit builder: GraphDSL.Builder[NotUsed] =>

        import GraphDSL.Implicits._
        val concat = builder.add(Concat[Int](2))

        firstSource ~> concat
        secondSource ~> concat

        SourceShape(concat.out)
    }
  )
  //graph.to(Sink.foreach(println)).run()

//------------------------------------------------------------------------------
  val sink1 = Sink.foreach[Int](x => println(s"Sink One $x"))
  val sink2 = Sink.foreach[Int](x => println(s"Sink Two $x"))

  val sinkGraph = Sink.fromGraph(
    GraphDSL.create() {
      implicit builder =>
        import GraphDSL.Implicits._

       val broadcast = builder.add(Broadcast[Int](2))

       broadcast ~> sink1
       broadcast ~> sink2

       SinkShape(broadcast.in)
    }
  )
  firstSource.to(sinkGraph).run()

//------------------------------------------------------------------------------
  val incrementer = Flow[Int].map(_ + 1)
  val multiplier = Flow[Int].map(_ * 10)

   val flowGraph = Flow.fromGraph(
     GraphDSL.create(){
       implicit builder =>
         import GraphDSL.Implicits._

         val incrementShape = builder.add(incrementer)
         val multiplierShape = builder.add(multiplier)

         incrementShape ~> multiplierShape

         FlowShape(incrementShape.in, multiplierShape.out)
     }
   )
  //firstSource.via(flowGraph).to(Sink.foreach(println)).run()

 //------------------------------------------------------------------------
// flow from a sink and a source ? is forbideen
}
