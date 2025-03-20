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
import Wallet._
import io.circe.parser._
import io.circe.generic.auto._


case class User(val id:Long, val firstName:String, val lastName:String, val dateOfBirth:String, val email:String, val password:String, val realWallet:Long, val virtualWallet:Long){
    
    def getEmail():String = {email}

    // Vérifie la validité de l'utilisateur
    def isValid: Boolean = {
        id > 0 &&
        firstName.nonEmpty &&
        lastName.nonEmpty &&
        dateOfBirth.nonEmpty &&
        email.contains("@") &&
        password.nonEmpty &&
        realWallet > 0 &&
        virtualWallet > 0
    }
}

object UsersDB{

    implicit val ec : ExecutionContext = Global.system.dispatcher  

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

    def getUserByEmail(email: String): Future[Option[User]] = {
        val futureResult = collection.find(equal("email", email)).first().toFuture()

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
                    doc.get("realWallet").map(_.asInt64().getValue).getOrElse(0L),
                    doc.get("virtualWallet").map(_.asInt64().getValue).getOrElse(0L)
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
            "realWallet" -> user.realWallet,
            "virtualWallet" -> user.virtualWallet
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
                println("Connexion réussie.")
                Some(User(
                    id,
                    doc.get("firstName").map(_.asString().getValue).getOrElse(""),
                    doc.get("lastName").map(_.asString().getValue).getOrElse(""),
                    doc.get("dateOfBirth").map(_.asString().getValue).getOrElse(""),
                    doc.get("email").map(_.asString().getValue).getOrElse(""),
                    doc.get("password").map(_.asString().getValue).getOrElse(""),
                    doc.get("realWallet").map(_.asInt64().getValue).getOrElse(0L),
                    doc.get("virtualWallet").map(_.asInt64().getValue).getOrElse(0L)


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
      case Success(claim) => 
        //println("VerifyJWT : Valid token")
        Success(claim)
      case Failure(_) => 
        println("VerifyJWT : Invalid token")
        Failure(new Exception("Invalid token"))
    }
  }

  def extractEmailFromToken(token: String): Option[String] = {
    verifyJWT(token) match {
      case Success(claim) => 
        val claimContent = claim.content
        parse(claimContent) match {
            case Right(json) =>
                json.hcursor.get[String]("sub").toOption
            case Left(_) =>
                println("Erreur lors du parsing du JSON du claim")
                None
        }
      case Failure(_) => 
        println("No e-mail found in token")
        None
    }
  }
}

