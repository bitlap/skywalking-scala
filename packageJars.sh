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
catseffect3=cats-effect-v3x-plugin


sbt clean assembly

cp plugins/$ziohttpv2/target/apm-$ziohttpv2-$version.jar dist/apm-$ziohttpv2-$version.jar

cp plugins/$ziogrpcrc/target/apm-$ziogrpcrc-$version.jar dist/apm-$ziogrpcrc-$version.jar
cp plugins/$ziogrpctest/target/apm-$ziogrpctest-$version.jar dist/apm-$ziogrpctest-$version.jar

cp plugins/$caliban/target/apm-$caliban-$version.jar dist/apm-$caliban-$version.jar

cp plugins/$zio200/target/apm-$zio200-$version.jar dist/apm-$zio200-$version.jar
cp plugins/$zio203/target/apm-$zio203-$version.jar dist/apm-$zio203-$version.jar

cp plugins/$catseffect3/target/apm-$catseffect3-$version.jar dist/apm-$catseffect3-$version.jar