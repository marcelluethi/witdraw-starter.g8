#!/usr/bin/env bash
# Starts a Scala 3 REPL with this project's classpath.
#
# `sbt console` fails with Scala 3.9, because its REPL needs JLine 4 while sbt 1.x puts its own JLine 3 on the
# console classpath. This starts the REPL in its own JVM instead. Needs coursier (cs) and uv.
set -euo pipefail
cd "$(dirname "$0")"

scala_version=$(grep -oP 'scalaVersion := "\K[^"]+' build.sbt)

cp=$(sbt -batch -error "Runtime/fullClasspath" "export Runtime/fullClasspath" | tail -1)
repl_cp=$(cs fetch --classpath \
  "org.scala-lang:scala3-repl_3:$scala_version" \
  "org.scala-lang:scala-library:$scala_version")

exec java -cp "$repl_cp" dotty.tools.repl.Main -classpath "$cp:$repl_cp"
