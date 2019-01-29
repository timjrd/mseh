# Multi-scale earth heightmap
https://github.com/timjrd/mseh

Timothée Jourde, Jeffrey Seutin, Okan Soyturk

Please install the [SBT](https://www.scala-sbt.org/download.html) build tool.

## Tiles generation
### Local setup
Launch HBase locally, then:
```
sbt "run batch --local <data directory>"
```

### Cluster setup
```
sbt assembly
spark-submit [...] target/scala-2.10/mseh-assembly-0.1.0-SNAPSHOT.jar batch <hdfs data directory>
```

## Tile server
```
sbt "run serve <port> <prefix>"
```
