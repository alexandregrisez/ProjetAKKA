// ------------------------------------------------- Imports

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.model._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import HttpMethods._

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._

// ------------------------------------------------- Finnhub API Actor

object FinnhubActor{
  case class GetStockPrice(symbol: String, replyTo: ActorRef)
  case class GetStockPriceAlt(symbol: String)
  case class GetCompanyName(symbol : String)
}

class FinnhubActor extends Actor{
  import context._
  
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

  case class CompanyName(
    name : String
  )

  def receive: Receive = {
    case FinnhubActor.GetStockPrice(symbol, replyTo) =>
      getPrice(symbol, replyTo)
    case FinnhubActor.GetStockPriceAlt(symbol) =>
      val priceFuture: Future[Double] = getPrice(symbol)
      val requester = sender()
      priceFuture.onComplete {
        case Success(price) =>
          requester ! price 
        case Failure(exception) =>
          requester ! -1.0
      }
    case FinnhubActor.GetCompanyName(symbol) =>
      val companyFuture : Future[String] = getCompanyName(symbol)
      val requester = sender()
      companyFuture.onComplete{
        case Success(companyName) =>
          requester ! companyName
        case Failure(exception) =>
          requester ! "Erreur interne"
      }
  }

  // Fonction pour faire appel à l'API
  def getPrice(symbol: String, replyTo: ActorRef): Unit = {
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
        response.entity.toStrict(5.seconds).map { strictEntity =>
          val jsonResponse = strictEntity.data.utf8String
          decode[StockQuote](jsonResponse) match {
            case Right(stockQuote) =>
              replyTo ! stockQuote.pc
            case Left(error) =>
              replyTo ! -2.0
          }
        }

      case scala.util.Success(response) =>
        // Erreur HTTP (par exemple 404, 500)
        replyTo ! -3.0

      case scala.util.Failure(exception) =>
        // En cas d'échec dans la requête HTTP elle-même
        replyTo ! -1.0
    }
  }

  // Version sans retourner un message à un autre acteur
  def getPrice(symbol: String): Future[Double] = {
    // Requête
    val url = s"https://finnhub.io/api/v1/quote?symbol=$symbol&token=$apiKey"
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(url)
    )
    
    // Exécution de la requête HTTP
    val futureResponse: Future[HttpResponse] = Http().singleRequest(request)

    futureResponse.flatMap { response =>
      if (response.status.isSuccess()) {
        // Réponse réussie, désérialisation du JSON
        response.entity.toStrict(5.seconds).map { strictEntity =>
          val jsonResponse = strictEntity.data.utf8String
          decode[StockQuote](jsonResponse) match {
            case Right(stockQuote) =>
              stockQuote.pc // On retourne le prix actuel
            case Left(error) =>
              -2.0 // En cas d'erreur de décodage, retourner une valeur par défaut
          }
        }
      } 
      else {
        // Erreur HTTP (par exemple 404, 500)
        Future.successful(-3.0) // Retourner une valeur par défaut pour une erreur HTTP
      }
    }.recover {
      case _ => -1.0 // Si une exception se produit, on retourne -1.0
    }
  }

  def getCompanyName(symbol : String) : Future[String] = {
    //Requête
    val url = s"https://finnhub.io/api/v1/stock/profile2?symbol=$symbol&token=$apiKey"
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(url)
    )

    //Exécution de la requete HTTP
    val futureResponse : Future[HttpResponse] = Http().singleRequest(request)

    futureResponse.flatMap { response =>
      if (response.status.isSuccess()) {
        //Réponse réussite, désérialisation du json
        response.entity.toStrict(5.seconds).map { strictEntity =>
         val jsonResponse = strictEntity.data.utf8String
         decode[CompanyName](jsonResponse) match {
            case Right(companyName) =>
              companyName.name //On retourne le nom de l'entreprise
            case Left(error) =>
              "Erreur de décodage"
         } 
        }
      } 
      else {
        Future.successful("Erreur HTTP")
      }
      }.recover {
        case _ => "Erreur interne"
    }
  }
}

