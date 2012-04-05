import AssemblyKeys._

organization := "at.ac.csf-ngs"

assemblySettings


name := "bam2fq"

version := "0.5-SNAPSHOT"

jarName in assembly := "bam2fq.jar"

libraryDependencies ++= Seq(
    "org.specs2" %% "specs2" % "1.7.1" % "test",
    "org.specs2" %% "specs2-scalaz-core" % "6.0.1" % "test",
    "org.mockito" % "mockito-all" % "1.8.5" % "test",
    "junit" % "junit" % "4.8" % "test",
    "org.pegdown" % "pegdown" % "1.0.2" % "test"
)

libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.6.2",   
    "ch.qos.logback" % "logback-core" % "1.0.0",
    "ch.qos.logback" % "logback-classic" % "1.0.0"
)

libraryDependencies += "com.weiglewilczek.slf4s" %% "slf4s" % "1.0.7"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

testOptions in Test += Tests.Argument("html", "console")

libraryDependencies ++= Seq( 
     "net.sf.picard" % "sam" % "1.65",
     "net.sf.picard" % "picard" % "1.65"
)
