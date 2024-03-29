#!/bin/bash

cd $(dirname $0)/../

folder=scenarios/skywalking-agent/plugins

rm -rf $folder
mkdir $folder

version=`cat version.sbt | awk -F '"' '{print $2}'`
zhttp=ziohttp-v2x-plugin
zgrpc=ziogrpc-v06rcx-plugin
caliban=caliban-v2x-plugin
zio2=zio-v2x-plugin
ce3=cats-effect-v3x-plugin
executors=executors-plugin
ziocache=ziocache-plugin


sbt clean assembly

cp plugins/$zio2/target/apm-$zio2-$version.jar $folder/apm-$zio2-$version.jar
cp plugins/$zhttp/target/apm-$zhttp-$version.jar $folder/apm-$zhttp-$version.jar
cp plugins/$zgrpc/target/apm-$zgrpc-$version.jar $folder/apm-$zgrpc-$version.jar
cp plugins/$caliban/target/apm-$caliban-$version.jar $folder/apm-$caliban-$version.jar
cp plugins/$ce3/target/apm-$ce3-$version.jar $folder/apm-$ce3-$version.jar
cp plugins/$executors/target/apm-$executors-$version.jar $folder/apm-$executors-$version.jar
cp plugins/$ziocache/target/apm-$ziocache-$version.jar $folder/apm-$ziocache-$version.jar