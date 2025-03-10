error id: 
file://<WORKSPACE>/src/main/scala/Asset.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
|empty definition using fallback
non-local guesses:
	 -

Document text:

```scala


// Enumération pour le type d'élément boursier
object AssetType extends Enumeration {
  type AssetType = Value
  val Share, ETF, Crypto = Value
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

}

```

#### Short summary: 

empty definition using pc, found symbol in pc: 