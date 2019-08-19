package Basics

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object AkkaFusion extends App{

  implicit val system = ActorSystem("AkkaFusionComponents")
  implicit val materializer = ActorMaterializer()

  // akka stream runs over akka's actor
  val source = Source(1 to 3)
  val flow1 = Flow[Int].map(x => x + 1)
  val flow2 = Flow[Int].map(x => x* 2)
  val sink = Sink.foreach(println)
  source.via(flow1).async.via(flow2).async.to(sink).run() //executes on different actors
  source.via(flow1).via(flow2).to(sink).run() // executes on the same actor

  class SimpleActor extends Actor{
    override def receive: Receive = {
      case x:Int =>
        val x1 = x +1
        val x2 = x1 * 2
        println(x2)
      case message => println(message)
    }
  }

  val actor = system.actorOf(Props[SimpleActor], "simpleActor")
  (1 to 3).foreach(actor ! _)

}
