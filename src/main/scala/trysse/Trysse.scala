package trysse

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source,Sink}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives

import scala.io.StdIn
import scala.concurrent.duration.DurationInt

import java.time.LocalTime

import de.heikoseeberger.akkasse.{ EventStreamMarshalling, ServerSentEvent }

object Trysse extends App {
  import Directives._
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  def source = Source.tick(2.seconds, 2.seconds, akka.NotUsed).map((_)=>LocalTime.now().toString()).map(ServerSentEvent(_, id = Some("1"))).alsoTo(Sink.onComplete((_)=> println("Oh no")))

  def route = { 
    import EventStreamMarshalling._
    path("events"){ complete(source)}
  }

  val bindingFuture = Http().bindAndHandle(route, interface = "localhost", port = 5050)
  println(s"Server is ready to send stuff")

  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_=> system.shutdown())
}
