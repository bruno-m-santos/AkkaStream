package Techniques

import akka.actor.ActorSystem
import akka.stream.Supervision.{Resume, Stop}
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.stream.scaladsl.{RestartSource, Sink, Source}
import scala.concurrent.duration._
import scala.language.postfixOps

import scala.util.Random

object FaultTolerance extends App {

  implicit val system = ActorSystem("FaultTolerance")
  implicit val materializer = ActorMaterializer()

  // 1- logging
  val faultySource = Source(1 to 10).map(e => if (e==6) throw new RuntimeException else e)
  faultySource.log("trackingElements").to(Sink.ignore)
    //.run()

  // 2 - gracefully terminating a stream
  faultySource.recover {
    case _:RuntimeException => Resume
  }.log("gracefulSource")
    .to(Sink.ignore)
    .run()

  // 3 - recover woth another stream
  faultySource.recoverWithRetries(3, {
    case _:RuntimeException => Source(90 to 99)
  }).log("recoverWithRetries")
    .to(Sink.ignore)
    //.run()

  // 4 - backof supervision
  val restartSource = RestartSource.onFailuresWithBackoff(
    minBackoff = 1 second,
    maxBackoff = 30 seconds,
    randomFactor = 0.2,
  )(() => {val randomNumber = new  Random().nextInt(20)
           Source(1 to 10).map(elem => if (elem == randomNumber) throw new RuntimeException else elem)})

  restartSource.log("restartBackOff")
    .to(Sink.ignore)
   //.run()

  // 5 - supervision strategy
  val numberSource = Source(1 to 20).map(n => if (n == 13) throw new RuntimeException("bad luck") else n).log("supervision")
  val supervisedFlow = numberSource.withAttributes(ActorAttributes.supervisionStrategy{
    case _:RuntimeException => Resume
    case _ => Stop
  })

  //supervisedFlow.to(Sink.ignore).run()
}
