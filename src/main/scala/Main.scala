import cats.data.Kleisli
import cats.effect.{Async, Concurrent, ExitCode, IO, IOApp, Resource}
import cats.implicits._
import doobie.util.transactor.Transactor
import org.http4s.implicits._
import org.http4s.server.{Router, Server}
import org.http4s.ember.server._
import org.http4s.{Request, Response}
import com.comcast.ip4s._
object Main extends IOApp {
  private def makeRouter[F[_] : Concurrent](xa: Transactor[F]): Kleisli[F, Request[F], Response[F]] = {
    Router[F](
      "/api" -> (CarRoutes.routes(xa) <+> StatRoutes.routes(xa))
    ).orNotFound
  }
  private def maybeServerResource[F[_] : Async](
    serverConfig: ServerConfig,
    router: Kleisli[F, Request[F], Response[F]]
  ): Option[Resource[F, Server]] = {
    for {
      host <- Host.fromString(serverConfig.host)
      port <- Port.fromInt(serverConfig.port)
    } yield EmberServerBuilder
            .default[F]
            .withHost(host)
            .withPort(port)
            .withHttpApp(router)
            .build
  }

  override def run(args: List[String]): IO[ExitCode] = {
   ConfigLoader.load[IO]().flatMap {
     case Config(serverConfig, databaseConfig) =>
       Database.transactor[IO](databaseConfig).use(
         xa => {
           Database.initialize(xa).flatMap( _ =>
             maybeServerResource(serverConfig, makeRouter(xa)) match {
               case Some(serverResource) => serverResource.useForever.as(ExitCode.Success)
               case None => IO(ExitCode.Error)
             }
           )
         }
       )
   }
  }
}