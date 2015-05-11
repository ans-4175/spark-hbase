lazy val root = (project in file(".")).
  settings(
    name := "spark_hbase",
    version := "1.0",
    scalaVersion := "2.11.6",
    sparkVersion := "1.3.0"
  )

resolvers ++= Seq(
  "Apache Repo"   at "https://repository.apache.org/content/repositories/releases",
  "Thrift-Repo"   at "http://people.apache.org/~rawson/repo",
  "ClouderaRepo"  at "https://repository.cloudera.com/content/repositories/releases",
  "ClouderaRcs"   at "https://repository.cloudera.com/artifactory/cdh-releases-rcs",
  "releases"      at "http://scala-tools.org/repo-releases",
  "rediscala"     at "http://dl.bintray.com/etaty/maven"
) 

libraryDependencies ++= Seq(
  "org.apache.hbase"         % "hbase"                    % "1.0.0-cdh5.4.0",
  "org.apache.hbase"         % "hbase-client"             % "1.0.0-cdh5.4.0",
  "org.apache.hbase"         % "hbase-server"             % "1.0.0-cdh5.4.0",
  "org.apache.hbase"         % "hbase-common"             % "1.0.0-cdh5.4.0"
)

mergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case "META-INF/jersey-module-version"        => MergeStrategy.first
  case _                                       => MergeStrategy.first
}
