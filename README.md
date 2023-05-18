Skywalking Extension Plugins for Scala 3
---

![CI][Badge-CI]


[Badge-CI]: https://github.com/bitlap/skywalking-scala/actions/workflows/ScalaCI.yml/badge.svg

Repository NOT Available!


## Features

- [x] caliban: 2.0.1
- [x] zio-grpc: 0.6.0-test4
- [x] zio-http(zhttp): 2.0.0-RC10
  - [x] `Http.collectHttp`
- [x] zio: 2.0.0
  - [x] `FiberRuntime#run`
  - [x] `blockingExecutor#submit`

## Environment

- Java 11
- Scala 3.2.0
- Skywalking 8.13.0

## How to use?

1. Clone code `git clone https://github.com/bitlap/skywalking-scala.git`
2. Enter the source file directory `cd skywalking-scala`
3. Compile and package locally `sbt assembly`
4. Put the jars into skywalking plugins folder:
```
skywalking-scala/ziogrpc-v06x-plugin/target/scala-3.2.0/apm-zio-v2x-plugin-0.1.0-SNAPSHOT.jar
skywalking-scala/ziogrpc-v06x-plugin/target/scala-3.2.0/apm-ziohttp-v2RC10-plugin-0.1.0-SNAPSHOT.jar
skywalking-scala/ziogrpc-v06x-plugin/target/scala-3.2.0/apm-ziogrpc-v06x-plugin-0.1.0-SNAPSHOT.jar
skywalking-scala/caliban-v2x-plugin/target/scala-3.2.0/apm-caliban-v2x-plugin-0.1.0-SNAPSHOT.jar
```

Please check the official documents for specific information
[skywalking.apache.org/docs](https://skywalking.apache.org/docs/skywalking-java/v8.15.0/en/setup/service-agent/java-agent/readme/)

