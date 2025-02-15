import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}
import akka.actor.typed.Scheduler // Ajout de l'import

object Main extends App {
  // Création du système d'acteurs
  val system: ActorSystem[CountActor.Command] = ActorSystem(CountActor(), "CountActorSystem")

  // Envoi de messages à l'actor
  system ! CountActor.Increment
  system ! CountActor.Increment
  system ! CountActor.Increment

  // Récupération du compteur avec un scheduler implicite
  implicit val timeout: Timeout = 3.seconds
  implicit val ec: ExecutionContextExecutor = system.executionContext
  implicit val scheduler: Scheduler = system.scheduler // ✅ Ajout du scheduler

  val countFuture = system.ask(ref => CountActor.GetCount(ref))
  val countValue = Await.result(countFuture, timeout.duration)

  println(s"Valeur finale du compteur : $countValue")

  // Arrêter le système proprement après exécution
  system.terminate()
}

