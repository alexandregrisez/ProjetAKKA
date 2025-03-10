
// --------------------------------------------------- Imports

import akka.actor.typed.ActorSystem
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.Scheduler

import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Future

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import scala.io.StdIn

// --------------------------------------------------- Usable routes for frontend

object Routes {

  def stockRoute(system: ActorSystem[Nothing],finnhub: ActorRef[FinnhubActor.Command]):Route =
    // Exemple : http://localhost:8080/stock/GOOG
    path("stock" / Segment) { symbol =>
      get {
        val futurePrice: Future[Double] = finnhub.ask(ref => FinnhubActor.GetStockPrice(symbol, ref))(Timeout(5.seconds), system.scheduler)
        onSuccess(futurePrice) { price =>
          complete(s"""{"symbol": "$symbol", "price": $price}""")
        }
      }
    }

  // Route pour l'Asset
  def assetRoute: Route = {
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

  // Route combinée
  def allRoutes(system: ActorSystem[Nothing],finnhub: ActorRef[FinnhubActor.Command]): Route = {
    assetRoute ~
    stockRoute(system,finnhub)
  }
}


// --------------------------------------------------- AKKA Server for communication with frontend

object Server {

  def startServer(): Unit = {

    // Définition du système d'acteurs et des acteurs participants
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "GlobalSystem")
    implicit val finnhub: ActorRef[FinnhubActor.Command] = system.systemActorOf(FinnhubActor(), "FinnhubActor")
    implicit val classicSystem: akka.actor.ClassicActorSystemProvider = system.classicSystem

    implicit val executionContext = system.executionContext
    implicit val timeout: Timeout = Timeout(5.seconds)
    implicit val scheduler: Scheduler = system.scheduler

    // Appelle la fonction combinant toutes les routes
    val route = Routes.allRoutes(system,finnhub)

    // Utilisation explicite de la méthode cors depuis ch.megard.akka.http.cors.scaladsl.CorsDirectives
    val corsRoute = ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors() {
      route
    }

    // Démarrage du serveur
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(corsRoute)
    println("Serveur en ligne sur http://localhost:8080/\nAppuyez sur ENTER pour arrêter...")

    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}

