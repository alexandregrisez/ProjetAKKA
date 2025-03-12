case class User(val id:Long, val firstName:String, val lastName:String, val dateOfBirth:String, val email:String, val realWallet:Wallet, val virtualWallet:Wallet){
    def isValid():Boolean = {
        //TODO: rajouter des verif comme pour des champs manquants ou un id incorrect
        !realWallet.isVirtual && virtualWallet.isVirtual
    }
}
