SkyWalking Extension Plugins for Scala 3
---

![CI][Badge-CI]


[Badge-CI]: https://github.com/bitlap/skywalking-scala/actions/workflows/ScalaCI.yml/badge.svg

## Introduction

[SkyWalking Scala](https://github.com/bitlap/skywalking-scala) is a SkyWalking Extension (Agent) Plugins for Scala 3.


| plugin              | library  | tested version | breakthrough points                                                   |
|---------------------|----------|----------------|-----------------------------------------------------------------------|
| caliban-v2x-plugin  | caliban  | 2.0.1          | `GraphQLInterpreter#executeRequest`                                   |
| zio-v2x-plugin      | zio      | 2.0.0          | `FiberRuntime#run`,`blockingExecutor#submit`,`zio.Scheduler#schedule` |
| ziogrpc-v06x-plugin | zio-grpc | 0.6.0-test4    | `ZChannel#newCall`,`ZServerCallHandler#startCall`,`ZServerCall`       |
| ziohttp-v2x-plugin  | zio-http | 2.0.0-RC10     | `Http.collectHttp`                                                    |

> Other small versions of the library supported by this plugin may also work, but they have not been tested.

Also need `apm-jdk-threadpool-plugin-x.y.z.jar` and `apm-jdk-threading-plugin-x.y.z.jar`.

## Available Configurations
| key                                           | description                                                                           |
|-----------------------------------------------|---------------------------------------------------------------------------------------|
| `plugin.caliban.url_prefix`                   | Add a custom prefix to the graphql operation, default is `GraphQL/`.                  |
| `plugin.caliban.ignore_url_prefixes`          | Ignore operation name that start with this prefix, i.e. no span will be created.      |
| `plugin.caliban.collect_variables`            | Collect request variables.                                                            |
| `plugin.caliban.variables_length_threshold`   | How many characters to keep and send to the OAP backend.                              |
| `plugin.ziohttp.ignore_url_prefixes`          | Ignore request path that should start with this prefix, i.e. no span will be created. |
| `plugin.ziohttp.collect_http_params`          | Collect http query params.                                                            |
| `plugin.ziohttp.http_params_length_threshold` | How many characters to keep and send to the OAP backend.                              |

The prefix should be added when passing command line parameters, such as: `-Dskywalking.plugin.caliban.url_prefix=GQL/`

## Environment

- Java 11
- Scala 3.2.0
- SkyWalking 8.16.0

## How to use?

1. Clone code `git clone https://github.com/bitlap/skywalking-scala.git`
2. Enter the source file directory `cd skywalking-scala`
3. Build plugin `sh packageJars.sh`
4. Put the `dist/*.jar` into skywalking plugins folder

Please check the official documents for specific information
[skywalking.apache.org/docs](https://skywalking.apache.org/docs/skywalking-java/v8.15.0/en/setup/service-agent/java-agent/readme/)