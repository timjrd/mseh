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