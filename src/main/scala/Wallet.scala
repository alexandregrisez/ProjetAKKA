import scala.concurrent.{Future,ExecutionContext}
import org.mongodb.scala.bson.BsonDocument
import AssetType._

case class Wallet(val userId:Long,var userRawMoney:Double,var assets:List[Asset],val isVirtual:Boolean){

    implicit val ec : ExecutionContext = Global.system.dispatcher    
    
    def getValue(date: String): Future[Double] = {
        val futures: List[Future[Double]] = assets.map(_.getValue(date))
        Future.sequence(futures).map(_.sum)
    }

    def getValueByType(date: String, atype: AssetType): Future[Double] = {
        val futures: List[Future[Double]] = assets.filter(_.assetType == atype).map(_.getValue(date))
        Future.sequence(futures).map(_.sum)
    }

    def purchaseAsset(assetId:String, priceInvested:Double): Unit = {
        //TODO: ajouter une manière de récupérer un asset via BDD pour l'ajouter à notre liste
        if(isVirtual) println("Rien n'est consommé!")
        else println("Vous avez acheté " + assetId + ", il vous reste " + (userRawMoney - priceInvested) + "€")
    }

    def sellAsset(assetId:String):Unit = {
        //TODO: Recupere l'asset si existant, l'enlever de la liste, recuperer sa valeur et l'ajouter au rawUserMoney
    }

}

object Wallet{
    def fromBson(doc: BsonDocument): Wallet = {
        Wallet(
            doc.get("userID").asInt64().longValue(),
            doc.get("userRawMoney").asDouble().doubleValue(),
            List.empty[Asset],
            doc.get("isVirtual").asBoolean().getValue
        )
    }
}
