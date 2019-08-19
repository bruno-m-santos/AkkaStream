package Techniques

import java.util.Date

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.duration._
import scala.language.postfixOps

import scala.concurrent.Future

object AkkaWithExternalServices  extends  App{

  implicit val system = ActorSystem("Akka")
  implicit val materializer = ActorMaterializer()
  //import system.dispatcher
  implicit val dispatcher = system.dispatchers.lookup("dedicated-dispatcher")
  implicit val timeout = Timeout(2 seconds)

  def genericExternalService[A,B](element:A): Future[B] = ???

  case class PagerEvent(application: String, description: String, date: Date)

  val eventSource = Source(List(
    PagerEvent("AkkaInfra", "Infra broke", new Date()),
    PagerEvent("FastDataPipeline","Illegal elements in the data pipeline", new Date()),
    PagerEvent("AkkaInfra","A service stopped responding", new Date()),
    PagerEvent("SuperFrontend","A button doesnt work", new Date())))

  val infraEvents = eventSource.filter(_.application == "AkkaInfra")
  //val pagedEngineersEmail = infraEvents.mapAsync (parallelism = 4)( x => PagerService.processEvent(x))
  val pagedSink = Sink.foreach[String]( x => println(s"Teste $x"))
  //pagedEngineersEmail.to(pagedSink).run()

   class PagerServiceActor extends Actor {
     private val engineers = List("Daniel","John","xxx")
     private val emails = Map(
       "Daniel" -> "daniel@gmail.com",
       "John" -> "john@gmail.com",
       "xxx" -> "xxx@gmail.com"
     )

     def processEvent(pagerEvent: PagerEvent) = {
       val engineerIndex = (pagerEvent.date.toInstant.getEpochSecond/ (24 * 36000)) % engineers.length
       val engineer = engineers(engineerIndex.toInt)
       val engineerEmail = emails(engineer)

       Thread.sleep(1000)

       //println(engineerEmail)
       engineerEmail
     }

     override def receive: Receive = {
       case pagerEvent: PagerEvent =>
         sender() ! processEvent(pagerEvent)
     }
   }

   val pagedActor = system.actorOf(Props[PagerServiceActor],"PageServiceActor")
   val pagedEmails = infraEvents.mapAsync(parallelism = 4)(event => (pagedActor ? event).mapTo[String])
   pagedEmails.to(pagedSink).run()
}
