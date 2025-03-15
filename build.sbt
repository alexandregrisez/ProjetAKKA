import Dependencies._

ThisBuild / scalaVersion     := "2.13.15"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

val AkkaVersion = "2.10.1"
val AkkaHttpVersion = "10.7.0"
val circeVersion = "0.14.6"
val circeExtrasVersion = "0.14.3"

libraryDependencies ++= Seq(
    // Akka
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion,
    "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
    "com.typesafe.akka" %% "akka-distributed-data" % AkkaVersion,
    "com.typesafe.akka" %% "akka-multi-node-testkit" % AkkaVersion % Test,
    "com.typesafe.akka" %% "akka-persistence" % AkkaVersion,
    "com.typesafe.akka" %% "akka-persistence-tck" % AkkaVersion,
    "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-persistence-query" % AkkaVersion,
    "com.typesafe.akka" %% "akka-protobuf-v3" % AkkaVersion,
    "com.typesafe.akka" %% "akka-remote" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
    "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-coordination" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-metrics" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
    // Circe (JSON)
    "io.circe" %% "circe-core" % circeVersion, 
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion, 
    "io.circe" %% "circe-generic-extras" % circeExtrasVersion,
    //MongoDB
    "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",
    // Tokens web
    "com.github.jwt-scala" %% "jwt-core" % "9.0.1"

)

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.14"

libraryDependencies += "ch.megard" %% "akka-http-cors" % "1.1.2"

logLevel := Level.Warn  // Ajuste cela à Level.Info ou Level.Error en fonction de tes besoins


lazy val root = (project in file("."))
  .settings(
    name := "akka",
    libraryDependencies += munit % Test
  )



