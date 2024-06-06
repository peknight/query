ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.2"

ThisBuild / organization := "com.peknight"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xfatal-warnings",
    "-language:strictEquality",
    "-Xmax-inlines:64",
    "-Ximplicit-search-limit:500000"
  ),
)

lazy val query = (project in file("."))
  .aggregate(
    queryCore.jvm,
    queryCore.js,
    queryHttp4s.jvm,
    queryHttp4s.js,
  )
  .settings(commonSettings)
  .settings(
    name := "query",
  )

lazy val queryCore = (crossProject(JSPlatform, JVMPlatform) in file("query-core"))
  .settings(commonSettings)
  .settings(
    name := "query-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-parse" % catsParseVersion,
      "com.peknight" %%% "codec-core" % pekCodecVersion,
      "com.peknight" %%% "commons-string" % pekCommonsVersion,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test,
    ),
  )

lazy val queryHttp4s = (crossProject(JSPlatform, JVMPlatform) in file("query-http4s"))
  .dependsOn(queryCore)
  .settings(commonSettings)
  .settings(
    name := "query-http4s",
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-core" % http4sVersion,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test,
    ),
  )

val catsParseVersion = "0.3.10"
val http4sVersion = "1.0.0-M34"
val scalaTestVersion = "3.2.18"
val pekVersion = "0.1.0-SNAPSHOT"
val pekCodecVersion = pekVersion
val pekCommonsVersion = pekVersion
