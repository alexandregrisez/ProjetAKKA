
// ------------------------------------------------- Imports

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.model._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import HttpMethods._

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._

// ------------------------------------------------- Finnhub API Actor

object FinnhubActor {

  implicit val system: ActorSystem = ActorSystem("FinnhubActorSystem")
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContext = system.dispatcher

  // Clé pour l'utilisation
  val apiKey = "cv4nc6hr01qn2gab5ju0cv4nc6hr01qn2gab5jug"

  // Une action
  case class StockQuote(
    c: Double,  // 'c' - Prix actuel
    d: Double,  // 'd' - Variation en valeur absolue
    dp: Double, // 'dp' - Variation en pourcentage
    h: Double,  // 'h' - Prix le plus haut
    l: Double,  // 'l' - Prix le plus bas
    o: Double,  // 'o' - Prix d'ouverture
    pc: Double, // 'pc' - Prix de clôture précédent
    t: Long     // 't' - Timestamp UNIX
  )
  
  // Messages que l'actor peut recevoir
  sealed trait Command
  case class GetStockPrice(symbol:String, replyTo: ActorRef[Double]) extends Command

  // apply() définit le comportement de l'acteur pour chaque message possible (ici 1 seul)
  def apply(): Behavior[Command] = Behaviors.receive { (context, message) =>
    message match {
      case GetStockPrice(symbol, replyTo) =>
        getPrice(symbol, replyTo)
        Behaviors.same
      case _ =>
        Behaviors.ignore
    }
  }

  // Fonction pour faire appel à l'API
  def getPrice(symbol: String, replyTo: ActorRef[Double]): Unit = {

    // Requête
    val url = s"https://finnhub.io/api/v1/quote?symbol=$symbol&token=$apiKey"
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(url)
    )

    // Exécution 
    Http().singleRequest(request).onComplete {
        case scala.util.Success(response: HttpResponse) if response.status.isSuccess() =>
        // Réponse réussie, désérialisation du JSON
        // println(s"Request successful. Status: ${response.status}")
        response.entity.toStrict(5.seconds).map { strictEntity =>
            val jsonResponse = strictEntity.data.utf8String
            // println(s"Raw JSON Response: $jsonResponse") 
            decode[StockQuote](strictEntity.data.utf8String) match {
            case Right(stockQuote) =>
                // println(s"Successfully decoded StockQuote")
                replyTo ! stockQuote.c
            case Left(error) =>
                // println(s"Error decoding JSON: ${error.getMessage}")
                replyTo ! -2.0
            }
      }

        case scala.util.Success(response) =>
        // Erreur HTTP (par exemple 404, 500)
        //println(s"Request failed with HTTP status: ${response.status}")
        replyTo ! -3.0

        case scala.util.Failure(exception) =>
        // En cas d'échec dans la requête HTTP elle-même
        //println(s"Request failed with exception: ${exception.getMessage}")
        replyTo ! -1.0
    }
  }    
}



