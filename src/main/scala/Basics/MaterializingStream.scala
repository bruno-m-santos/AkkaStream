package Basics

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

object MaterializingStream extends App {

  implicit val system = ActorSystem("MaterializingStreams")
  implicit val materializer = ActorMaterializer()

  val graph = Source(1 to 10).to(Sink.foreach(println))
  //val simpleMaterialized = graph.run()

  val source = Source(1 to 10)
  //val source = Source(1 to 10).to(Sink.reduce[Int]((a,b) =>  a+ b))
//  val sink = Sink.reduce[Int]((a,b) => a + b)
//  val someFuture = source.runWith(sink)
//  someFuture.onComplete{
//    case Success(value) =>  println(s"Sucess $value")
//    case Failure(exception) => print("Failure")
//  }

  //choosing materiliazed value
//  val simpleSource = Source(1 to 10)
//  val simpleFlow = Flow[Int].map(x => x * 2)
//  val simpleSink = Sink.foreach(println)
//  val graph1 = simpleSource.viaMat(simpleFlow)((a,b) => b).toMat(simpleSink)((a,b) => b)
//  //val graph1 = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)
//  //val graph1 = simpleSource.viaMat(simpleFlow)((a,b) => a).toMat(simpleSink)((a,b) => a) => returns a runnable graph but no "Futurable"
//  graph1.run().onComplete{
//    case Success(value) => println(value)
//    case Failure(exception) => print("Erro")
//  }

  //sugar sintax
//  Source(1 to 10).runWith(Sink.reduce(_+_))
//  Source(1 to 10).runReduce(_+_)

  //-------------------------------------------------------------------
  //# 1 way
  val exercSouce = Source(List("Rita", "Bruno", "Seisa", "Danilo", "Osvaldo"))
  //Flow[String].map(name => println(name)).runWith(exercSouce,Sink.last )
//  exercSouce.runWith(Sink.last).onComplete{
//    case Success(value) => println(value)
//    case Failure(e) => println(e)
//  }
  // # 2 way
//  exercSouce.toMat(Sink.last)(Keep.right).run().onComplete{
//    case Success(value) => println(value)
//    case Failure(e) => println(e)
//  }

  // # 3 way
  val exercSink1 = Sink.last.runWith(exercSouce)
  val exercflow = Flow[String].map(name => name.toUpperCase)
//  exercSouce.viaMat(exercflow)(Keep.right).runWith(Sink.last).onComplete{
//    case Success(value) => println(value)
//    case Failure(e) => println(e)
//  }
  //-------------------------------------------------------------------

  val listExerc = List("bbbbbbbbbbb bbbbbbbbbbb","aaaaaaaaaaa aaaaaaaaaaaaaaaaaaaa", "cccccccc cccccccccccccccccccc")

  val exercSource2 = Source(listExerc)
//  exercSource2.viaMat(Flow[String].map(sentence => sentence.length()))(Keep.right).toMat(Sink.foreach(println))(Keep.right).run().onComplete{
//    case Success(value) => println(value)
//    case Failure(e) => println(e)
//  }

  //-------------------------------------------------------------------
  val listExerc2 = List("bbbb bbbbb bbbbb bbbbb b b b","aaaaa aaaaaa aaaaaaaa aaaaaaa aaaaa", "ccccccc ccccc cccccc cccc cccccc cccc")
  val exercSource3 = Source(listExerc2)
  val exercFlow2 = Flow[String].map(sentences => sentences.split(" "))
  //exercSource3.via(exercFlow2).to(Sink.foreach(println)).run()

  val wordCountFlow = Flow[String].fold[Int](0)((a,b) => b.split(" ").length)
//  exercSource3.via(wordCountFlow).toMat(Sink.head)(Keep.right).run().onComplete{
//    case Success(value) => println(value)
//    case Failure(e) => println(e)
//  }
  //val exercFlow3 = Flow[String].map(string => string.length())
  //Source(listExerc2).viaMat(exercFlow2)(Keep.left).viaMat(exercFlow3)(Keep.right)
  //-----------------------------------------------------------------------------------------

  val listExerc3 = List("bbbb bbbbb bbbbb bbbbb b b b a","aaaaa aaaaaa aaaaaaaa aaaaaaa aaaaa", "ccccccc ccccc cccccc cccc cccccc cccc")
  val exercSource4 = Source(listExerc3)
  val exercFlow3 = Flow[String].map(sentences => sentences.split(" ").length)
  val exercFlow4 = Flow[Int].reduce((x,y) => x + y)
  //val exercFlow3 = Flow[String].map(sentences => sentences.split(" ").length)
  //exercSource4.via(exercFlow3).to(Sink.foreach(println)).run()
  exercSource4.viaMat(exercFlow3)(Keep.left).async.viaMat(exercFlow4)(Keep.left).async.toMat(Sink.foreach(println))(Keep.left).async.run()
//  exercSource4.viaMat(exercFlow3)(Keep.right).toMat(Sink.head)(Keep.right).run().onComplete{
//    case Success(value) => println(value)
//    case Failure(e) => println(e)
//  }


}
