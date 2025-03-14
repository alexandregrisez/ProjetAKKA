
// ---------------------------------------------- Imports
import org.mongodb.scala._
import org.mongodb.scala.SingleObservable
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Sorts._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.Some
import scala.None

// ---------------------------------------------- Data Management Singleton
object AkkaData extends App{
  // MongoDB Connexion
  val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
  val database: MongoDatabase = mongoClient.getDatabase("AkkaData")

  // Equivalent of a table in MySQL. (Un document peut être considéré comme un enregistrement)
  val collection: MongoCollection[Document] = database.getCollection("wallet")

  val results = collection.find(equal("userRawMoney", 500)).first().toFuture()
  val documents = Await.result(results, 10.seconds)

  println(documents.toJson())

  mongoClient.close()
}
