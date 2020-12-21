name := "webhook-service"

version := "0.1"

scalaVersion := "2.13.4"

val http4sVersion = "1.0.0-M5+109-c8e90397-SNAPSHOT"

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core" % "0.9.0",
  "org.tpolecat" %% "doobie-hikari" % "0.9.0",
  "org.tpolecat" %% "doobie-postgres" % "0.9.0",
  "org.tpolecat" %% "doobie-specs2" % "0.9.0" % "test",
  "org.tpolecat" %% "doobie-scalatest" % "0.9.0" % "test",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % "0.13.0",
  "io.circe" %% "circe-literal" % "0.13.0",
  "dev.zio" %% "zio-streams" % "1.0.2",
  "dev.zio" %% "zio-kafka"   % "0.13.0",
  "dev.zio" %% "zio-interop-cats" % "2.2.0.1",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.2",
  "dev.zio" %% "zio-config-typesafe" % "1.0.0-RC31-1",
  "com.softwaremill.sttp.client3" %% "core" % "3.0.0-RC11",
  "org.manatki" %% "derevo-circe-magnolia" % "0.11.5",
  "com.softwaremill.sttp.client3" %% "circe" % "3.0.0-RC11",
  "com.softwaremill.sttp.client3" %% "httpclient-backend-zio" % "3.0.0-RC11",
  "dev.zio" %% "zio" % "1.0.3",
  "dev.zio" %% "zio-test" % "1.0.3" % "test",
  "dev.zio" %% "zio-test-sbt"      % "1.0.3" % "test",
  "dev.zio" %% "zio-test-magnolia" % "1.0.3" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "dev.zio" %% "zio-logging-slf4j" % "0.4.0",
  "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.14.0",
  "com.github.pureconfig" %% "pureconfig" % "0.14.0",
  "io.chrisdavenport" %% "log4cats-slf4j"   % "1.1.1"
    )

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")


scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-Ymacro-annotations"
)
