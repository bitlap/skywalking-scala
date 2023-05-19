#!/bin/bash

rm -rf dist
mkdir dist

version=`cat version.sbt | awk -F '"' '{print $2}'`
ziohttp=ziohttp-v2RC10-plugin
ziogrpc=ziogrpc-v06x-plugin
caliban=caliban-v2x-plugin
zio=zio-v2x-plugin

sbt clean assembly

cp $ziohttp/target/scala-3.2.0/apm-$ziohttp-$version.jar dist/apm-$ziohttp-$version.jar

cp $ziogrpc/target/scala-3.2.0/apm-$ziogrpc-$version.jar dist/apm-$ziogrpc-$version.jar

cp $caliban/target/scala-3.2.0/apm-$caliban-$version.jar dist/apm-$caliban-$version.jar

cp $zio/target/scala-3.2.0/apm-$zio-$version.jar dist/apm-$zio-$version.jar