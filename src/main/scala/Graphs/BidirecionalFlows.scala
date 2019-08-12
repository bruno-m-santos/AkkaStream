package Graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, BidiShape, ClosedShape}
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}

object BidirecionalFlows extends App{

  implicit val system = ActorSystem("BidirecionalFlows")
  implicit val materializer = ActorMaterializer()

  def encrypt(n:Int)(string: String) = string.map(c => (c + n).toChar)
  def decrypt(n:Int)(string: String) = string.map(c => (c -  n).toChar)
  //println(encrypt(3)("Akka"))

  val bidiGraphStatic = GraphDSL.create() {
    implicit builder =>
      val encryptFlow = builder.add(Flow[String].map(encrypt(3)))
      val decryptFlow = builder.add(Flow[String].map(decrypt(3)))

      BidiShape.fromFlows(encryptFlow, decryptFlow)
  }

  val unencryptedString = List("Akka", "Seisa", "Bruno")
  val unencryptedSource = Source(unencryptedString)
  val encryptedSource = Source(unencryptedString.map(encrypt(3)))

  val cryptoBidiGraph = RunnableGraph.fromGraph(
    GraphDSL.create(){
      implicit builder =>
        import GraphDSL.Implicits._

        val unencryptedSourceShape = builder.add(unencryptedSource)
        val encryptedSourceShape = builder.add(encryptedSource)

        val bidi = builder.add(bidiGraphStatic)

        val encryptedSinkShape = builder.add(Sink.foreach[String](string => println(s"Encrypted: $string")))
        val decryptedSinkShape = builder.add(Sink.foreach[String](string => println(s"Decrypted: $string")))

        unencryptedSourceShape ~> bidi.in1; bidi.out1 ~> encryptedSinkShape
        bidi.in2 <~ encryptedSourceShape; decryptedSinkShape <~ bidi.out2

        ClosedShape
    }
  )

  cryptoBidiGraph.run()

}
