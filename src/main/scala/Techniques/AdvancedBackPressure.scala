package Techniques

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Source, Sink}
import scala.concurrent.duration._
import scala.language.postfixOps

object AdvancedBackPressure extends App{

  implicit val system = ActorSystem("AdvancedBackPressure")
  implicit val materializer = ActorMaterializer()

  val controlledFlow = Flow[Int].map(_* 2).buffer(10, OverflowStrategy.dropHead)

  case class PageEvent(description: String, date: Date, instances: Int = 1)
  case class Notification(email: String, pageEvent: PageEvent)

  val events = List(
    PageEvent("xxxxxx", new Date),
    PageEvent("yyyyy", new Date),
    PageEvent("zzzzz", new Date)
  )

  val eventSource = Source(events)

  val onCallEngineer = "xxxx@gmail.com"

  def sendEmail(notification: Notification) = println(notification)

  val notificationSink = Flow[PageEvent].map(event => Notification(onCallEngineer, event)).to(Sink.foreach[Notification](sendEmail))

  //eventSource.to(notificationSink).run()

  def sendEmailSlow(notification: Notification) = {
    Thread.sleep(1000)
    println(s" Dear ${notification.email} you have ${notification.pageEvent}")
  }

  val aggregateNotificationFlow = Flow[PageEvent].conflate((event1, event2) =>{
    val inst = event1.instances + event2.instances
    PageEvent(s"Instances $inst", new Date, inst)
  } ).map(x => Notification(onCallEngineer, x))

  //eventSource.via(aggregateNotificationFlow).async.to(Sink.foreach[Notification](sendEmailSlow)).run()

  val slowCounter = Source(Stream.from(1)).throttle(1, 1 second)
  val hungrySink = Sink.foreach[Int](println)
  val extrapolator = Flow[Int].extrapolate(element  => Iterator.from(element))
  val repeater = Flow[Int].extrapolate(element => Iterator.continually(element))

  slowCounter.via(repeater).to(hungrySink).run()

}
