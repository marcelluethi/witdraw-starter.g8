ThisBuild / scalaVersion := "3.9.0"
ThisBuild / resolvers += "Central Portal Snapshots" at "https://central.sonatype.com/repository/maven-snapshots/"

lazy val root = (project in file("."))
  .settings(
    name := "$name;format="norm"$",
    libraryDependencies ++= Seq(
      "ch.contrafactus" %% "dimwit-core" % "0.3-SNAPSHOT",
      "ch.contrafactus" %% "plotwit-core" % "0.3-SNAPSHOT",
      "ch.contrafactus" %% "witdraw-core" % "0.1-SNAPSHOT",
      "ch.contrafactus" %% "witdraw-plots" % "0.1-SNAPSHOT",
      "io.github.quafadas" %% "scautable" % "0.0.45",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
    // dimwit.initialize() sets up Python with `uv sync`, in the directory that holds pyproject.toml
    fork := true
  )
