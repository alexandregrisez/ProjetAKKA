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

case class Asset(val id: String,val obtentionDate: String, var invested: Double,val assetType: AssetType) {
    def getId: String = {
      id
    }

    def getObtentionDate: String = {
      obtentionDate
    }

    def getType: AssetType = {
      assetType
    }

    def getInvested: Double = {
      invested
    }

    def setInvested(newInvested: Double): Unit = {
      invested = newInvested
    }

    def getValue(date:String):Double = {
      //TODO: get value with API or in MongoDB
      0d
    }

    def getPercentageVariation(date:String):Double = {
      //TODO:Verifier si cela est bien un pourcentage utilisable
      val obtentionValue = getValue(obtentionDate)
      val currentValue = getValue(date)
      if( obtentionValue == 0 || currentValue == 0 ) 0d
      else currentValue/obtentionValue
    }

    def getAnnualPercentage(date:String):Double = {
      //TODO: Chercher un moyen de recup la valeur annuelle de l'asset en fonction de l'année d'obtention
      0d
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
  def getAssetFromDB(id: String): Future[Option[Asset]] = {
    // Pour l'exemple, on retourne un Asset fictif si l'ID est 1, sinon None
    Future.successful {
      if (id == "1") Some(Asset("1", "2024-03-10", 1500.0, AssetType.ETF))
      else None
    }
  }
}
