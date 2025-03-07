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
import scala.io.StdIn

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

    // Démarrage du serveur
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println("Serveur en ligne sur http://localhost:8080/\nAppuyez sur ENTER pour arrêter...")

    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}

