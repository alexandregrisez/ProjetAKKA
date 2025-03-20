import org.mongodb.scala._
import org.mongodb.scala.bson.collection.mutable.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import scala.concurrent.{Future,ExecutionContext}
import scala.util.{Failure, Success, Try}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Sorts._
import scala.concurrent.Await
import scala.concurrent.duration._
import Akka.actor.{Actor, ActorRef, Props, ActorSystem,PoisonPill}

class Asset(id: Long, symbol:String, var quantity: Double, obtentionDate: String, var invested: Double, assetType: AssetType, isVirtual:Boolean) extends Actor {
    import context._

    def receive: Receive = {
        case 
    }
}

