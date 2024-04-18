name := "MinesweeperApp"

version := "1.0"

scalaVersion := "3.4.1"


Compile / run / mainClass := Some("MinesweeperGUI")
Compile / packageBin / mainClass := Some("MinesweeperGUI")
assembly / mainClass := Some("MinesweeperGUI")

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0"
