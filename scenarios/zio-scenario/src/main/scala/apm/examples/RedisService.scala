package apm.examples

import cats.effect.*
import cats.effect.unsafe.implicits.global
import dev.profunktor.redis4cats.*
import dev.profunktor.redis4cats.connection.*
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.log4cats.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import zio.interop.catz.*
import zio.{Duration, IO as _, *}

final class RedisService(cmd: Resource[IO, RedisCommands[IO, String, String]])(implicit logger: Logger[IO]):

  def use[B](operate: RedisCommands[IO, String, String] => IO[B]): Task[B] =
    LiftIO.liftK[Task].apply(cmd.use(operate))

object RedisService:

  implicit val logger: Logger[IO]             = Slf4jLogger.getLogger[IO]
  val stringCodec: RedisCodec[String, String] = RedisCodec.Utf8

  lazy val live: ZLayer[Any, Nothing, RedisService] =
    ZLayer.fromZIO {
      for
        url <- ZIO.succeed("redis://127.0.0.1:6379")
        redisCommand =
          for
            uri    <- Resource.eval(RedisURI.make[IO](url))
            client <- RedisClient[IO].fromUri(uri)
            cmd    <- Redis[IO].fromClient(client, stringCodec)
          yield cmd
      yield RedisService(redisCommand)
    }
