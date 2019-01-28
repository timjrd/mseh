import scala.sys.process._

scalaVersion := "2.11.8"

javaOptions += "-Xmx4G"
fork in run := true

libraryDependencies := Seq(
  // Spark
  "org.apache.spark"      %% "spark-core"          % "2.2.0",
  "org.apache.spark"      %% "spark-mllib"         % "2.2.0",

  // HBase
  "org.apache.hbase"      %  "hbase"               % "2.1.2",
  "org.apache.hbase"      %  "hbase-common"        % "2.1.2",
  "org.apache.hbase"      %  "hbase-client"        % "2.1.2",
  "org.apache.hbase"      %  "hbase-server"        % "2.1.2",
  "org.apache.hbase"      %  "hbase-mapreduce"     % "2.1.2",

  // Scrimage (PNG encoding)
  "com.sksamuel.scrimage" %% "scrimage-core"       % "2.1.8",

  // http4s (web server)
  "org.http4s"            %% "http4s-dsl"          % "0.16.6a",
  "org.http4s"            %% "http4s-blaze-server" % "0.16.6a",
)

assemblyMergeStrategy in assembly := {
  case "META-INF/io.netty.versions.properties" => MergeStrategy.discard
  case x @ PathList("META-INF", _*) => (assemblyMergeStrategy in assembly).value(x)
  case _ => MergeStrategy.first
}
