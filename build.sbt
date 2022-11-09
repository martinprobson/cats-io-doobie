val doobieVersion = "1.0.0-RC1"
val scalaTestVersion = "3.2.11"

val doobie = Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion
)

val db = Seq("mysql" % "mysql-connector-java" % "8.0.22", "com.h2database" % "h2" % "1.4.200")

val cats = Seq("org.typelevel" %% "cats-effect" % "3.3.6")

val logging = Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.11" % Runtime,
  "org.typelevel" %% "log4cats-slf4j" % "2.5.0"
)

val test = Seq(
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalatest" %% "scalatest-flatspec" % scalaTestVersion % Test
)

val config =
  Seq(
    "com.typesafe" % "config" % "1.4.1",
    "com.github.andr83" %% "scalaconfig" % "0.7",
    "com.github.pureconfig" %% "pureconfig" % "0.17.1",
    "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.1"
  )

lazy val cats_io_doobie = (project in file("."))
  .settings(
    name := "CATS Effect with Doobie Example",
    version := "0.0.1-SNAPSHOT",
    libraryDependencies ++= logging,
    libraryDependencies ++= config,
    libraryDependencies ++= doobie,
    libraryDependencies ++= cats,
    libraryDependencies ++= test,
    libraryDependencies ++= db,
    scalaVersion := "2.13.8"
  )

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*)       => MergeStrategy.discard
  case n if n.startsWith("reference.conf") => MergeStrategy.concat
  case _                                   => MergeStrategy.first
}
