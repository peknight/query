import com.peknight.build.gav.*
import com.peknight.build.sbt.*

commonSettings

lazy val query = (project in file("."))
  .settings(name := "query")
  .aggregate(
    queryCore.jvm,
    queryCore.js,
    queryHttp4s.jvm,
    queryHttp4s.js,
  )

lazy val queryCore = (crossProject(JVMPlatform, JSPlatform) in file("query-core"))
  .settings(name := "query-core")
  .settings(crossDependencies(
    peknight.codec,
    typelevel.catsParse,
    typelevel.spire,
  ))
  .settings(crossTestDependencies(scalaTest))

lazy val queryHttp4s = (crossProject(JVMPlatform, JSPlatform) in file("query-http4s"))
  .dependsOn(queryCore)
  .settings(name := "query-http4s")
  .settings(crossDependencies(http4s))
  .settings(crossTestDependencies(scalaTest))
