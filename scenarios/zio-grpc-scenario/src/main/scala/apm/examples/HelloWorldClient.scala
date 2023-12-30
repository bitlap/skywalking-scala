package apm.examples

import scalapb.zio_grpc.ZManagedChannel

import io.grpc.ManagedChannelBuilder

import zio.*
import zio.Console.*

import examples.helloworld.helloworld.HelloRequest
import examples.helloworld.helloworld.ZioHelloworld.GreeterClient
import zhttp.http.*
import zhttp.service.Server

object HelloWorldClient extends zio.ZIOAppDefault {

  val clientLayer: Layer[Throwable, GreeterClient] =
    GreeterClient.live(
      ZManagedChannel(
        ManagedChannelBuilder.forAddress("localhost", 9000).usePlaintext()
      )
    )

  val app: HttpApp[Any, Throwable] = Http.collectHttp[Request] { case Method.GET -> !! / "hello" =>
    Http.fromFunctionZIO[Request] { _ =>
      for {
        r <- GreeterClient.sayHello(HelloRequest("World")).provide(clientLayer)
        _ <- printLine(r.message)
      } yield Response.text(r.message)
    }

  }

  def myAppLogic =
    Server.start(8090, app)

  final def run =
    myAppLogic.provideLayer(clientLayer).exitCode
}
