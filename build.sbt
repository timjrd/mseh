import scala.sys.process._

scalaVersion := "2.12.7"

val http4sVersion = "0.18.21"

// HBase
resolvers ++= Seq(
  "Apache HBase" at "https://repository.apache.org/content/repositories/releases",
  "Thrift"       at "https://people.apache.org/~rawson/repo/"
)

libraryDependencies := Seq(
  // Spark
  "org.apache.spark"      %% "spark-core"          % "2.4.0",
  "org.apache.spark"      %% "spark-mllib"         % "2.4.0",

  // HBase
  "org.apache.hadoop"     %  "hadoop-core"         % "0.20.2",
  "org.apache.hbase"      %  "hbase"               % "0.90.4",

  // Scrimage (PNG encoding)
  "com.sksamuel.scrimage" %% "scrimage-core"       % "2.1.8",

  // http4s (web server)
  "org.http4s"            %% "http4s-dsl"          % http4sVersion,
  "org.http4s"            %% "http4s-blaze-server" % http4sVersion,
)

// http4s
scalacOptions ++= Seq("-Ypartial-unification")

// Custom tasks
val windows = System.getProperty("os.name").startsWith("Windows")
val ext = if (windows) "cmd" else "sh"

def runClass(cmd: String) = (runMain in Compile).toTask(" " + cmd)

lazy val startHBase = taskKey[Unit]("start HBase")
startHBase := Process("hbase/bin/start-hbase." + ext).!

lazy val stopHBase_ = Process("hbase/bin/stop-hbase." + ext).!
lazy val stopHBase  = taskKey[Unit]("stop HBase")
stopHBase := stopHBase_

lazy val batch = taskKey[Unit]("run tiles precomputation")
batch := runClass("Batch").dependsOn(startHBase).andFinally(stopHBase_).value

lazy val server = taskKey[Unit]("run web server")
server := runClass("Server").dependsOn(startHBase).andFinally(stopHBase_).value
