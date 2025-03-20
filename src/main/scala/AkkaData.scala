
// ---------------------------------------------- Imports
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Sorts._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.{Future,ExecutionContext}

import scala.{Some,None}
import scala.util.{Failure, Success, Try}
import scala.util.Random

// ---------------------------------------------- Data Management Singleton
object AkkaData {

  implicit val ec : ExecutionContext = Global.system.dispatcher  

  val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
  val database: MongoDatabase = mongoClient.getDatabase("AkkaData")

  val assets: MongoCollection[Document] = database.getCollection("assets")
  val users: MongoCollection[Document] = database.getCollection("users")
  val wallets: MongoCollection[Document] = database.getCollection("wallets")

  def AddingExampleData() = {
    assets.insertMany(DataExample.getAssets())
  }

  def Close() = {
    mongoClient.close()
  }

  // --------------------- User Data methods ----------------------

  def createUser(id:Long, firstName:String, lastName:String, dateOfBirth:String, email:String, password:String): Option[User] = {
  if(emailExists(email)) {
    None
  } else {
    val newUser = Document("id" -> id, "firstName" -> firstName, "lastName" -> lastName, "dateOfBirth" -> dateOfBirth, "email" -> email, "password" -> password)

    try {
      Await.result(users.insertOne(newUser).toFuture(), 5.seconds)
      println(s"Nouvel utilisateur $id créé avec succès!")

      createUserRealWallet(id, 10000.0)
      createUserVirtualWallet(id, 0)

      Some(User(id, firstName, lastName, dateOfBirth, email, password))
    } catch {
      case e: Exception =>
        println(s"Erreur lors de la création de l'utilisateur : ${e.getMessage}")
        None
    }
  }
}

  def generateID(): Long = {
    val futureMaxID = users
        .find()
        .sort(descending("id")) 
        .limit(1)
        .first()
        .toFuture()

    val maxUser = Await.result(futureMaxID, 5.seconds)

    if (maxUser != null && !maxUser.isEmpty) {
      maxUser.get("id").map(_.asInt64().longValue()).getOrElse(1L)
    } 
    else {
      1L
    }
  }

  def emailExists(email:String):Boolean={
      val result = Try {
          val futureResult = users.find(equal("email", email)).first().toFuture()
          Await.result(futureResult, 10.seconds)
      }
      result match {
          case Success(doc) if doc != null => true      
          case Success(_) => false
          case Failure(exception) =>
              println(s"Erreur lors de l'inscription (emailExists) : ${exception.getMessage}")
              false
      }
  }

  // Connexion 
  def getUser(email: String,password:String): Option[User] = {
    val filter = and(equal("email",email), equal("password",password))
    val userFuture : Future[Document] = users.find(filter).first().toFuture()

    val userDoc = Await.result(userFuture, 5.seconds)

    if(userDoc != null){
      val user:User = User(
        userDoc.get("id").map(_.asInt64().longValue()).getOrElse(0L),
        userDoc.get("firstName").map(_.asString().getValue()).getOrElse("Null"),
        userDoc.get("lastName").map(_.asString().getValue()).getOrElse("Null"),
        userDoc.get("dateOfBirth").map(_.asString().getValue()).getOrElse("Null"),
        userDoc.get("email").map(_.asString().getValue()).getOrElse("Null"),
        userDoc.get("password").map(_.asString().getValue()).getOrElse("Null"),
      )
      Some(user)
    }
    else{
      None
    }
  }

  def getUserByEmail(email:String): Future[Option[User]] = {
    val filter = equal("email",email)
    val userFuture : Future[Document] = users.find(filter).first().toFuture()

    val userDoc = Await.result(userFuture, 5.seconds)

    if(userDoc != null){
      val user:User = User(
        userDoc.get("id").map(_.asInt64().longValue()).getOrElse(0L),
        userDoc.get("firstName").map(_.asString().getValue()).getOrElse("Null"),
        userDoc.get("lastName").map(_.asString().getValue()).getOrElse("Null"),
        userDoc.get("dateOfBirth").map(_.asString().getValue()).getOrElse("Null"),
        email,
        userDoc.get("password").map(_.asString().getValue()).getOrElse("Null"),
      )
      Some(user)
    }
    else{
      None
    }
  }

    // --------------------- Wallet Data methods ----------------------

    def createUserRealWallet(userId:Long, userRawMoney:Double, assets:List[Long] = List.empty[Long]) = {
      val newWallet = Document("userId" -> userId, "userRawMoney" -> userRawMoney, "assets" -> assets, "isVirtual" -> true)

      wallets.insertOne(newWallet).subscribe(
        (_: Completed) => println(s"Nouveau porte-monnaie réel de user$userId crée!"),
        (e: Throwable) => println(s"Erreur lors de la création du porte-monnaie réel: ${e.getMessage}"),
        () => println("Défault : Création de l'utilisateur avec succès!")
      )

      // Attendre la fin de l'opération
      Await.ready(wallets.insertOne(newWallet).toFuture(), 5.seconds)
    }

    def createUserVirtualWallet(userId:Long, userRawMoney:Double, assets:List[Long] = List.empty[Long]) = {
      val newWallet = Document("userId" -> userId, "userRawMoney" -> userRawMoney, "assets" -> assets, "isVirtual" -> false)

      wallets.insertOne(newWallet).subscribe(
        (_: Completed) => println(s"Nouveau porte-monnaie virtuel de user$userId crée!"),
        (e: Throwable) => println(s"Erreur lors de la création du porte-monnaie virtuel: ${e.getMessage}"),
        () => println("Défault : Création de l'utilisateur avec succès!")
      )

      // Attendre la fin de l'opération
      Await.ready(wallets.insertOne(newWallet).toFuture(), 5.seconds)
    }

