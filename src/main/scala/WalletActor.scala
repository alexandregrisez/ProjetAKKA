import org.mongodb.scala._
import org.mongodb.scala.bson.collection.mutable.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.concurrent.{Future,ExecutionContext}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import org.mongodb.scala.model.Sorts._
import AssetType._
import io.circe.parser._
import io.circe.generic.auto._
import Akka.actor.{Actor, ActorRef, Props, ActorSystem,PoisonPill}

class WalletActor(val userId : Long, var userRawMoney : Double, var assets: List[Long], var isVirtual:Boolean) extends Actor {
    import context._


    def receive: Receive = {
        case purchaseAsset(id: Long, symbol:String, quantity: Double, invested: Double, assetType: AssetType) => 
            if((userRawMoney - invested) > 0) {
                val currentDateTime: LocalDateTime = LocalDateTime.now()
                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val formattedDate: String = currentDateTime.format(formatter)
                AkkaData.createAsset(id, symbol, quantity, formattedDate, invested, assetType)
                userRawMoney = userRawMoney - invested
            }
        case sellAsset(id: Long, symbol:String, quantity: Double, invested: Double, assetType: AssetType) =>
            val asset = AkkaData.getAssetFromDB(id)
            if(quantity > asset.quantity){
                val currentDateTime: LocalDateTime = LocalDateTime.now()
                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val formattedDate: String = currentDateTime.format(formatter)
                userRawMoney = userRawMoney + AkkaData.getAssetPrice(formattedDate) * quantity
                asset.quantity = asset.quantity - quantity
            }

    }
}

object WalletActor {
    case class AssetActor(id: Long, symbol: String, var quantity: Double, obtentionDate: String, var invested: Double, assetType: AssetType, isVirtual: Boolean)
}