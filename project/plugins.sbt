//addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.7")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")
addSbtPlugin("com.eed3si9n"  % "sbt-assembly" % "2.1.1")

// for scenarios
addSbtPlugin("com.thesamet"   % "sbt-protoc"          % "1.0.6")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")

libraryDependencies ++= Seq(
  "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "0.6.0-rc6",
  "com.thesamet.scalapb"          %% "compilerplugin"   % "0.11.10"
)
