package apm.examples

import examples.helloworld.helloworld.ZioHelloworld.Greeter
import examples.helloworld.helloworld.{HelloReply, HelloRequest}
import io.grpc.StatusException
import zio.*
import zio.Console.*

object GreeterImpl extends Greeter {
  def sayHello(
                request: HelloRequest
              ): ZIO[Any, StatusException, HelloReply] =
    printLine(s"Got request: $request").orDie.as(HelloReply(s"Hello, ${request.name}"))
}
