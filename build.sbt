val doobieVersion = "1.0.0-RC1"

val doobie = Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion
  )

val db = Seq(
  "mysql" % "mysql-connector-java" % "8.0.22",
  "com.h2database" % "h2" % "1.4.200"
  )

val cats = Seq(
  "org.typelevel" %% "cats-effect" % "3.3.6"
  )

val logging = Seq(
  "org.slf4j" % "slf4j-api" % "2.0.0-alpha4",
  "ch.qos.logback" % "logback-classic" % "1.3.0-alpha10",
  "ch.qos.logback" % "logback-core" % "1.3.0-alpha10",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
)

val config = Seq("com.typesafe" % "config" % "1.4.1", "com.github.andr83" %% "scalaconfig" % "0.7")

lazy val cats_io_doobie = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "CATS Effect with Doobie Example",
    version := "0.0.1-SNAPSHOT",
    libraryDependencies ++= logging,
    libraryDependencies ++= config,
    libraryDependencies ++= doobie,
    libraryDependencies ++= cats,
    libraryDependencies ++= db,
    scalaVersion := "2.13.8"
  )

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-explaintypes", // Explain type errors in more detail.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xsource:3", // Warn for Scala 3 features
  "-Ywarn-dead-code" // Warn when dead code is identified.
)

javacOptions ++= Seq("-source", "17", "-target", "17", "-Xlint")

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*)       => MergeStrategy.discard
  case n if n.startsWith("reference.conf") => MergeStrategy.concat
  case _                                   => MergeStrategy.first
}
