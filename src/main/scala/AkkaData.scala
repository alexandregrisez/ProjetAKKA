
/*
// ---------------------------------------------- Imports
import org.mongodb.scala._
import org.mongodb.scala.SingleObservable
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.bson.BsonDocument

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
            1
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

    def getUserByEmail(email: String): Future[Option[User]] = {
        val futureResult = users.find(equal("email", email)).first().toFuture()

        futureResult.map { doc =>
            if (doc != null) {
                val id = doc.get("id").map(_.asInt64().longValue()).getOrElse(0L)
                Some(User(
                    id,
                    doc.get("firstName").map(_.asString().getValue).getOrElse(""),
                    doc.get("lastName").map(_.asString().getValue).getOrElse(""),
                    doc.get("dateOfBirth").map(_.asString().getValue).getOrElse(""),
                    doc.get("email").map(_.asString().getValue).getOrElse(""),
                    doc.get("password").map(_.asString().getValue).getOrElse(""),
                    new Wallet(id, 0L, 0d, List.empty[Asset], false), // Initialisation d'un wallet fictif
                    new Wallet(id, 0L, 0d, List.empty[Asset], true)   // Initialisation d'un wallet fictif
                ))
            } 
            else {
                None
            }
        }.recover {
            case ex: Throwable =>
                println(s"Erreur lors de la récupération de l'utilisateur : ${ex.getMessage}")
                None
        }
    }

    // Inscription
    def signup(user: User): Boolean = {

        if (!user.isValid) {
            println("Utilisateur invalide.")
            return false
        }

        val userDoc = Document(
            "id" -> user.id,
            "firstName" -> user.firstName,
            "lastName" -> user.lastName,
            "dateOfBirth" -> user.dateOfBirth,
            "email" -> user.email,
            "password" -> user.password,
            "realWallet" -> Document("userID" -> user.id,"userRawMoney" -> user.realWallet.userRawMoney, "isVirtual" -> user.realWallet.isVirtual),
            "virtualWallet" -> Document("userID" -> user.id,"userRawMoney" -> user.virtualWallet.userRawMoney, "isVirtual" -> user.virtualWallet.isVirtual)
        )

        Try {
            val result = users.insertOne(userDoc).toFuture()
            Await.result(result, 10.seconds)
            true
        } 
        match {
            case Success(_) => 
                println("Utilisateur inscrit avec succès.")
                true
            case Failure(exception) => 
                println(s"Erreur lors de l'inscription : ${exception.getMessage}")
                false
        }
    }

    // Connexion 
    def signin(email: String,password:String): Option[User] = {

        val result = Try {
            val futureResult = users.find(and(
                equal("email", email),        
                equal("password", password) 
            )).first().toFuture()
            Await.result(futureResult, 10.seconds)
        }

        result match {
            case Success(doc) if doc != null =>
                val id = doc.get("id").map(_.asInt64().longValue()).getOrElse(0L)
                println("Connexion réussie.")
                Some(User(
                    id,
                    doc.get("firstName").map(_.asString().getValue).getOrElse(""),
                    doc.get("lastName").map(_.asString().getValue).getOrElse(""),
                    doc.get("dateOfBirth").map(_.asString().getValue).getOrElse(""),
                    doc.get("email").map(_.asString().getValue).getOrElse(""),
                    doc.get("password").map(_.asString().getValue).getOrElse(""),
                    doc.get("realWallet").map(_.asDocument()).map(Wallet.fromBson).getOrElse(Wallet(id,0L, 0d, List.empty[Asset], false)),
                    doc.get("virtualWallet").map(_.asDocument()).map(Wallet.fromBson).getOrElse(Wallet(id,0L, 0d, List.empty[Asset], true))
                ))
      
            case Success(_) =>
                println("Aucun utilisateur trouvé.")
                None

            case Failure(exception) =>
                println(s"Erreur lors de la connexion : ${exception.getMessage}")
                None
        }
    }

    // --------------------- Wallet Data methods ----------------------

    def fromBson(doc: BsonDocument): Wallet = {
        Wallet(
            doc.get("userID").asInt64().longValue(),
            0L,
            doc.get("userRawMoney").asDouble().doubleValue(),
            List.empty[Asset],
            doc.get("isVirtual").asBoolean().getValue
        )
    }

    // --------------------- Assets Data methods ----------------------

    // Simulation d'une fonction de récupération d'un asset depuis une base de données
    def getAssetFromDB(id: Int): Future[Option[Asset]] = {
      // Pour l'exemple, on retourne un Asset fictif si l'ID est 1, sinon None
      Future.successful {
        if (id == 1) Some(Asset(0L, 0L, "AAPL", 2.5, "2024-03-10", 1500.0, AssetType.Share))
        else None
      }
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
  */