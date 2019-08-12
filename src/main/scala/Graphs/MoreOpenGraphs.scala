package Graphs

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, FanOutShape2, UniformFanInShape, scaladsl}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipWith}

object MoreOpenGraphs extends App{

  implicit val system = ActorSystem("MoreOpenGraphs")
  implicit val materializer = ActorMaterializer()

  val max3StaticGraph = GraphDSL.create()
  {
    implicit builder =>
      import GraphDSL.Implicits._

      val max1 = builder.add(ZipWith[Int,Int,Int]((a,b) => Math.max(a,b)))
      val max2 = builder.add(ZipWith[Int,Int,Int]((a,b) => Math.max(a,b)))

      max1.out ~> max2.in0

      UniformFanInShape(max2.out, max1.in0, max1.in1,max2.in1)
  }

  val source1 = Source(1 to 10)
  val source2 = Source(1 to 10).map(x => 5)
  val source3 = Source((1 to 10).reverse)

  val maxSink = Sink.foreach[Int](x => println(s"Max is: $x"))

  val max3RunnableGraph = RunnableGraph.fromGraph(
    GraphDSL.create(){ implicit builder =>
      import GraphDSL.Implicits._
      val max3Shape = builder.add(max3StaticGraph)

      source1 ~>max3Shape.in(0)
      source2 ~>max3Shape.in(1)
      source3 ~>max3Shape.in(2)
      max3Shape.out ~> maxSink

      ClosedShape
    }
  )
  //max3RunnableGraph.run()

  case class Transaction(id: String, source: String, recipient: String, amount: Int, date:Date)

  val transactionSource = Source(List(
    Transaction("1234567890", "Bruno", "Jose", 100, new Date),
    Transaction("1234445555", "Jose", "Saidera", 100000, new Date),
    Transaction("1234444444", "Bruno", "Seisa", 1000, new Date)))

  val bankProcessor = Sink.foreach[Transaction](println)
  val suspeciousTransactionProcessor = Sink.foreach[Transaction](transaction => println(s"Suspecious transaction: $transaction.id"))

  val suspeciousBankTransactionStaticGraph = GraphDSL.create(){
    implicit builder =>
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[Transaction](2))
      val suspeciousTxnFilter = builder.add(Flow[Transaction].filter(transaction => transaction.amount > 1000))
      val transactionExtractor = builder.add(Flow[Transaction].map[Transaction](trans => trans))

      broadcast.out(0) ~> suspeciousTxnFilter ~> transactionExtractor

      new FanOutShape2(broadcast.in, broadcast.out(1), transactionExtractor.out)
  }

  val suspiciousRunnableGraph = RunnableGraph.fromGraph(
    GraphDSL.create(){
      implicit builder =>
        import GraphDSL.Implicits._

        val suspiciusTnxShape = builder.add(suspeciousBankTransactionStaticGraph)

        transactionSource ~> suspiciusTnxShape.in
        suspiciusTnxShape.out0 ~> bankProcessor
        suspiciusTnxShape.out1 ~> suspeciousTransactionProcessor

        ClosedShape
    }
  )
  suspiciousRunnableGraph.run()
}

