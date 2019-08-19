package Basics

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future

object AkkaStreamsPrinciples extends App {

  implicit val system = ActorSystem("FirstPrinciples")
  implicit val materializer = ActorMaterializer() //needs a materializer

  //every akka stream starts with source
  val source = Source(1 to 10 ) //constructor pass a collection

  //every akka stream ends with sink
  val sink = Sink.foreach[Int](println)

  val graph = source.to(sink)
  //graph.run()

  //lets introduce flows into the picture
  val flow = Flow[Int].map(x => x+1)
  val sourceWithFlow = source.via(flow)
  val flowWithSink = flow.to(sink)

  //sourceWithFlow.to(sink).run() // one way to call
  //source.to(flowWithSink).run() // other way to call
  //source.via(flow).to(sink).run() //another way to call

  //null are not allowed
//  val illegalSource = Source.single[String](null)
//  illegalSource.to(Sink.foreach(println)).run()

  //types of sources
  val finiteSource = Source.single(1)
  val anotherFiniteSource = Source(List(1,2,3))
  val emptySource = Source.empty[Int]
  val infiteSource = Source(Stream.from(1))
  import scala.concurrent.ExecutionContext.Implicits.global
  val futureSource = Source.fromFuture(Future(1))

  //types of sinks
  val theMostBoringSink = Sink.ignore
  val foreachSink = Sink.foreach[String](println)
  val headSink = Sink.head[Int] //receives the head and finishes the strem
  val foldSink = Sink.fold[Int,Int](0)((a,b) => a + b )

  //types os flows
  val mapFlow = Flow[Int].map(x => x * 2)
  val takeFlow =  Flow[Int].take(5)

  //source -> flow -> flow -> .... -> sink
  val doubleFlow = source.via(mapFlow).via(takeFlow).to(sink)
  //doubleFlow.run()

  val mapSource = Source(1 to 10).map(x => x * 2) // Source(1 to 10).via(Flow[Int].map(x => x*2))
  //mapSource.runForeach(println) // mapSource.to(Sink.foreach[Int](println))

  val exercSource = Source(List("Seisa","Danilo", "Danilo", "Bruno", "Rita","Osvaldo"))
  //val exercFlow = Flow[String].map(name => name.toUpperCase).filter(_.length() > 5)//.take(3)
  //val exercFlow = Flow[String].map(name => name.toUpperCase).filter(_.length() > 5).take(3)
  val exercFlow = Flow[String].map(name => name.toUpperCase).filter(name => name.length() > 5) .take(3)
  val exerctakeFlow = Flow[String].take(3)

  exercSource.via(exercFlow).via(exerctakeFlow).to(Sink.foreach(println)).run()

}
