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

import java.net.URLEncoder

// ------------------------------------------------- Finnhub API Actor

object FinnhubActor{
  case class GetStockPrice(symbol: String, replyTo: ActorRef)
  case class GetStockPriceAlt(symbol: String)
  case class GetCompanyName(symbol: String)
  case class GetDetails(symbol: String)
  case class GetSuggestions(query : String)
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

  case class StockSuggestion(
    symbol: String, 
    description: String
  )

  case class SuggestionList(
    result: List[StockSuggestion]
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
    case FinnhubActor.GetDetails(symbol) =>
      val detailsFuture : Future[String] = getDetails(symbol)
      val requester = sender()
      detailsFuture.onComplete{
        case Success(details) =>
          requester ! details
        case Failure(exception) =>
          requester ! "Erreur interne"
      }
    case FinnhubActor.GetSuggestions(query) =>
      val suggestionFuture : Future[String] = getSuggestions(query)
      val requester = sender()
      suggestionFuture.onComplete{
        case Success(result) =>
          requester ! result
        case Failure(exception) =>
          requester ! "Erreur interne"
      }
  }

  // Fonction pour faire appel à l'API
  def getPrice(symbol: String, replyTo: ActorRef): Unit = {
    //Encodage des symbol pour la gestion des caractères spéciaux et chiffres
    val encodedSymbol = URLEncoder.encode(symbol, "UTF-8")
    // Requête
    val url = s"https://finnhub.io/api/v1/quote?symbol=$encodedSymbol&token=$apiKey"
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
              replyTo ! stockQuote.c
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
    //Encodage des symbol pour la gestion des caractères spéciaux et chiffres
    val encodedSymbol = URLEncoder.encode(symbol, "UTF-8")
    // Requête
    val url = s"https://finnhub.io/api/v1/quote?symbol=$encodedSymbol&token=$apiKey"
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
              stockQuote.c // On retourne le prix actuel
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
    //Encodage des symbol pour la gestion des caractères spéciaux et chiffres
    val encodedSymbol = URLEncoder.encode(symbol, "UTF-8")
    
    //Requête
    val url = s"https://finnhub.io/api/v1/stock/profile2?symbol=$encodedSymbol&token=$apiKey"
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
        case _ => s"Erreur interne"
    }
  }

  def getDetails(symbol: String): Future[String] = {
  // Encodage des symbol pour la gestion des caractères spéciaux et chiffres
  val encodedSymbol = URLEncoder.encode(symbol, "UTF-8")
  // Requête
  val url = s"https://finnhub.io/api/v1/quote?symbol=$encodedSymbol&token=$apiKey"
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
            // Formater la réponse avec toutes les informations
            s"""{"current" : ${stockQuote.c},
               |"open" : ${stockQuote.o},
               |"high" : ${stockQuote.h},
               |"low" : ${stockQuote.l}}""".stripMargin
          case Left(error) =>
            "Erreur de décodage"
        }
      }
    } 
    else {
      // Erreur HTTP (par exemple 404, 500)
      Future.successful("Erreur HTTP")
    }
  }.recover {
    case _ => "Erreur interne"
  }
  }

  // Obtenir des suggestions d'actif
  def getSuggestions(query: String): Future[String] = {
  // Encodage de la recherche pour la gestion des caractères spéciaux et chiffres
  val encodedQuery = URLEncoder.encode(query, "UTF-8")

  // Requête
  val url = s"https://finnhub.io/api/v1/search?q=$encodedQuery&token=$apiKey"
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
        decode[SuggestionList](jsonResponse) match {
          case Right(suggestionList) =>
            // Extraire les symboles et la description (Nom de l'entreprise)
            val filteredSuggestions = suggestionList.result.map { suggestion =>
              s"""{"symbol": "${suggestion.symbol}", "description": "${suggestion.description}"}"""
            }
            s"""{"result": [${filteredSuggestions.mkString(",")}]}"""
          case Left(error) =>
            """{"result": []}"""
        }
      }
    } else {
      // Erreur HTTP (par exemple 404, 500)
      Future.successful("""{"result": []}""")
    }
  }.recover {
    case _ => """{"result": []}""" // Gestion des erreurs générales
  }
}


}

