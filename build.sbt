scalaVersion := "2.12.7"

val http4sVersion = "0.18.21"

libraryDependencies := Seq(
  "org.apache.spark" %% "spark-core"          % "2.4.0",
  "org.http4s"       %% "http4s-dsl"          % http4sVersion,
  "org.http4s"       %% "http4s-blaze-server" % http4sVersion
)

scalacOptions ++= Seq("-Ypartial-unification")
