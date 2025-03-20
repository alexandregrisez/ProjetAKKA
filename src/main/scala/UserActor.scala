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
import Akka.actor.{Actor, ActorRef, Props, ActorSystem,PoisonPill}

class UserActor(val id:Long, val firstName:String, val lastName:String, val dateOfBirth:String, val email:String, val password:String, val realWallet:Wallet, val virtualWallet:Wallet) extends Actor{
    import context._

    def receive: Receive = {
        case userSignIn(user: User) => AkkaData.getUser(user.email, user.password)

        case addRealWallet(userId: Long, userRawMoney: Double) => AkkaData.createUserRealWallet(userId, userRawMoney)
            
        case addVirtualWallet(userId: Long, userRawMoney: Double) => AkkaData.createVirtualRealWallet(userId, userRawMoney)

        case getRealWallet(userId: Long) => AkkaData.getRealWallet(userId)

        case getVirtualWallet(userId: Long) => AkkaData.getVirtualWallet(userId)

    }
}


/*
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
}*/


