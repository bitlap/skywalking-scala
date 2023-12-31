package apm.examples

import examples.helloworld.helloworld.HelloRequest
import examples.helloworld.helloworld.ZioHelloworld.GreeterClient
import io.grpc.ManagedChannelBuilder
import scalapb.zio_grpc.ZManagedChannel
import zhttp.http.*
import zhttp.service.Server
import zio.*
import zio.Console.*

object HelloWorldClient extends zio.ZIOAppDefault {

  val clientLayer: Layer[Throwable, GreeterClient] =
    GreeterClient.live(
      ZManagedChannel(
        ManagedChannelBuilder.forAddress("localhost", 9000).usePlaintext()
      )
    )

  val app: HttpApp[GreeterClient & RedisService, Throwable] = Http.collectHttp[Request] {
    case Method.GET -> !! / "hello" =>
      Http.fromFunctionZIO[Request] { _ =>
        for {
          _ <- ZIO.serviceWithZIO[RedisService](_.use(_.set("key", "value")))
          r <- GreeterClient.sayHello(HelloRequest("World"))
          _ <- printLine(r.message)
          _ <- ZIO.serviceWithZIO[RedisService](_.use(_.get("key")))
        } yield Response.text(r.message)
      }

  }

  final def run =
    (for {
      _ <- Server.start(8090, app)
      _ <- ZIO.never
    } yield ()).provide(clientLayer, RedisService.live).exitCode
}
