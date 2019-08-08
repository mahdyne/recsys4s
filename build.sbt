name := "recsys4s"

version := "0.1"

scalaVersion := "2.12.8"

organization := "com.snapptrip"
libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "1.1.0" ,
  "commons-codec" % "commons-codec" % "1.6"
)