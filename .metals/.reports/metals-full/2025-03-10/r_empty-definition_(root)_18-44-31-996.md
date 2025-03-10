error id: io/circe/generic/extras/
file://<WORKSPACE>/src/main/scala/Asset.scala
empty definition using pc, found symbol in pc: io/circe/generic/extras/
semanticdb not found
|empty definition using fallback
non-local guesses:
	 -

Document text:

```scala
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.generic.extras.{enumEncoder, enumDecoder}

// Enumération pour le type d'élément boursier
object AssetType extends Enumeration {
  type AssetType = Value
  val Share, ETF, Crypto = Value

  implicit val assetTypeDecoder: Decoder[AssetType.Value] = Decoder.enumDecoder(AssetType)
  implicit val assetTypeEncoder: Encoder[AssetType.Value] = Encoder.enumEncoder(AssetType)
}
import AssetType._

// Classe Asset
case class Asset(id: Int, obtentionDate: String, var invested: Double, assetType: AssetType) {
  
  def getId: Int = {
    id
  }

  def getObtentionDate: String = {
    obtentionDate
  }

  def getInvested: Double = {
    invested
  }

  def getAssetType: AssetType = {
    assetType
  }

  def setInvested(newInvested: Double): Unit = {
    invested = newInvested
  }

  
  implicit val assetDecoder: Decoder[Asset] = deriveDecoder
  implicit val assetEncoder: Encoder[Asset] = deriveEncoder

  def toJson: String = {
    this.asJson.noSpaces
  }

}

```

#### Short summary: 

empty definition using pc, found symbol in pc: io/circe/generic/extras/