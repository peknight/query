import com.peknight.build.gav.*
import com.peknight.build.sbt.*

commonSettings

lazy val query = (project in file("."))
  .settings(name := "query")
  .aggregate(queryCore.projectRefs *)
  .aggregate(queryHttp4s.projectRefs *)

lazy val queryCore = (projectMatrix in file("query-core"))
  .settings(name := "query-core")
  .settings(libraryDependencies ++= dependencies(
    peknight.codec,
    typelevel.catsParse,
    typelevel.spire,
  ))
  .settings(libraryDependencies ++= testDependencies(scalaTest))
  .jvmPlatform(scalaVersions = Seq(scala.scala3.version))
  .jsPlatform(scalaVersions = Seq(scala.scala3.version))

lazy val queryHttp4s = (projectMatrix in file("query-http4s"))
  .dependsOn(queryCore)
  .settings(name := "query-http4s")
  .settings(libraryDependencies ++= dependencies(http4s))
  .settings(libraryDependencies ++= testDependencies(scalaTest))
  .jvmPlatform(scalaVersions = Seq(scala.scala3.version))
  .jsPlatform(scalaVersions = Seq(scala.scala3.version))
