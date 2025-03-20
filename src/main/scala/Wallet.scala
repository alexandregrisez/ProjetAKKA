import org.mongodb.scala._
import org.mongodb.scala.bson.collection.mutable.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import scala.concurrent.{Future,ExecutionContext}
import scala.util.{Failure, Success, Try}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Sorts._
import AssetType._
import scala.concurrent.Await
import scala.concurrent.duration._

case class Wallet(val userId:Long,var userRawMoney:Double,var assets:List[Long],val isVirtual:Boolean){

    implicit val ec : ExecutionContext = Global.system.dispatcher    
  
}

object Wallet{

    implicit val ec : ExecutionContext = Global.system.dispatcher  

    // Connexion à MongoDB
    val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
    val database: MongoDatabase = mongoClient.getDatabase("AkkaData")
    val wallets: MongoCollection[Document] = database.getCollection("wallets")
    val assets: MongoCollection[Document] = database.getCollection("assets")

    def fromBson(doc: BsonDocument): Wallet = {
        Wallet(
            doc.get("userID").asInt64().longValue(),
            doc.get("money").asDouble().doubleValue(),
            List.empty[Long],
            doc.get("isVirtual").asBoolean().getValue
        )
    }


    def addWallet(walletId:Long, money: Double, assets: List[String], virtual: Boolean): Boolean = {

        Try {

            // Création du document wallet
            val walletDoc = Document(
                "id" -> walletId,
                "money" -> money,
                "isVirtual" -> virtual
            )

            val insertResult = wallets.insertOne(walletDoc).toFuture()
            Await.result(insertResult, 10.seconds)

            println(s"Nouveau wallet : $walletId (isVirtual=$virtual) avec un solde de $money.")
            true
            
        } 
        match {
            case Success(result) => result
            case Failure(exception) =>
                println(s"Erreur lors de l'ajout du wallet : ${exception.getMessage}")
                false
        }
    }

    def generateAssetID(): Long = {
        val futureMaxID = assets
            .find()
            .sort(descending("id")) 
            .limit(1)
            .first()
            .toFuture()

        val maxAsset = Await.result(futureMaxID, 5.seconds)

        if (maxAsset != null && !maxAsset.isEmpty) {
            maxAsset.get("id").map(_.asInt64().longValue()).getOrElse(1L)
        } 
        else {
            1
        }
    }

    def generateWalletID(): Long = {
        val futureMaxID = wallets
            .find()
            .sort(descending("id")) 
            .limit(1)
            .first()
            .toFuture()

        val maxWallet = Await.result(futureMaxID, 5.seconds)

        if (maxWallet != null && !maxWallet.isEmpty) {
            maxWallet.get("id").map(_.asInt64().longValue()).getOrElse(1L) + 1
        } 
        else {
            1
        }
    }


    def addAsset(id: Long, walletId: Long, category:String, symbol: String, quantity: Int): Boolean = {
        val existingAssetFuture = assets.find(
            and(equal("walletId", walletId), equal("symbol", symbol))
        ).first().toFuture()

        Try {
            val existingAsset = Await.result(existingAssetFuture, 10.seconds)

            if (existingAsset != null) {
                // L'asset existe déjà : mise à jour de la quantité
                val updatedQuantity = existingAsset.get("quantity").map(_.asInt32().getValue).getOrElse(0) + quantity


                val updateResult = assets.updateOne(
                    and(equal("walletId", walletId), equal("symbol", symbol)),
                    set("quantity", updatedQuantity)
                ).toFuture()

                Await.result(updateResult, 10.seconds)
                println(s"Quantité mise à jour : $updatedQuantity pour $symbol")
            } 
            else {
                // L'asset n'existe pas encore : insertion
                val assetDoc = Document(
                    "id" -> id,
                    "walletId" -> walletId,
                    "type" -> category,
                    "symbol" -> symbol,
                    "quantity" -> quantity
                )

                val insertResult = assets.insertOne(assetDoc).toFuture()
                Await.result(insertResult, 10.seconds)
                println(s"Nouvel asset ajouté : $symbol, quantité $quantity (id $id)")
            }

            true
        } 
        match {
            case Success(_) => true
            case Failure(exception) =>
                println(s"Erreur lors de l'ajout ou mise à jour de l'asset : ${exception.getMessage}")
                false
        }
    }

    def spendMoney(walletId:Long,price:Double):Boolean={
        Try {
            // Récupérer le wallet pour vérifier le solde
            val walletFuture = wallets.find(equal("id", walletId)).first().toFuture()
            val wallet = Await.result(walletFuture, 10.seconds)

            if (wallet == null) {
                println(s"Aucun wallet trouvé avec l'ID $walletId")
                false
            } 
            else {
                val currentMoney = wallet.get("money").map(_.asDouble().getValue).getOrElse(0.0)


                if (currentMoney < price) {
                    println(s"Fonds insuffisants dans le wallet $walletId : solde actuel = $currentMoney, requis = $price")
                    false
                } 
                else {
                    val newBalance = currentMoney - price

                    // Mise à jour du solde dans MongoDB
                    val updateResult = wallets.updateOne(
                        equal("id", walletId),
                        set("money", newBalance)
                    ).toFuture()

                    Await.result(updateResult, 10.seconds)

                    println(s"Transaction réussie : nouveau solde du wallet $walletId = $newBalance")
                    true
                }
            }
        } 
        match {
            case Success(result) => result
            case Failure(exception) =>
                println(s"Erreur lors de la mise à jour du wallet : ${exception.getMessage}")
                false
            }
    }

    def findRealWalletID(email: String): Long = {
        val futureWalletId = UsersDB.getUserByEmail(email).map {
            case Some(user) => user.realWallet
            case None => -1L // Retourne une valeur par défaut si l'utilisateur n'est pas trouvé
        }

        Await.result(futureWalletId, 10.seconds) 
    }


    def purchase(email:String, category:String, symbol: String, quantity: Int,price:Double): Int = {
        // Utilisé au cas où l'utilisateur n'a pas encore d'asset avec ce symbole
        val possibleAssetID=generateAssetID()

        val walletId=findRealWalletID(email)

        if(walletId<=0){
            println("Le wallet associé à l'utilisateur n'a pas pu être trouvé")
            false
        }

        if(!spendMoney(walletId,price)){
            println("L'utilisateur n'a pas assez d'argent.")
            -1
        }
        else{
            if(addAsset(possibleAssetID,walletId,category,symbol,quantity)){
                0
            }
            else{
                spendMoney(walletId,-price)
                -2
            }
            
        }
    }
}


