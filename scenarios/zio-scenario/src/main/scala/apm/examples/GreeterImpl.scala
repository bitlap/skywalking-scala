package apm.examples

import io.grpc.StatusException

import zio.*
import zio.Console.*
import zio.cache.{ Cache, Lookup }

import examples.helloworld.helloworld.{ HelloReply, HelloRequest }
import examples.helloworld.helloworld.ZioHelloworld.Greeter

object GreeterImpl {

  val live = ZLayer.fromZIO(
    for {
      redis <- ZIO.service[RedisService]
      cache <- Cache
        .make(
          100,
          java.time.Duration.ofSeconds(10),
          Lookup[String, Any, Throwable, Option[String]](lookup = id => redis.use(_.get(id)))
        )
        .flatMap(Ref.make(_))
    } yield new GreeterImpl(redis, cache)
  )
}

final case class GreeterImpl(redis: RedisService, cacheRef: Ref[Cache[String, Throwable, Option[String]]])
    extends Greeter {

  def sayHello(
    request: HelloRequest
  ): ZIO[Any, StatusException, HelloReply] =
    for {
      _     <- redis.use(_.set(request.name, request.name)).ignoreLogged
      cache <- cacheRef.get
      value <- cache.get(request.name).orDie
      _     <- printLine(s"Got request: $value").ignoreLogged
      size <- cache.size
      _     <- printLine(s"Cache size: $size").ignoreLogged
    } yield HelloReply(s"Hello, ${request.name}")
}
