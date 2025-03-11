import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext

object Main extends App {
  /*
  // Utile pour débugger l'API directement via Scala pour l'instant, mais disparaîtra un moment

  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val ec : ExecutionContext = Global.system.dispatcher

  val finnhub: ActorRef = Global.system.actorOf(Props[FinnhubActor], "FinnhubActorTest")
  val symbol = "GOOG"

  val futurePrice: Future[Double] = (finnhub ? FinnhubActor.GetStockPriceAlt(symbol)).mapTo[Double]

  futurePrice.onComplete {
  case Success(price) =>
    println(s"Prix pour $symbol : $price") // Affiche le prix
  case Failure(exception) =>
    println(s"Échec : ${exception.getMessage}")
  }

  */

  /*
  // Test JSON Asset
  val asset = Asset(1, "AAPL", 2.5,"2024-03-10", 1500.0, AssetType.ETF)
  println(asset.toJson)
  */

  /*

  // Tests pour le calendrier

  // Test de now() (format correct)
  val today = Calendar.now()
  println(s"Today: $today") // Doit afficher la date du jour au format dd-MM-yyyy

  // Test de parseDate() et getDay(), getMonth(), getYear()
  val testDate = "15-03-2025"
  assert(Calendar.getDay(testDate) == 15)
  assert(Calendar.getMonth(testDate) == 3)
  assert(Calendar.getYear(testDate) == 2025)

  // Test de chronological()
  assert(Calendar.chronological("01-01-2023", "01-01-2024")) // Doit retourner true
  assert(!Calendar.chronological("01-01-2024", "01-01-2023")) // Doit retourner false

  // Test de daysBetween()
  assert(Calendar.daysBetween("01-01-2023", "01-01-2024") == 365)

  */

  // Lancer le serveur HTTP
  Server.startServer()

}

