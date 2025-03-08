import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object CountActor {
  // Messages que l'actor peut recevoir
  sealed trait Command
  case object Increment extends Command
  case class GetCount(replyTo: ActorRef[Int]) extends Command

  // Comportement de l'actor
  def apply(count: Int = 0): Behavior[Command] = Behaviors.receive { (context, message) =>
    message match {
      case Increment =>
        val newCount = count + 1
        println(s"Compteur mis à jour : $newCount")
        apply(newCount) // Retourne un nouvel état avec le compteur mis à jour

      case GetCount(replyTo) =>
        replyTo ! count
        Behaviors.same // Ne change pas l'état
    }
  }
}

