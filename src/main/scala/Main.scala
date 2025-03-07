import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}
import akka.stream.Materializer
import akka.http.scaladsl.Http
import scala.util.{Failure, Success}
import akka.actor.typed.Scheduler


object Main extends App {

  /* Utile pour débugger l'API directement via Scala pour l'instant, mais disparaîtra un moment

  // Créer un ActorSystem
  val system: ActorSystem[FinnhubActor.Command] = ActorSystem(FinnhubActor(), "FinnhubActorSystem")

  // Utiliser la méthode getStockQuote pour récupérer les informations d'une action
  val symbol = "GOOG"
  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val ec: ExecutionContextExecutor = system.executionContext
  implicit val scheduler: Scheduler = system.scheduler
  
  val response = system.ask(ref => FinnhubActor.GetStockPrice(symbol, ref))
  // println("Request sent, waiting for response...")
  Await.result(response, timeout.duration)

  response.onComplete {
    case Success(price) =>
      println(s"Quote for $symbol: $price")
    case Failure(exception) =>
      println(s"Failed to fetch stock quote: ${exception.getMessage}")
  }
  */

  // Lancer le serveur HTTP
  Server.startServer()

}
