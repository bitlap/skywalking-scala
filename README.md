SkyWalking Extension Plugins for Scala 3
---

![CI][Badge-CI]


[Badge-CI]: https://github.com/bitlap/skywalking-scala/actions/workflows/ScalaCI.yml/badge.svg

## Introduction

[SkyWalking Scala](https://github.com/bitlap/skywalking-scala) is a SkyWalking Extension (Agent) Plugins for Scala 3.


| plugin                  | library     | maybe support version     | tested version |
|-------------------------|-------------|---------------------------|----------------|
| cats-effect-v3x-plugin  | cats-effect | 3.4.0-RC1 ~ 3.5.x         | 3.4.1          |
| zio-v200-plugin         | zio         | 2.0.0 ~ 2.0.2             | 2.0.2          |
| zio-v203-plugin         | zio         | 2.0.3 ~ 2.0.15            | 2.0.9,2.0.13   |
| ziogrpc-v06testx-plugin | zio-grpc    | 0.6.0-test1 ~ 0.6.0-test5 | 0.6.0-test4    |
| ziogrpc-v06rcx-plugin   | zio-grpc    | 0.6.0-test6 ~ 0.6.0-RC5   | 0.6.0-RC5      |
| ziohttp-v2x-plugin      | zio-http    | 2.0.0-RC2 ~ 2.0.0-RC10    | 2.0.0-RC10     |
| caliban-v2x-plugin      | caliban     | 2.0.0 ~ 2.0.2             | 2.0.1          |


> Other small versions of the library supported by this plugin may also work, but they have not been tested.

## Available Configurations
| key                                             | description                                                                           |
|-------------------------------------------------|---------------------------------------------------------------------------------------|
| `plugin.calibanv2.url_prefix`                   | Add a custom prefix to the graphql operation, default is `Caliban/GraphQL/`.          |
| `plugin.calibanv2.ignore_url_prefixes`          | Ignore operation name that start with this prefix, i.e. no span will be created.      |
| `plugin.calibanv2.collect_variables`            | Collect request variables.                                                            |
| `plugin.calibanv2.variables_length_threshold`   | How many characters to keep and send to the OAP backend.                              |
| `plugin.ziohttpv2.ignore_url_prefixes`          | Ignore request path that should start with this prefix, i.e. no span will be created. |
| `plugin.ziohttpv2.collect_http_params`          | Collect http query params.                                                            |
| `plugin.ziohttpv2.http_params_length_threshold` | How many characters to keep and send to the OAP backend.                              |

The prefix should be added when passing command line parameters, such as: `-Dskywalking.plugin.calibanv2.url_prefix=GQL/`

## Environment

- Java 11
- Scala 3.2.2
- SkyWalking 8.16.0

## How to use?

1. Clone code `git clone https://github.com/bitlap/skywalking-scala.git`
2. Enter the source file directory `cd skywalking-scala`
3. Build plugin `sh packageJars.sh`
4. Put the `dist/*.jar` into skywalking plugins folder

Please check the official documents for specific information
[skywalking.apache.org/docs](https://skywalking.apache.org/docs/skywalking-java/v8.15.0/en/setup/service-agent/java-agent/readme/)