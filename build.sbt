ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "rest-api-with-cats"
  )


libraryDependencies ++= {

  lazy val doobieVersion = "1.0.0-RC4"
  lazy val http4sVersion = "0.23.18"
  lazy val circeVersion = "0.14.5"

  Seq(
    "org.tpolecat"  %%  "doobie-core" % doobieVersion,
    "org.tpolecat"  %%  "doobie-h2"   % doobieVersion,
    "org.tpolecat"  %% "doobie-hikari" % doobieVersion,
    "org.tpolecat"  %% "doobie-postgres"  % doobieVersion,
    "org.tpolecat"  %% "doobie-specs2" % doobieVersion,
    "org.http4s"  %% "http4s-ember-server" % http4sVersion,
    "org.http4s"  %% "http4s-circe" % http4sVersion,
    "org.http4s"  %% "http4s-dsl" % http4sVersion,
    "io.circe"  %% "circe-core" % circeVersion,
    "io.circe"  %% "circe-generic" % circeVersion,
    "io.circe"  %% "circe-config" % "0.10.0",
    "org.typelevel" %% "log4cats-slf4j" % "2.6.0"
  )

}