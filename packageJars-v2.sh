#!/bin/bash

folder=dist-v2

rm -rf $folder
mkdir $folder

version=`cat version.sbt | awk -F '"' '{print $2}'`
ziohttpv2=ziohttp-v2x-plugin
ziogrpcrc=ziogrpc-v06rcx-plugin
caliban=caliban-v2x-plugin
zio2=zio-v2x-plugin
ce3=cats-effect-v3x-plugin
executors=executors-plugin


sbt clean assembly

cp plugins/$zio2/target/apm-$zio2-$version.jar $folder/apm-$zio2-$version.jar
cp plugins/$ziohttpv2/target/apm-$ziohttpv2-$version.jar $folder/apm-$ziohttpv2-$version.jar
cp plugins/$ziogrpcrc/target/apm-$ziogrpcrc-$version.jar $folder/apm-$ziogrpcrc-$version.jar
cp plugins/$caliban/target/apm-$caliban-$version.jar $folder/apm-$caliban-$version.jar
cp plugins/$ce3/target/apm-$ce3-$version.jar $folder/apm-$ce3-$version.jar
cp plugins/$executors/target/apm-$executors-$version.jar $folder/apm-$executors-$version.jar