#!/bin/bash

folder=dist-v2
rm -rf $folder
mkdir $folder

version=`cat version.sbt | awk -F '"' '{print $2}'`
ziohttpv2=ziohttp-v2x-plugin
ziogrpctest=ziogrpc-v06testx-plugin
caliban=caliban-v2x-plugin
zio200=zio-v200-plugin
catseffect3=cats-effect-v3x-plugin


sbt clean assembly

cp plugins/$zio200/target/apm-$zio200-$version.jar $folder/apm-$zio200-$version.jar
cp plugins/$ziohttpv2/target/apm-$ziohttpv2-$version.jar $folder/apm-$ziohttpv2-$version.jar
cp plugins/$ziogrpctest/target/apm-$ziogrpctest-$version.jar $folder/apm-$ziogrpctest-$version.jar
cp plugins/$caliban/target/apm-$caliban-$version.jar $folder/apm-$caliban-$version.jar
cp plugins/$catseffect3/target/apm-$catseffect3-$version.jar $folder/apm-$catseffect3-$version.jar