import akka.actor.{Actor, ActorRef, ActorSystem, Props, PoisonPill}

object Global {
  // Définition du système d'acteurs et des acteurs participants
  val system: ActorSystem = ActorSystem("GlobalSystem")
  val finnhub: ActorRef = Global.system.actorOf(Props[FinnhubActor], "FinnhubActor") 
  //val finnhub: ActorRef = system.actorOf(Props[FinnhubActor], "FinnhubActor")
}
