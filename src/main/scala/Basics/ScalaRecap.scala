package Basics

import scala.concurrent.Future
import scala.util.{Failure, Success}

object ScalaRecap extends App {

  val condition: Boolean = false

  def myFunction(x:Int) = {}

  class Animal

  trait Carnivore{
    def eat(a:Animal):Unit
  }

  object Carnivore

  abstract class MyList[+A]
  //val anIncremental: Function1[Int, Int]
  val anIncrementalAnonymous: Int => Int = (x:Int) => x +1

  anIncrementalAnonymous(1)
  // HOF : flatMap, filter
  //for-comprehensions
  //Monads ? Option, Try

  // Pattern matching
  val unknown : Any = 2
  val order =  unknown match {
    case 1 => print(1)
    case 2 => print(2)
    case _ => println("unknown")
  }

  try{
    throw new RuntimeException
  }catch{
    case e: Exception => print("Caught")
  }

  //Scala advanced
  // multithreading

  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future{
    //long computation here
    //executed on some other thread
  }
  //map , flatMap, filter + recover, recoverWith
  future.onComplete{
    case Success(value) => println("Sucess")
    case Failure(exception) => println("Failure")
  } // executed on some thread

  //partial function
  val partionFunction : PartialFunction[Int, Int] = {
    case 1 => 99
    case 2 => 100
    case _ => 1000
  } //base on pattern matching

  //implicits
  implicit val timeout = 3000
  def setTimeout(f:() => Unit) (implicit  timeout: Int) = f()

  setTimeout(() => println("timeout")) // the compiler inject the arg

  case class Person(name: String){
    def greet: String = s"Hello $name"
  }

  implicit def fromStringToPerson(name: String) = Person(name)

  "Peter".greet // new Person("Peter").greet

  implicit val numberOrdering: Ordering[Int] = Ordering.fromLessThan(_>_)
  List(1,2,3).sorted // injected (numberOrdering)

}
