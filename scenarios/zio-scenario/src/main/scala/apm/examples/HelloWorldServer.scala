package apm.examples

import scalapb.zio_grpc.*

import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService

import zio.*

import examples.helloworld.helloworld.ZioHelloworld.Greeter

object HelloWorldServer extends ZIOAppDefault {

  private lazy val grpcApp = ServerLayer.fromServiceList(
    ServerBuilder.forPort(9000).addService(ProtoReflectionService.newInstance()),
    ServiceList.addFromEnvironment[Greeter]
  )

  override val run: ZIO[Any, Any, Unit] = (for {
    _ <- grpcApp.launch.exitCode
    _ <- ZIO.never
  } yield {}).provide(
    RedisService.live,
    GreeterImpl.live
  )
}
