ThisBuild / resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Releases" at "https://s01.oss.sonatype.org/content/repositories/releases"
)

lazy val scala3Version         = "3.3.0"
lazy val scalatestVersion      = "3.2.15"
lazy val junitVersion          = "4.12"
lazy val mockitoVersion        = "5.0.0"
lazy val junitInterfaceVersion = "0.12"

lazy val skywalkingVersion = "9.1.0"

lazy val calibanVersion    = "2.0.1"
lazy val zioGrpcVersion    = "0.6.0-rc5"
lazy val zioVersion        = "2.0.15"
lazy val zioHttp2Version   = "2.0.0-RC10"
lazy val catsEffectVersion = "3.4.1"
lazy val zioCacheVersion   = "0.2.3"

inThisBuild(
  List(
    scalaVersion := scala3Version,
    organization := "org.bitlap",
    homepage     := Some(url("https://github.com/bitlap/skywalking-scala")),
    licenses     := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        id = "jxnu-liguobin",
        name = "jxnu-liguobin",
        email = "dreamylost@outlook.com",
        url = url("https://github/jxnu-liguobin")
      )
    )
  )
)

lazy val commonSettings =
  Seq(
    scalaVersion                  := scala3Version,
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    scalacOptions ++= Seq(
      "-explain",
      "unchecked",
      "-deprecation",
      "-feature"
    ),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    assembly / assemblyShadeRules := Seq(
      ShadeRule.rename("net.bytebuddy.**" -> s"org.apache.skywalking.apm.dependencies.net.bytebuddy.@1").inAll
    ),
    libraryDependencies ++= Seq(
      "org.scalatest"        %% "scalatest"       % scalatestVersion      % Test,
      "junit"                 % "junit"           % junitVersion          % Test,
      "org.mockito"           % "mockito-core"    % mockitoVersion        % Test,
      "org.apache.skywalking" % "apm-test-tools"  % skywalkingVersion     % Test,
      "org.apache.skywalking" % "apm-agent-core"  % skywalkingVersion     % Provided,
      "com.github.sbt"        % "junit-interface" % junitInterfaceVersion % Test
    ),
    crossPaths               := false,
    Test / testOptions       := Seq(Tests.Argument(TestFrameworks.JUnit, "-a")),
    run / fork               := true,
    Test / fork              := true,
    Test / parallelExecution := true
  )

lazy val `skywalking-scala` = (project in file("."))
  .aggregate(
    `plugin-common`,
    `ziogrpc-plugin-common`,
    `executors-plugin`,
    `caliban-v2x-plugin`,
    `zio-v2x-plugin`,
    `ziohttp-v2x-plugin`,
    `cats-effect-v3x-plugin`,
    `ziogrpc-v06rcx-plugin`,
    `ziocache-plugin`
  )
  .settings(
    publish / skip := true,
    commonSettings,
    commands ++= Commands.value
  )

