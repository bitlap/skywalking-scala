Skywalking Extension Plugins for Scala 3
---

Repository NOT Available!


## Features

- [x] caliban: 2.0.1
- [x] zio-grpc: 0.6.0-test4
  - [x] client
  - [x] server
- [ ] zio: 2.0.0
  - [x] `FiberRuntime`
    - [x] `run`
  - [x] `blockingExecutor`
    - [x] `submit`

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
skywalking-scala/ziogrpc-v06x-plugin/target/scala-3.2.0/apm-ziogrpc-v06x-plugin-0.1.0-SNAPSHOT.jar
skywalking-scala/caliban-v2x-plugin/target/scala-3.2.0/apm-caliban-v2x-plugin-0.1.0-SNAPSHOT.jar
```

Please check the official documents for specific information
[skywalking.apache.org/docs](https://skywalking.apache.org/docs/skywalking-java/v8.15.0/en/setup/service-agent/java-agent/readme/)

