#!/bin/bash

folder=dist-v1

rm -rf $folder
mkdir $folder

version=`cat version.sbt | awk -F '"' '{print $2}'`
ziohttpv2=ziohttp-v2x-plugin
ziogrpcrc=ziogrpc-v06rcx-plugin
caliban=caliban-v2x-plugin
zio203=zio-v203-plugin
catseffect3=cats-effect-v3x-plugin


sbt clean assembly

cp plugins/$zio203/target/apm-$zio203-$version.jar $folder/apm-$zio203-$version.jar
cp plugins/$ziohttpv2/target/apm-$ziohttpv2-$version.jar $folder/apm-$ziohttpv2-$version.jar
cp plugins/$ziogrpcrc/target/apm-$ziogrpcrc-$version.jar $folder/apm-$ziogrpcrc-$version.jar
cp plugins/$caliban/target/apm-$caliban-$version.jar $folder/apm-$caliban-$version.jar
cp plugins/$catseffect3/target/apm-$catseffect3-$version.jar $folder/apm-$catseffect3-$version.jar