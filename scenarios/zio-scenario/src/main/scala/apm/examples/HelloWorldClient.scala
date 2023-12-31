package apm.examples

import scalapb.zio_grpc.ZManagedChannel

import io.grpc.ManagedChannelBuilder

import zio.*
import zio.Console.*

import examples.helloworld.helloworld.HelloRequest
import examples.helloworld.helloworld.ZioHelloworld.{ GreeterClient, WelcomerClient }
import zhttp.http.*
import zhttp.service.Server

object HelloWorldClient extends zio.ZIOAppDefault {

  val greeterLayer: Layer[Throwable, GreeterClient] =
    GreeterClient.live(
      ZManagedChannel(
        ManagedChannelBuilder.forAddress("localhost", 9000).usePlaintext()
      )
    )

  val welcomerLayer: Layer[Throwable, WelcomerClient] =
    WelcomerClient.live(
      ZManagedChannel(
        ManagedChannelBuilder.forAddress("localhost", 9000).usePlaintext()
      )
    )

  val app: HttpApp[GreeterClient & WelcomerClient & RedisService, Throwable] = Http.collectHttp[Request] {
    case Method.GET -> !! / "hello" =>
      Http.fromFunctionZIO[Request] { _ =>
        for {
          r1 <- GreeterClient.sayHello(HelloRequest("World"))
          r2 <- WelcomerClient.welcome(HelloRequest("World")).runCollect
          _  <- printLine(r1.message)
          _  <- printLine(r2.toList.map(_.message).mkString)
          _  <- ZIO.serviceWithZIO[RedisService](_.use(_.set(r1.message, r1.message)))
        } yield Response.text(r1.message)
      }

  }

  final def run =
    (for {
      _ <- Server.start(8090, app)
      _ <- ZIO.never
    } yield ()).provide(greeterLayer, welcomerLayer, RedisService.live).exitCode
}
