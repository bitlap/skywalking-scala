package apm.examples

import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import scalapb.zio_grpc.*
import zio.*

object HelloWorldServer extends ServerMain {

  private lazy val grpcApp = ServerLayer.fromServiceList(
    ServerBuilder.forPort(port).addService(ProtoReflectionService.newInstance()),
    ServiceList.add(GreeterImpl)
  )

  def services: ServiceList[Any] = ServiceList.add(GreeterImpl)

  override val run = for {
    _ <- grpcApp.launch.exitCode
    _ <- ZIO.never
  } yield {}
}
