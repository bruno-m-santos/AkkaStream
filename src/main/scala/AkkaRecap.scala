import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorSystem, Kill, OneForOneStrategy, PoisonPill, Props, Stash, SupervisorStrategy}
import akka.util.Timeout

object AkkaRecap extends App{

  class AnotherActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => { print(s" Message to another simple actor $message")}
      sender ! "And Hello to you!"
    }
  }
  class SimpleActor extends Actor with ActorLogging with Stash {
    override def receive: Receive = {
      case "createChild" =>
        val childActor = context.actorOf(Props[SimpleActor], "myChild" )
        childActor ! "Hello child"
      case "stashThis" =>
        stash()
      case "UnstashAll" =>
        unstashAll()
        context.become(anotherHandler)
      case "change" => context.become(anotherHandler)
      case message =>
        stash()
        //println(s"Received message: $message" )
      case reply => println(reply)
    }

    def anotherHandler: Receive = {
      case message => println(s"Received in another handler message: $message")
    }

    override def preStart(): Unit = {
      //println("Starting phase")
      log.info("Starting phase")
    }

    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy (){
      case _:RuntimeException => Stop
      case _ => Stop
    }
  }

  val system = ActorSystem("AkkaRecap")
  // #1 you can only instantiate an actor throught the actor system
  val actor = system.actorOf(Props[SimpleActor], "simpleActor")


  //#2 sending message (call tell method)
  actor ! "message Hello"
  //actor ! "createChild"
  //actor ! "stashThis"
  //actor ! "change1111111111"
  //actor ! "stashThis"
  actor ! "stashThis1"
  actor ! "stashThis2"
  //actor ! "change handler now"
  //actor ! "will received in other context"
  //actor ! "message Hello"
  actor ! "UnstashAll"
  actor ! "will received in other context"

  // stops actor
  //actor ! Kill

  /*
  / - messages are sent asynchronously
  / - many actors can share a few dozen threads
  / - each message is processed/handles ATOMICALLY
  / - no need for locks
   */

  //actors can spwn other actors
  // actors life cycles
  // akka's guardians
  //logging
  //supervision


  //scheduler

  import scala.concurrent.duration._
  import system.dispatcher

  import scala.language.postfixOps
//  system.scheduler.scheduleOnce(2 seconds) {
//    actor ! "delayed Hello"
//  }

  // akka patterns including FSM + ask pattern
  import akka.pattern.ask
  implicit val timeout = Timeout(2 seconds)
  val future = actor ? "question" // the return con be sent to "anotherSimpleActor"

  // the pipe pattern
  import akka.pattern.pipe
  val anotherActor2 = system.actorOf(Props[SimpleActor], "anotherSimpleActor")
  val anotherActor = system.actorOf(Props[AnotherActor], "AnotherActor")
  future.mapTo[String].pipeTo(anotherActor2)
}
