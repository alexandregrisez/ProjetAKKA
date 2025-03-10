// ------------------------------------------------- Imports

import scala.concurrent.Future

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

// ------------------------------------------------- Asset Type Enumeration

object AssetType extends Enumeration {
  type AssetType = Value
  val Share, ETF, Crypto = Value

  implicit val assetTypeDecoder: Decoder[AssetType.Value] = Decoder.decodeEnumeration(AssetType)
  implicit val assetTypeEncoder: Encoder[AssetType.Value] = Encoder.encodeEnumeration(AssetType)
}
import AssetType._

// ------------------------------------------------- Asset Class

case class Asset(id: Int, obtentionDate: String, var invested: Double, assetType: AssetType) {
  
  def getId: Int = {
    id
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

  def setInvested(newInvested: Double): Unit = {
    invested = newInvested
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
      if (id == 1) Some(Asset(1, "2024-03-10", 1500.0, AssetType.ETF))
      else None
    }
  }
}
