// ------------------------------------------------- Imports

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask

import scala.concurrent.{Await, Future, ExecutionContext}

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

import scala.util.Random

import akka.util.Timeout
import scala.concurrent.duration._

// ------------------------------------------------- Asset Type Enumeration

object AssetType extends Enumeration {
  type AssetType = Value
  val Share, ETF, Crypto = Value

  // Les encodeurs et décodeurs sont utilisés par Circe pour facilement faire des transformations au format JSON
  implicit val assetTypeDecoder: Decoder[AssetType.Value] = Decoder.decodeEnumeration(AssetType)
  implicit val assetTypeEncoder: Encoder[AssetType.Value] = Encoder.encodeEnumeration(AssetType)
}
import AssetType._

// ------------------------------------------------- Asset Class

case class Asset(id: Long, symbol:String, var quantity: Double, obtentionDate: String, var invested: Double, assetType: AssetType) {

  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val ec : ExecutionContext = Global.system.dispatcher
  
  def getId: Long = {
    id
  }

  def getSymbol: String = {
    symbol
  }

  def getQuantity: Double = {
    quantity
  }

  def getObtentionDate: String = {
    obtentionDate
  }

  def getAssetType: AssetType = {
    assetType
  }

  def getInvested: Double = {
    invested
  }

  def setQuantity(newQuantity: Double): Unit = {
    quantity = newQuantity
  }

  def setInvested(newInvested: Double): Unit = {
    invested = newInvested
  }

  def getValue(date: String): Future[Double] = {
    // Cas où on ne simule pas la valeur
    if (assetType == AssetType.Share && date == Calendar.now()) {
      val futurePrice: Future[Double] = (Global.finnhub ? FinnhubActor.GetStockPriceAlt(symbol))
      .mapTo[Double]
      .recover {
        case ex =>
          println(s"Erreur lors de l'appel API: ${ex.getMessage}")
          -1.0
      }
      futurePrice 
    } 
    // Simulation
    else {
      // Variance entre 80% et 120% de l'investissement
      Future.successful(invested * (0.8 + Random.nextDouble() * 0.4))
    }
  }

  def getPercentageVariation(date:String): Double = {
    val today=Calendar.now()

    if (!Calendar.chronological(date,today)) {
      println("The given date must be before today.")
    }

    val todayValueFuture=getValue(today)
    val dateValueFuture=getValue(date)

    val todayValue = Await.result(todayValueFuture, 5.seconds)
    val dateValue = Await.result(dateValueFuture, 5.seconds)

    ((todayValue-dateValue)/dateValue)*100
  }

  def getAnnualPercentage(date: String): Double = {
        val today = Calendar.now()

        if (!Calendar.chronological(date,today)) {
          println("The given date must be before today.")
        }

        val days = Calendar.daysBetween(date,today)
        
        val variation = getPercentageVariation(date) / 100

        Math.pow(1 + variation, 365 / days) - 1
    }

  // Les encodeurs et décodeurs sont utilisés par Circe pour facilement faire des transformations au format JSON
  implicit val assetDecoder: Decoder[Asset] = deriveDecoder
  implicit val assetEncoder: Encoder[Asset] = deriveEncoder

  // Convert to JSON String
  def toJson: String = {
    this.asJson.noSpaces
  }
}

object DB{

  // Simulation d'une fonction de récupération d'un asset depuis une base de données
  def getAssetFromDB(id: Int): Future[Option[Asset]] = {
    // Pour l'exemple, on retourne un Asset fictif si l'ID est 1, sinon None
    Future.successful {
      if (id == 1) Some(Asset(-1, "AAPL", 2.5, "2024-03-10", 1500.0, AssetType.Share))
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
