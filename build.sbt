ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

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
    queryParser.jvm,
    queryParser.js,
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
      "com.peknight" %%% "codec-core" % pekCodecVersion,
      "com.peknight" %%% "commons-string" % pekCommonsVersion,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test,
    ),
  )

lazy val queryParser = (crossProject(JSPlatform, JVMPlatform) in file("query-parser"))
  .dependsOn(queryCore)
  .settings(commonSettings)
  .settings(
    name := "query-parser",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-parse" % catsParseVersion,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test,
    ),
  )

val scalaTestVersion = "3.2.16"
val catsParseVersion = "0.3.10"
val pekVersion = "0.1.0-SNAPSHOT"
val pekCodecVersion = pekVersion
val pekCommonsVersion = pekVersion