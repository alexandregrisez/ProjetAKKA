error id: akka/http/scaladsl/
file://<WORKSPACE>/src/main/scala/Server.scala
empty definition using pc, found symbol in pc: akka/http/scaladsl/
semanticdb not found
|empty definition using fallback
non-local guesses:
	 -

Document text:

```scala

// --------------------------------------------------- Imports

import akka.actor.typed.ActorSystem
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.Scheduler

import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import scala.io.StdIn

// --------------------------------------------------- Usable routes for frontend

object Routes {
  
  // Route pour l'Asset
  def assetRoutes: Route = pathPrefix("asset") {
    path(IntNumber) { id =>
      get {
        // logiques pour récupérer un Asset par ID
        complete(s"Asset avec ID $id")
      }
    }
  }

  // Route pour Stock
  def stockRoutes: Route = pathPrefix("stock") {
    path(Segment) { symbol =>
      get {
        // logiques pour récupérer une action via Finnhub
        complete(s"Prix pour $symbol")
      }
    }
  }

  // Autres routes
  def otherRoutes: Route = pathPrefix("other") {
    path("example") {
      get {
        // logiques pour cette route
        complete("Example route")
      }
    }
  }

  // Route combinée
  def allRoutes: Route = {
    assetRoutes ~
    stockRoutes ~
    otherRoutes ~
    // Ajoute d'autres routes ici
    complete("Toutes les routes combinées")
  }
}


// --------------------------------------------------- AKKA Server for communication with frontend

object Server {

  def startServer(): Unit = {

    // Définition du système d'acteurs et des acteurs participants
    val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "GlobalSystem")
    val finnhub: ActorRef[FinnhubActor.Command] = system.systemActorOf(FinnhubActor(), "FinnhubActor")
    implicit val classicSystem: akka.actor.ClassicActorSystemProvider = system.classicSystem

    implicit val executionContext = system.executionContext
    implicit val timeout: Timeout = Timeout(5.seconds)
    implicit val scheduler: Scheduler = system.scheduler


    // Appelle la fonction combinant toutes les routes
    val route = Routes.allRoutes 

    // Exemple : http://localhost:8080/stock/GOOG
    val route =
      path("stock" / Segment) { symbol =>
        get {
          val futurePrice: Future[Double] = finnhub.ask(ref => FinnhubActor.GetStockPrice(symbol, ref))
          onSuccess(futurePrice) { price =>
            complete(s"""{"symbol": "$symbol", "price": $price}""")
          }
        }
      }

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


```

#### Short summary: 

empty definition using pc, found symbol in pc: akka/http/scaladsl/