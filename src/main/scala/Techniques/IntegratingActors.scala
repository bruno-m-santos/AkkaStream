package Techniques

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout

object IntegratingActors extends App {

  implicit val system = ActorSystem("IntegratingStreamActor")
  implicit val materializer = ActorMaterializer()

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case s: String => sender() ! s"$s$s"
      case n:Int => sender() ! n+1
      case _ =>
    }
  }

  val simpleActor = system.actorOf(Props[SimpleActor],"simpleActor")
  val numbersSource = Source(1 to 10)

  import scala.concurrent.duration._
  import scala.language.postfixOps

  implicit val timeout = Timeout(2 seconds)
  val actorBasedFlow = Flow[Int].ask[Int](parallelism = 4)(simpleActor)

  numbersSource.via(actorBasedFlow).to(Sink.foreach(println)).run()
}
