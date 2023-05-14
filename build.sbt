ThisBuild / resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Releases" at "https://s01.oss.sonatype.org/content/repositories/releases"
)

lazy val scala3Version     = "3.2.0"
lazy val scalatestVersion  = "3.2.15"
lazy val calibanVersion    = "2.0.1"
lazy val skywalkingVersion = "8.13.0"
lazy val junitVersion      = "4.12"
lazy val mockitoVersion    = "5.0.0"
lazy val zioGrpcVersion    = "0.6.0-test4"
lazy val zioVersion        = "2.0.0"

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
      "org.scalatest"        %% "scalatest"      % scalatestVersion  % Test,
      "junit"                 % "junit"          % junitVersion      % Test,
      "org.mockito"           % "mockito-core"   % mockitoVersion    % Test,
      "org.apache.skywalking" % "apm-test-tools" % skywalkingVersion % Test,
      "org.apache.skywalking" % "apm-agent-core" % skywalkingVersion % Provided
    )
  )

lazy val `skywalking-scala` = (project in file("."))
  .aggregate(
    `caliban-v2x-plugin`,
    `ziogrpc-v06x-plugin`,
    `plugin-common`
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
  .dependsOn(`plugin-common`)

lazy val `ziogrpc-v06x-plugin` = (project in file("ziogrpc-v06x-plugin"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    assembly / assemblyJarName := s"apm-ziogrpc-v06x-plugin-${(ThisBuild / version).value}.jar",
    name                       := "ziogrpc-v06x-plugin",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-core" % zioGrpcVersion % Provided
    )
  )
  .dependsOn(`plugin-common`)

lazy val `plugin-common` = (project in file("plugin-common"))
  .settings(
    commonSettings,
    commands ++= Commands.value,
    name := "plugin-common",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion % Provided
    )
  )