lazy val `caliban-v2x-plugin` = (project in file("plugins/caliban-v2x-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                                         := "caliban-v2x-plugin",
    assembly / assemblyJarName                   := s"apm-caliban-v2x-plugin-${(ThisBuild / version).value}.jar",
    assemblyPackageScala / assembleArtifact      := false,
    assemblyPackageDependency / assembleArtifact := false,
    libraryDependencies ++= Seq(
      "com.github.ghostdogpr" %% "caliban" % calibanVersion % Provided
    )
  )
  .dependsOn(`plugin-common`)

lazy val `ziogrpc-plugin-common` = (project in file("shared/ziogrpc-plugin-common"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                                         := "ziogrpc-plugin-common",
    assemblyPackageScala / assembleArtifact      := false,
    assemblyPackageDependency / assembleArtifact := false,
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-core" % zioGrpcVersion % Provided
    )
  )
  .dependsOn(`plugin-common`)

lazy val `ziogrpc-v06rcx-plugin` = (project in file("plugins/ziogrpc-v06rcx-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                                         := "ziogrpc-v06rcx-plugin",
    assembly / assemblyJarName                   := s"apm-ziogrpc-v06rcx-plugin-${(ThisBuild / version).value}.jar",
    assemblyPackageScala / assembleArtifact      := false,
    assemblyPackageDependency / assembleArtifact := false,
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-core" % zioGrpcVersion % Provided
    )
  )
  .dependsOn(`ziogrpc-plugin-common`)

lazy val `plugin-common` = (project in file("shared/plugin-common")).settings(
  commonSettings,
  commands ++= Commands.value,
  assemblyPackageScala / assembleArtifact      := false,
  assemblyPackageDependency / assembleArtifact := false,
  name                                         := "plugin-common"
)

lazy val `cats-effect-v3x-plugin` = (project in file("plugins/cats-effect-v3x-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                                         := "cats-effect-v3x-plugin",
    assembly / assemblyJarName                   := s"apm-cats-effect-v3x-plugin-${(ThisBuild / version).value}.jar",
    assemblyPackageScala / assembleArtifact      := false,
    assemblyPackageDependency / assembleArtifact := false,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion % Provided
    )
  )
  .dependsOn(`plugin-common`)

lazy val `zio-v2x-plugin` = (project in file("plugins/zio-v2x-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                                         := "zio-v2x-plugin",
    assembly / assemblyJarName                   := s"apm-zio-v2x-plugin-${(ThisBuild / version).value}.jar",
    assemblyPackageScala / assembleArtifact      := false,
    assemblyPackageDependency / assembleArtifact := false,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion % Provided
    )
  )
  .dependsOn(`plugin-common`)

lazy val `ziocache-plugin` = (project in file("plugins/ziocache-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                                         := "ziocache-plugin",
    assembly / assemblyJarName                   := s"apm-ziocache-plugin-${(ThisBuild / version).value}.jar",
    assemblyPackageScala / assembleArtifact      := false,
    assemblyPackageDependency / assembleArtifact := false,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-cache" % zioCacheVersion % Provided
    )
  )
  .dependsOn(`plugin-common`)

lazy val `ziohttp-v2x-plugin` = (project in file("plugins/ziohttp-v2x-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                                         := "ziohttp-v2x-plugin",
    assembly / assemblyJarName                   := s"apm-ziohttp-v2x-plugin-${(ThisBuild / version).value}.jar",
    assemblyPackageScala / assembleArtifact      := false,
    assemblyPackageDependency / assembleArtifact := false,
    libraryDependencies ++= Seq(
      "io.d11" %% "zhttp" % zioHttp2Version % Provided
    )
  )
  .dependsOn(`plugin-common`)

lazy val `executors-plugin` = (project in file("plugins/executors-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                                         := "executors-plugin",
    assembly / assemblyJarName                   := s"apm-executors-plugin-${(ThisBuild / version).value}.jar",
    assemblyPackageScala / assembleArtifact      := false,
    assemblyPackageDependency / assembleArtifact := false
  )
  .dependsOn(`plugin-common`)

lazy val `zio-scenario` = (project in file("scenarios/zio-scenario"))
  .settings(
    scalaVersion := scala3Version,
    Compile / PB.targets := Seq(
      scalapb.gen(grpc = true)          -> (Compile / sourceManaged).value,
      scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value
    ),
    Compile / packageDoc / mappings := Seq(),
    Compile / mainClass             := Some("apm.examples.HelloWorldServer"),
    libraryDependencies ++= Seq(
      "io.d11"               %% "zhttp"                % zioHttp2Version,
      "dev.zio"              %% "zio"                  % zioVersion,
      "io.grpc"               % "grpc-netty"           % "1.50.1",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
    ) ++ Seq(
      "dev.profunktor" %% "redis4cats-effects"  % "1.3.0",
      "dev.profunktor" %% "redis4cats-log4cats" % "1.3.0",
      "dev.profunktor" %% "redis4cats-streams"  % "1.3.0",
      "org.typelevel"  %% "log4cats-slf4j"      % "2.5.0",
      "dev.zio"        %% "zio-interop-cats"    % "23.0.03",
      "ch.qos.logback"  % "logback-classic"     % "1.2.11",
      "dev.zio"        %% "zio-cache"           % zioCacheVersion
    )
  )
  .enablePlugins(JavaAppPackaging, JavaServerAppPackaging)
