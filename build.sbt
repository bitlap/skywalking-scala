ThisBuild / resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Releases" at "https://s01.oss.sonatype.org/content/repositories/releases"
)

lazy val scala3Version         = "3.2.2"
lazy val scalatestVersion      = "3.2.15"
lazy val junitVersion          = "4.12"
lazy val mockitoVersion        = "5.0.0"
lazy val junitInterfaceVersion = "0.12"

lazy val skywalkingVersion = "8.16.0"

lazy val calibanVersion     = "2.0.1"
lazy val zioGrpcVersion     = "0.6.0-rc5"
lazy val zioGrpcTestVersion = "0.6.0-test1"
lazy val zio200Version      = "2.0.0"
lazy val zio203Version      = "2.0.3"
lazy val zioHttp2Version    = "2.0.0-RC10"
lazy val catsEffectVersion  = "3.4.1"

inThisBuild(
  List(
    scalaVersion := scala3Version,
    organization := "org.bitlap",
    homepage     := Some(url("https://github.com/bitlap/skywalking-scala")),
    licenses     := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        id = "jxnu-liguobin",
        name = "梦境迷离",
        email = "dreamylost@outlook.com",
        url = url("https://blog.dreamylost.cn")
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
    crossPaths         := false,
    Test / testOptions := Seq(Tests.Argument(TestFrameworks.JUnit, "-a"))
  )

lazy val `skywalking-scala` = (project in file("."))
  .aggregate(
    `caliban-v2x-plugin`,
    `ziogrpc-v06rcx-plugin`,
    `ziogrpc-v06testx-plugin`,
    `zio-v200-plugin`,
    `zio-v203-plugin`,
    `plugin-common`,
    `ziohttp-v2x-plugin`,
    `ziogrpc-plugin-common`,
    `zio-plugin-common`,
    `cats-effect-v3x-plugin`
  )
  .settings(
    publish / skip := true,
    commonSettings,
    commands ++= Commands.value
  )

lazy val `caliban-v2x-plugin` = (project in file("caliban-v2x-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                       := "caliban-v2x-plugin",
    assembly / assemblyJarName := s"apm-caliban-v2x-plugin-${(ThisBuild / version).value}.jar",
    libraryDependencies ++= Seq(
      "com.github.ghostdogpr" %% "caliban" % calibanVersion % Provided
    )
  )
  .dependsOn(`zio-plugin-common` % "compile->compile;provided->provided")

lazy val `ziogrpc-plugin-common` = (project in file("ziogrpc-plugin-common"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name := "ziogrpc-plugin-common",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-core" % zioGrpcTestVersion % Provided
    )
  )
  .dependsOn(`zio-plugin-common` % "compile->compile;provided->provided")

lazy val `zio-plugin-common` = (project in file("zio-plugin-common"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name := "zio-plugin-common",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zio200Version % Provided
    )
  )
  .dependsOn(`plugin-common` % "compile->compile;provided->provided")

lazy val `ziogrpc-v06rcx-plugin` = (project in file("ziogrpc-v06rcx-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                       := "ziogrpc-v06rcx-plugin",
    assembly / assemblyJarName := s"apm-ziogrpc-v06rcx-plugin-${(ThisBuild / version).value}.jar",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-core" % zioGrpcVersion % Provided
    )
  )
  .dependsOn(`ziogrpc-plugin-common` % "compile->compile;provided->provided")

lazy val `ziogrpc-v06testx-plugin` = (project in file("ziogrpc-v06testx-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                       := "ziogrpc-v06testx-plugin",
    assembly / assemblyJarName := s"apm-ziogrpc-v06testx-plugin-${(ThisBuild / version).value}.jar",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-core" % zioGrpcTestVersion % Provided
    )
  )
  .dependsOn(`ziogrpc-plugin-common` % "compile->compile;provided->provided")

lazy val `plugin-common` = (project in file("plugin-common"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name := "plugin-common"
  )

lazy val `zio-v200-plugin` = (project in file("zio-v200-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                       := "zio-v200-plugin",
    assembly / assemblyJarName := s"apm-zio-v200-plugin-${(ThisBuild / version).value}.jar",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zio200Version % Provided
    )
  )
  .dependsOn(`zio-plugin-common` % "compile->compile;provided->provided")

lazy val `cats-effect-v3x-plugin` = (project in file("cats-effect-v3x-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                       := "cats-effect-v3x-plugin",
    assembly / assemblyJarName := s"apm-cats-effect-v3x-plugin-${(ThisBuild / version).value}.jar",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion % Provided
    )
  )
  .dependsOn(`plugin-common` % "compile->compile;provided->provided")

lazy val `zio-v203-plugin` = (project in file("zio-v203-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                       := "zio-v203-plugin",
    assembly / assemblyJarName := s"apm-zio-v203-plugin-${(ThisBuild / version).value}.jar",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zio203Version % Provided
    )
  )
  .dependsOn(`zio-plugin-common` % "compile->compile;provided->provided")

lazy val `ziohttp-v2x-plugin` = (project in file("ziohttp-v2x-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name                       := "ziohttp-v2x-plugin",
    assembly / assemblyJarName := s"apm-ziohttp-v2x-plugin-${(ThisBuild / version).value}.jar",
    libraryDependencies ++= Seq(
      "io.d11" %% "zhttp" % zioHttp2Version % Provided
    )
  )
  .dependsOn(`zio-plugin-common` % "compile->compile;provided->provided")
