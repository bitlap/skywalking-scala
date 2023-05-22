SkyWalking Extension Plugins for Scala 3
---

![CI][Badge-CI]


[Badge-CI]: https://github.com/bitlap/skywalking-scala/actions/workflows/ScalaCI.yml/badge.svg

## Features

> Other small versions of the library supported by this plugin may also work, but they have not been tested.

- [x] caliban: 2.0.1
- [x] zio-grpc: 0.6.0-test4
- [x] zio-http(zhttp): 2.0.0-RC10
  - [x] `Http.collectHttp`
- [x] zio: 2.0.0
  - [x] `FiberRuntime#run`
  - [x] `blockingExecutor#submit`
  - [x] Also need `apm-jdk-threadpool-plugin-8.15.0.jar` and `apm-jdk-threading-plugin-8.15.0.jar`

## Environment

- Java 11
- Scala 3.2.0
- SkyWalking 8.13.0

## How to use?

1. Clone code `git clone https://github.com/bitlap/skywalking-scala.git`
2. Enter the source file directory `cd skywalking-scala`
3. Build plugin `sh packageJars.sh`
4. Put the `dist/*.jar` into skywalking plugins folder

Please check the official documents for specific information
[skywalking.apache.org/docs](https://skywalking.apache.org/docs/skywalking-java/v8.15.0/en/setup/service-agent/java-agent/readme/)