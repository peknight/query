import com.peknight.build.gav.*
import com.peknight.build.sbt.*

commonSettings

lazy val query = (project in file("."))
  .aggregate(
    queryCore.jvm,
    queryCore.js,
    queryHttp4s.jvm,
    queryHttp4s.js,
  )
  .settings(
    name := "query",
  )

lazy val queryCore = (crossProject(JVMPlatform, JSPlatform) in file("query-core"))
  .settings(crossDependencies(
    peknight.codec,
    typelevel.catsParse,
  ))
  .settings(crossTestDependencies(scalaTest))
  .settings(
    name := "query-core",
  )

lazy val queryHttp4s = (crossProject(JVMPlatform, JSPlatform) in file("query-http4s"))
  .dependsOn(queryCore)
  .settings(crossDependencies(http4s))
  .settings(crossTestDependencies(scalaTest))
  .settings(
    name := "query-http4s",
  )
