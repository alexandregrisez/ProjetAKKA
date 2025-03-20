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

class Supervisor() extends Actor{
    import context._
    var signedInUser = null


    def receive: Receive = {
        case signIn(email:String, password:String) => 
            val user = AkkaData.getUser(email, password)
            if(user != null){
                user ! userSignIn
                signedInUser = user
            }
        case signUp(firstName:String,lastName:String,dateOfBirth:String,email:String,password:String) =>
            val id = AkkaData.generateID()
            val user = AkkaData.createUser(id, firstName, lastName, dateOfBirth, email, password)
            user ! userSignIn
            signedInUser = user
        case signOut() =>
            signedInUser = null
        
        case purchaseAsset(id: Long, symbol:String, quantity: Double, invested: Double, assetType: AssetType) =>
            val user = signedInUser
            AkkaData.getRealWallet() ! purchaseAsset(id, symbol, quantity, invested, assetType)
        case sellAsset(id: Long, symbol:String, quantity: Double, invested: Double, assetType: AssetType) =>
            val user = signedInUser
            AkkaData.getRealWallet() ! sellAsset(id, symbol, quantity, invested, assetType)
    }

    object SupervisorActor {
        case class UserActor(userId: Long, val firstName:String, val lastName:String, val dateOfBirth:String, val email:String, val password:String, val realWallet:Wallet, val virtualWallet:Wallet)
        //Gérés parr User ? case class WalletActor(val userId : Long, var userRawMoney : Double, var assets: List[Long], var isVirtual:Boolean)
        //Gérés par Wallet ? case class AssetActor(id: Long, symbol:String, var quantity: Double, obtentionDate: String, var invested: Double, assetType: AssetType, isVirtual:Boolean)

        def props: Props = Props(new SupervisorActor)
    }
}