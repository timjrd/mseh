# Multi-scale earth heightmap

## Cluster setup
*TODO*

## Local setup
Install the [SBT](https://www.scala-sbt.org/download.html) build tool.

`data/dem3` contains 64 random tiles (185Mo) taken from the complete
dataset. This partial dataset will be used to generate an incomplete
heightmap on a single machine for testing purposes.

To run the heightmap precomputation:
```
sbt batch
```

To launch the web server:
```
sbt server
```
then go to http://localhost:8080 .

`batch` and `server` should automatically start (and hopefully stop)
HBase, but you can manage it manually with:
```
sbt startHBase
```
and
```
sbt stopHBase
```