    def getRealWallet(userId:Long) : Option[Wallet] = {
      val filter = and(equal("userId",userId), equal("isVirtual",false))

      val docWallet : Future[Document] = wallets.find(filter).first().toFuture() 
      val wallet = Await.result(docWallet, 5.seconds)

      if(wallet != null){
        val realWallet:Wallet = Wallet(
            wallet.get("userId").map(_.asInt64().longValue()).getOrElse(0L),
            wallet.get("userRawMoney").map(_.asDouble().doubleValue()).getOrElse(0.0),
            wallet.get("assets").toList.map(_.asInt64().longValue()),
            wallet.get("isVirtual").map(_.asBoolean().getValue()).getOrElse(false)
          )
        Some(realWallet)
      }
      None    
    }

    def getVirtualWallet(userId:Long) : Option[Wallet] = {
      val filter = and(equal("userId",userId), equal("isVirtual",true))

      val docWallet : Future[Document] = wallets.find(filter).first().toFuture() 
      val wallet = Await.result(docWallet, 5.seconds)

      if(wallet != null){
        val virtualWallet:Wallet = Wallet(
            wallet.get("userId").map(_.asInt64().longValue()).getOrElse(0L),
            wallet.get("userRawMoney").map(_.asDouble().doubleValue()).getOrElse(0.0),
            wallet.get("assets").toList.map(_.asInt64().longValue()),
            wallet.get("isVirtual").map(_.asBoolean().getValue()).getOrElse(true)
          )
        Some(virtualWallet)
      }
      None    
    }

    def purchaseAsset(assetId:Long,email:String,category:AssetType,symbol:String,quantity:Double,price:Double):Option[Asset] = {
      val currentAsset:Option[Asset] = getAsset(assetId)
      if(currentAsset.isDefined){
        var updater = set("quantity",quantity + currentAsset.get.quantity)

        assets.updateOne(equal("id",assetId),updater)
        // Attendre la fin de l'opération
        Await.ready(assets.updateOne(equal("id",assetId), updater).toFuture(), 5.seconds)
      }
      else{
        None
      }
    }

    // --------------------- Assets Data methods ----------------------



    def createAsset(id: Long, symbol:String, quantity: Double, obtentionDate: String, invested: Double, assetType: AssetType, isVirtual:Boolean) = {
      val newAsset = Document("id" -> id, "symbol" -> symbol, "quantity" -> quantity, "obtentionDate" -> obtentionDate, 
      "invested" -> invested, "assetType" -> assetType, "isVirtual" -> isVirtual)

      assets.insertOne(newAsset).subscribe(
        (_: Completed) => println(s"Nouveau porte-monnaie réel de user$id crée!"),
        (e: Throwable) => println(s"Erreur lors de la création du porte-monnaie réel: ${e.getMessage}"),
        () => println("Défault : Création de l'utilisateur avec succès!")
      )
      // Attendre la fin de l'opération
      Await.ready(assets.insertOne(newAsset).toFuture(), 5.seconds)
    }
    // Simulation d'une fonction de récupération d'un asset depuis une base de données
    def getAsset(id: Long): Option[Asset] = {
      val filter = equal("id",id)

      val futureAsset : Future[Document] = assets.find(filter).first().toFuture() 
      val docAsset = Await.result(futureAsset, 5.seconds)

      if(docAsset != null){
        val asset:Asset = Asset(
            docAsset.get("id").map(_.asInt64().longValue()).getOrElse(0L),
            docAsset.get("symbol").map(_.asString().getValue()).getOrElse("NULL"),
            docAsset.get("quantity").toList.map(_.asDouble().doubleValue()),
            docAsset.get("obtentionDate").map(_.asString().getValue()).getOrElse("01/01/75"),
            docAsset.get("invested").toList.map(_.asDouble().doubleValue()),
            AssetType.Share,
            docAsset.get("isVirtual").map(_.asBoolean().getValue()).getOrElse(false)
          )
        Some(asset)
      }
      None  
    }

    def getAssetPrice(date: String): Future[Option[Double]] = {
      // Simulation d'un appel API pour récupérer le prix de l'asset
      Future.successful {
        // Prix aléatoire entre 100 et 150
        Some(100.0 + Random.nextDouble() * 50) 
      }
    }
}

// An example dataset for debugging and testing features
object DataExample {

  val assets = List(
    Document(
      "id" -> 1,
      "walletId" -> 1,
      "symbol" -> "GOOG",
      "quantity" -> 3,
      "obtentionDate" -> "12/12/2012",
      "invested" -> 30,
      "assetType" -> "Share"
    ),
    Document(
      "id" -> 2,
      "walletId" -> 2,
      "symbol" -> "APPL",
      "quantity" -> 1,
      "obtentionDate" -> "03/06/1985",
      "invested" -> 50,
      "assetType" -> "Share"
    ),
    Document(
      "id" -> 3,
      "walletId" -> 3,
      "symbol" -> "APPL",
      "quantity" -> 5,
      "obtentionDate" -> "31/10/1999",
      "invested" -> 100,
      "assetType" -> "Share"
    )
  )

  val users = List()
  val wallets = List()

  def getAssets(): List[Document] = {
    assets
  }

  def getUsers(): List[Document] = {
    users
  }

  def getWallets(): List[Document] = {
    wallets
  }
}