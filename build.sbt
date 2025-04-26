ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "tempomem",
    assembly / mainClass := Some("pl.belicki.tempomem.Tempomem"),
    assembly / assemblyOutputPath := new File("tempoaware.jar"),
    assembly / assemblyMergeStrategy := {
      case "application.conf" => MergeStrategy.concat
      case "reference.conf" => MergeStrategy.concat
      case PathList("META-INF", "services", _*) => MergeStrategy.filterDistinctLines
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    },
    libraryDependencies ++= List(
      "org.playframework" %% "play-ahc-ws-standalone" % "3.1.0-M6",
      "org.playframework" %% "play-ws-standalone-json" % "3.1.0-M6",
      "org.jline" % "jline" % "3.29.0",
      "org.apache.pekko" %% "pekko-stream" % "1.2.0-M1",
      "com.github.cb372" %% "scalacache-core" % "0.28.0",
      "com.github.cb372" %% "scalacache-guava" % "0.28.0",
      "ch.qos.logback" % "logback-classic" % "1.5.18",
      "org.apache.pekko" %% "pekko-slf4j" % "1.2.0-M1",
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.scalatest" %% "scalatest" % "3.3.0-SNAP4" % Test
    )
  )
