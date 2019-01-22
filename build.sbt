import scala.sys.process._

scalaVersion := "2.10.7"

javaOptions += "-Xmx4G"
fork in run := true

libraryDependencies := Seq(
  // Spark
  "org.apache.spark"      %% "spark-core"          % "1.6.3",
  "org.apache.spark"      %% "spark-mllib"         % "1.6.3",

  // HBase
  "org.apache.hbase"      %  "hbase"               % "2.1.2",
  "org.apache.hbase"      %  "hbase-common"        % "2.1.2",
  "org.apache.hbase"      %  "hbase-client"        % "2.1.2",
  "org.apache.hbase"      %  "hbase-server"        % "2.1.2",
  "org.apache.hbase"      %  "hbase-mapreduce"     % "2.1.2",
  "org.apache.hbase"      %  "hbase-spark"         % "2.0.0-alpha4",

  // Scrimage (PNG encoding)
  "com.sksamuel.scrimage" %% "scrimage-core"       % "2.1.7",

  // http4s (web server)
  "org.http4s"            %% "http4s-dsl"          % "0.16.6a",
  "org.http4s"            %% "http4s-blaze-server" % "0.16.6a",
)

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
