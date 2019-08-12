package Graphs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source, Zip}
import akka.stream.{ActorMaterializer, ClosedShape}

object GraphBasics extends App {

  implicit val system = ActorSystem("GraphBasics")
  implicit val materializer = ActorMaterializer()

  val input = Source(1 to 1000)
  val incrementer = Flow[Int].map(x => x + 1)
  val multiplier = Flow[Int].map(x => x * 10)

  val output = Sink.foreach[(Int,Int)](println)

  val graph = RunnableGraph.fromGraph(
    GraphDSL.create(){
      implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        // #1 define fan out and fan int
        val broadcast = builder.add(Broadcast[Int](2))
        val zip = builder.add(Zip[Int,Int])

        // #Source linked with broadcast
        input ~> broadcast
        // # Link broadcast into the flow and the zip (fin int)
        broadcast.out(0) ~> incrementer ~> zip.in0
        broadcast.out(1) ~> multiplier ~> zip.in1

        // # link fan in into the sink
        zip.out ~> output

        ClosedShape
    }
  )

  //graph.run()

  val graph2 = RunnableGraph.fromGraph(
    GraphDSL.create(){
      implicit builder:GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        val broadcast = builder.add(Broadcast[Int](2)) //fan-out
       // val zip = builder.add(Zip[Int, Int]) // fan-in

        val sink1 = Sink.foreach[Int]( x => println(s" First Sink $x"))
        val sink2 = Sink.foreach[Int](x => println(s"Second Sink $x"))

        input ~> broadcast
        broadcast.out(0) ~> incrementer ~> sink1
        broadcast.out(1) ~> multiplier ~> sink2
        //zip.out ~> output

        ClosedShape
    }
  )
  //graph2.run()

  val graph3 = RunnableGraph.fromGraph(
    GraphDSL.create(){
      implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        import scala.concurrent.duration._
        import scala.language.postfixOps

        val fastSource = Source(1 to 1000).throttle(10, 1 second)
        val slowSource = Source (1 to 1000).throttle(2, 1 second)

        val merge = builder.add(Merge[Int](2))
        val balance = builder.add(Balance[Int](2)) // balance and distribute the rate of prodction of the produces and distribute to sinks

        val sink1 = Sink.foreach[Int]( x => println(s"First Sink $x"))
        val sink2 = Sink.foreach[Int](x => println(s"Second Sink $x"))

        fastSource ~> merge ~> balance ~> sink1
        slowSource ~> merge;   balance ~> sink2

        ClosedShape
    }
  )
  graph3.run()

}
