// The giter8 template for a witdraw starter project. The template lives in src/main/g8.
// `sbt test` generates a project from it and runs that project's tests (see src/test/g8/test).
lazy val root = (project in file("."))
  .settings(
    name := "witdraw-starter.g8",
    Test / test := {
      val _ = (Test / g8Test).toTask("").value
    },
    scriptedLaunchOpts ++= List("-Xms1024m", "-Xmx1024m", "-XX:ReservedCodeCacheSize=128m", "-Xss2m", "-Dfile.encoding=UTF-8")
  )
