name := "Lab2ReactiveScala"

version := "1.0"

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.4.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.0" % "test")
