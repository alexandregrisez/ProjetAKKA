import org.mongodb.scala._
import org.mongodb.scala.bson.collection.mutable.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import org.mongodb.scala.model.Sorts._
import Wallet._


case class User(val id:Long, val firstName:String, val lastName:String, val dateOfBirth:String, val email:String, val password:String, val realWallet:Wallet, val virtualWallet:Wallet){
    
    def getEmail():String = {email}

    // Vérifie la validité de l'utilisateur
    def isValid: Boolean = {
        id > 0 &&
        firstName.nonEmpty &&
        lastName.nonEmpty &&
        dateOfBirth.nonEmpty &&
        email.contains("@") &&
        password.nonEmpty &&
        !realWallet.isVirtual && virtualWallet.isVirtual
    }
}

object UsersDB{

    // Connexion à MongoDB
    val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
    val database: MongoDatabase = mongoClient.getDatabase("AkkaData")
    val collection: MongoCollection[Document] = database.getCollection("users")

    def generateID(): Long = {
        val futureMaxID = collection
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
            val futureResult = collection.find(equal("email", email)).first().toFuture()
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
            val result = collection.insertOne(userDoc).toFuture()
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
            val futureResult = collection.find(and(
                equal("email", email),        
                equal("password", password) 
            )).first().toFuture()
            Await.result(futureResult, 10.seconds)
        }

        result match {
            case Success(doc) if doc != null =>
                val id = doc.get("id").map(_.asInt64().longValue()).getOrElse(0L)
                Some(User(
                    id,
                    doc.get("firstName").map(_.asString().getValue).getOrElse(""),
                    doc.get("lastName").map(_.asString().getValue).getOrElse(""),
                    doc.get("dateOfBirth").map(_.asString().getValue).getOrElse(""),
                    doc.get("email").map(_.asString().getValue).getOrElse(""),
                    doc.get("password").map(_.asString().getValue).getOrElse(""),
                    doc.get("realWallet").map(_.asDocument()).map(Wallet.fromBson).getOrElse(Wallet(id,0L,List.empty[Asset], false)),
                    doc.get("virtualWallet").map(_.asDocument()).map(Wallet.fromBson).getOrElse(Wallet(id,0L,List.empty[Asset], true))
                ))
      
            case Success(_) =>
                println("Aucun utilisateur trouvé.")
                None

            case Failure(exception) =>
                println(s"Erreur lors de la connexion : ${exception.getMessage}")
                None
        }
    }
}


object AuthService {

  val secretKey = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

  // Générer un JWT
  def generateJWT(userEmail: String): String = {
    val claim = JwtClaim(
      subject = Some(userEmail), 
      issuedAt = Some(System.currentTimeMillis() / 1000),
      expiration = Some((System.currentTimeMillis() / 1000) + 3600)  // Token valide pendant 1 heure
    )
    Jwt.encode(claim, secretKey, JwtAlgorithm.HS256) 
  }

  // Vérifier un JWT
  def verifyJWT(token: String): Try[JwtClaim] = {
    Jwt.decode(token, secretKey, Seq(JwtAlgorithm.HS256)) match {
      case Success(claim) => Success(claim)
      case Failure(_) => Failure(new Exception("Invalid token"))
    }
  }
}

