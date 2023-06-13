#!/bin/bash

rm -rf dist
mkdir dist

version=`cat version.sbt | awk -F '"' '{print $2}'`
ziohttpv2=ziohttp-v2x-plugin
ziogrpc=ziogrpc-v06x-plugin
caliban=caliban-v2x-plugin
zio200=zio-v200-plugin
zio203=zio-v203-plugin


sbt clean assembly

cp $ziohttpv2/target/scala-3.2.2/apm-$ziohttpv2-$version.jar dist/apm-$ziohttpv2-$version.jar

cp $ziogrpc/target/scala-3.2.2/apm-$ziogrpc-$version.jar dist/apm-$ziogrpc-$version.jar

cp $caliban/target/scala-3.2.2/apm-$caliban-$version.jar dist/apm-$caliban-$version.jar

cp $zio200/target/scala-3.2.2/apm-$zio200-$version.jar dist/apm-$zio200-$version.jar
cp $zio203/target/scala-3.2.2/apm-$zio203-$version.jar dist/apm-$zio203-$version.jar