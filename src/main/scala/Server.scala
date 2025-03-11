// --------------------------------------------------- Imports
import akka.actor.{Actor, ActorRef, ActorSystem, Props, PoisonPill}
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import scala.io.StdIn
import scala.concurrent.{Future,ExecutionContext}
import scala.concurrent.duration._

// --------------------------------------------------- Usable routes for frontend

object Routes {

  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val ec : ExecutionContext = Global.system.dispatcher

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

  // Route combinée
  def allRoutes(finnhub: ActorRef): Route = {
    assetRoute ~
    stockRoute(finnhub)
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
      .onComplete(_ => Global.system.terminate())
  }
}
