// --------------------------------------------------- Imports

import akka.actor.{Actor, ActorRef, ActorSystem, Props, PoisonPill}
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import scala.io.StdIn
import scala.concurrent.{Future,ExecutionContext}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import akka.http.scaladsl.model.FormData

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._

import ch.megard.akka.http.cors.scaladsl.CorsDirectives._


// --------------------------------------------------- Usable routes for frontend

object Routes {

  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val ec : ExecutionContext = Global.system.dispatcher

  def signinRoute:Route = {
    path("signin") {
      post {
        entity(as[FormData]) { formData =>
          val fields = formData.fields.toMap
          val email = fields.getOrElse("email", "") 
          val password = fields.getOrElse("password", "")
          val user = UsersDB.signin(email,password)
          if(user.isDefined){
            val token = user match {
              case Some(u) => AuthService.generateJWT(u.email)
              case None => ""
            }
            complete(s"""{"token": "$token"}""")
          }   
          else{
            complete("""{"token": 0}""")
          }
        }
      }
    }
  }

  def signupRoute: Route = {
    path("signup") {
      post {
        entity(as[FormData]) { formData =>
          val fields = formData.fields.toMap
          val email = fields.getOrElse("email", "") 
          val password = fields.getOrElse("password", "")
          val firstName = fields.getOrElse("firstName","")
          val lastName = fields.getOrElse("lastName","")
          val dateOfBirth = fields.getOrElse("dateOfBirth","")  

          if(UsersDB.emailExists(email)){
            complete("""{"status": -1}""")
          }

          val id = UsersDB.generateID()

          val user=new User(id, firstName, lastName, dateOfBirth, email, password, new Wallet(id,1000,List.empty[Asset],false), new Wallet(id,1000,List.empty[Asset],true))

          if(UsersDB.signup(user)){
            complete("""{"status": 0}""")
          }
          else{
            complete("""{"status": -2}""")
          }
        }
      }
    }
  }

  def userinfoRoute: Route = {
    path("userinfo") {
      get {
        optionalHeaderValueByName("Authorization") {
          case Some(tokenHeader) if tokenHeader.startsWith("Bearer ") =>
            val token = tokenHeader.replace("Bearer ", "")
            AuthService.extractEmailFromToken(token) match {
              case Some(email) =>
                onComplete(UsersDB.getUserByEmail(email)) {
                  case Success(Some(user:User)) => 
                  complete(s"""{
                    "status": 0,
                    "firstName": "${user.firstName}",
                    "lastName": "${user.lastName}",
                    "dateOfBirth": "${user.dateOfBirth}",
                    "email": "${user.email}"
                  }""")
                  case Success(None)       => 
                    complete("""{"status": -1}""")
                  case Failure(ex)         => 
                    complete("""{"status": -2}""")
                }
              case None => complete("""{"status": -3}""")
            }
          case _ => complete("""{"status": -4}""")
        }
      }
    }
  }

  def stockRoute(finnhub: ActorRef): Route =
    // Exemple : http://localhost:8080/stock/GOOG
    path("stock" / Segment) { symbol =>
      get {
        val futurePrice: Future[Double] = (finnhub ? FinnhubActor.GetStockPriceAlt(symbol)).mapTo[Double].recover {
          case ex =>
            println(s"Erreur lors de l'appel API: ${ex.getMessage}")
            -1.0
          }
        onSuccess(futurePrice) { price =>
          complete(s"""{"symbol": "$symbol", "price": $price}""")
        }
      }
    }

  def assetRoute: Route = {
    // Exemple : http://localhost:8080/asset/7777
    path("asset" / Segment) { id =>
      get {
        onComplete(DB.getAssetFromDB(id.toInt)) {
          case scala.util.Success(Some(asset)) =>
            complete(asset.toJson)
          case scala.util.Success(None) =>
            complete(s"Asset avec ID $id non trouvé.")
          case scala.util.Failure(exception) =>
            complete(s"Erreur serveur: ${exception.getMessage}")
        }
      }
    }
  }

  def companyRoute(finnhub : ActorRef): Route = 
    // Exemple : http://localhost:8080/company/GOOG
    path("company" / Segment) { symbol =>
      get {
        val futureName: Future[String] = (finnhub ? FinnhubActor.GetCompanyName(symbol)).mapTo[String].recover {
          case ex =>
            println(s"Erreur lors de l'appel API : ${ex.getMessage}")
            "Erreur interne"
        }
      onSuccess(futureName) { companyName =>
          complete(s"""{"symbol": "$symbol", "companyName": "$companyName"}""")
        }
      }  
    }
  
  def detailsRoute(finnhub: ActorRef): Route =
  // Exemple : http://localhost:8080/details/GOOG
  path("details" / Segment) { symbol =>
    get {
      val futureDetails: Future[String] = (finnhub ? FinnhubActor.GetDetails(symbol)).mapTo[String].recover {
        case ex =>
          println(s"Erreur lors de l'appel API : ${ex.getMessage}")
          "Erreur interne"
      }
      onSuccess(futureDetails) { details =>
        complete(s"""{"symbol": "$symbol", "details": $details}""")
      }
    }
  }

  def suggestionsRoute(finnhub : ActorRef) : Route =
    //Exemple : http://localhost:8080/suggestion/GOOG
    path("suggestion" / Segment) { query =>
      get {
        val futureSuggestions : Future[String] = (finnhub ? FinnhubActor.GetSuggestions(query)).mapTo[String].recover {
          case ex =>
            println(s"Erreur lors de l'appel API : ${ex.getMessage}")
            "Erreur interne"
        }
        onSuccess(futureSuggestions) { suggestions =>
          complete(s"""{"query": "$query", "details": $suggestions}""")
        }
      }  
    }

  // Route combinée
  def allRoutes(finnhub: ActorRef): Route = ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors() {
    signinRoute ~
    signupRoute ~
    userinfoRoute ~
    assetRoute ~
    stockRoute(finnhub) ~
    companyRoute(finnhub) ~
    detailsRoute(finnhub) ~
    suggestionsRoute(finnhub)
  }
}


// --------------------------------------------------- AKKA Server for communication with frontend

object Server {

  def startServer(): Unit = {

    implicit val classicSystem = Global.system.classicSystem
    
    implicit val executionContext = Global.system.dispatcher
    implicit val timeout: Timeout = Timeout(5.seconds)
    implicit val scheduler: akka.actor.Scheduler = Global.system.scheduler

    // Appelle la fonction combinant toutes les routes
    val route = Routes.allRoutes(Global.finnhub)

    // Démarrage du serveur
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println("Serveur en ligne sur http://localhost:8080/\nAppuyez sur ENTER pour arrêter...")

    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => Global.system.terminate())
  }
}
