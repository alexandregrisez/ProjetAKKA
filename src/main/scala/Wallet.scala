import AssetType._

case class Wallet(val userId:Long,var userRawMoney:Double,var assets:List[Asset],val isVirtual:Boolean){
    def getValue(date:String):Double = {
        var total:Double = 0d;
        assets.foreach( asset => total += asset.getValue(date))
        total
    }

    def getValueByType(date:String, atype:AssetType):Double = {
        var total:Double = 0d;
        assets.foreach( asset => if(asset.assetType == atype) total += asset.getValue(date))
        total
    }

    def purchaseAsset(assetId:String, priceInvested:Double): Unit = {
        //TODO: ajouter une manière de récupérer un asset via BDD pour l'ajouter à notre liste
        if(isVirtual) println("Rien n'est consommé!")
        else println("Vous avez acheté " + assetId + ", il vous reste " + (userRawMoney - priceInvested) + "€")
    }

    def sellAsset(assetId:String):Unit = {
        //TODO: Recupere l'asset si existant, l'enlever de la liste, recuperer sa valeur et l'ajouter au rawUserMoney
    }
}
