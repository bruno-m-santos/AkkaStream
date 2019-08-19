package Basics

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}

object BackPressureBasics extends App{

  implicit val system = ActorSystem("BackPressure")
  implicit val materializer = ActorMaterializer()

  val fastSource = Source(1 to 1000)
  val slowSink  = Sink.foreach[Int] { x=>
    Thread.sleep(1000)
    println(s"Sink $x")
  }

  // no baskpressure
  //fastSource.to(slowSink).run()

  //with pressure
  //fastSource.async.to(slowSink).run()

  val simpleFlow = Flow[Int].map{ x=>
    println(s"Incoming: $x")
    x + 1
  }

  // internal buffer is 16 positions
  fastSource.async.via(simpleFlow).async.to(slowSink)//.run()

  /**
   *  - reactions to backpressure(in order):
   *  - try to slow down if possible
   *  - buffer the elements until there's more demond
   *  - drop down elements from buffer if it overflows
   *  - tear down /kill the whole stream
   */

  // override overflow strategy
  val bufferedFlow = simpleFlow.buffer(10,overflowStrategy = OverflowStrategy.backpressure)
  fastSource.async.via(bufferedFlow).async.to(slowSink).run()

  //throttling
  import scala.concurrent.duration._
  import scala.language.postfixOps
  fastSource.throttle(2, 2 second).to(Sink.foreach(println))

}
