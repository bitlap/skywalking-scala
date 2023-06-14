#!/bin/bash

rm -rf dist
mkdir dist

version=`cat version.sbt | awk -F '"' '{print $2}'`
ziohttpv2=ziohttp-v2x-plugin
ziogrpcrc=ziogrpc-v06rcx-plugin
ziogrpctest=ziogrpc-v06testx-plugin
caliban=caliban-v2x-plugin
zio200=zio-v200-plugin
zio203=zio-v203-plugin


sbt clean assembly

cp $ziohttpv2/target/apm-$ziohttpv2-$version.jar dist/apm-$ziohttpv2-$version.jar

cp $ziogrpcrc/target/apm-$ziogrpcrc-$version.jar dist/apm-$ziogrpcrc-$version.jar
cp $ziogrpctest/target/apm-$ziogrpctest-$version.jar dist/apm-$ziogrpctest-$version.jar

cp $caliban/target/apm-$caliban-$version.jar dist/apm-$caliban-$version.jar

cp $zio200/target/apm-$zio200-$version.jar dist/apm-$zio200-$version.jar
cp $zio203/target/apm-$zio203-$version.jar dist/apm-$zio203-$version.jar