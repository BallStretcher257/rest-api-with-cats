import cats.data.Kleisli
import cats.effect.{Concurrent, ExitCode, IO, IOApp}
import doobie.util.transactor.Transactor
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.ember.server._
import org.http4s.{Request, Response}
import com.comcast.ip4s._
object Main extends IOApp {
  private def makeRouter[F[_] : Concurrent](xa: Transactor[F]): Kleisli[F, Request[F], Response[F]] = {
    Router[F](
      "/api" -> CarRoutes.routes(new CarRepository(xa))
    ).orNotFound
  }

  override def run(args: List[String]): IO[ExitCode] = {
   ConfigLoader.load[IO]().flatMap {
     case Config(serverConfig, databaseConfig) =>
       Database.transactor[IO](databaseConfig).use(
         xa => {
           Database.initialize(xa)
           EmberServerBuilder
             .default[IO]
             .withHost(Host.fromString(serverConfig.address).get)
             .withPort(Port.fromInt(serverConfig.port).get)
             .withHttpApp(makeRouter(xa))
             .build
             .useForever
             .as(ExitCode.Success)
         }
       )
   }
  }
}