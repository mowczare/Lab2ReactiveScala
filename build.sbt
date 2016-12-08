name := "Lab2ReactiveScala"

version := "1.4"

scalaVersion := "2.12.0"

parallelExecution := false

lazy val akkaVersion = "2.4.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.3.14"
)
