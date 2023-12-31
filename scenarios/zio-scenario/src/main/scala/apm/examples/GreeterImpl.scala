package apm.examples

import io.grpc.StatusException

import zio.*
import zio.Console.*

import examples.helloworld.helloworld.{ HelloReply, HelloRequest }
import examples.helloworld.helloworld.ZioHelloworld.Greeter

object GreeterImpl {
  val live = ZLayer.fromFunction(GreeterImpl.apply)
}

final case class GreeterImpl(redis: RedisService) extends Greeter {

  def sayHello(
    request: HelloRequest
  ): ZIO[Any, StatusException, HelloReply] =
    for {
      _ <- redis.use(_.set("key", "value")).ignoreLogged
      _ <- printLine(s"Got request: $request").orDie
      _ <- redis.use(_.get("key")).ignoreLogged
    } yield HelloReply(s"Hello, ${request.name}")
}
