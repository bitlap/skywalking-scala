package apm.examples

import io.grpc.StatusException
import zio.*
import zio.Console.*
import zio.cache.{Cache, Lookup}
import zio.stream.ZStream
import examples.helloworld.helloworld.{HelloReply, HelloRequest}
import examples.helloworld.helloworld.ZioHelloworld.Welcomer

object WelcomerImpl {

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
    } yield new WelcomerImpl(redis, cache)
  )
}

final case class WelcomerImpl(redis: RedisService, cacheRef: Ref[Cache[String, Throwable, Option[String]]])
    extends Welcomer {

  def welcome(
    request: HelloRequest
  ): ZStream[Any, StatusException, HelloReply] =
    ZStream
      .fromChunk(Chunk(0 to 10*))
      .mapZIO(i =>
        for {
          // TODO cats effect cross thread by executor
//        _     <- redis.use(_.set(request.name, request.name)).ignoreLogged
          cache <- cacheRef.get
          value <- cache.get(request.name).orDie
          _     <- printLine(s"Got stream request: $value").ignoreLogged
          size  <- cache.size
          _     <- printLine(s"Cache size: $size").ignoreLogged
          _ <- ZIO.blocking(redis.use(_.get("key"))).ignoreLogged
        } yield HelloReply(s"Hello, ${request.name} - $i")
      )
}
