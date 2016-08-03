import AssemblyKeys._

organization := "at.ac.csf-ngs"

assemblySettings

name := "bam2fq"

scalaVersion := "2.10.6"

version := "0.7-SNAPSHOT"

jarName in assembly := "bam2fq.jar"

//mainClass in assembly := Some("at.ac.imp.genau.jnomicss.cli.doApp")

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case "logback.xml"     => MergeStrategy.first
    case x => old(x)
  }
}

excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  val toFilter = Set("specs2","pegdown", "scalacheck","scalatest","junit","mockito")
  cp.filter{ j =>
     val name = j.data.getName.toLowerCase
     toFilter.filter(f => name.contains(f)).size > 0
  }
}


libraryDependencies ++= Seq(
    "org.specs2" %% "specs2-core" % "3.6.4" % "test",
    "org.specs2" %% "specs2-mock" % "3.6.4" % "test",
    "org.specs2" %% "specs2-html" % "3.6.4" % "test",
    "org.mockito" % "mockito-all" % "1.9.0" % "test",
    "junit" % "junit" % "4.11" % "test",
    "org.pegdown" % "pegdown" % "1.2.0" % "test",
    "org.scalacheck" %% "scalacheck" % "1.10.1" % "test"
)

libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.6.2",   
    "ch.qos.logback" % "logback-core" % "1.0.0",
    "ch.qos.logback" % "logback-classic" % "1.0.0"
)

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

testOptions in Test += Tests.Argument("html", "console")

libraryDependencies ++= Seq( 
     "com.github.broadinstitute" % "picard" % "2.5.0",
     "com.github.samtools" % "htsjdk" % "2.5.1"
)
