import cats.data.Kleisli
import cats.effect.unsafe.implicits.global
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import doobie.util.transactor.Transactor
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.ember.server._
import org.http4s.{Request, Response}
import com.comcast.ip4s._
object Main extends IOApp {
  def makeRouter(transactorResource: Resource[IO, Transactor[IO]]): Kleisli[IO, Request[IO], Response[IO]] = {
    Router[IO](
      "/api" -> CarRoutes.routes(new QueryRunner(transactorResource))
    ).orNotFound
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val Config(serverConfig, databaseConfig) = ConfigLoader.load().unsafeRunSync()
    Database.initialize(Database.transactor(databaseConfig)).unsafeRunSync()
    EmberServerBuilder
      .default[IO]
      .withHost(Host.fromString(serverConfig.address).get)
      .withPort(Port.fromInt(serverConfig.port).get)
      .withHttpApp(makeRouter(Database.transactor(databaseConfig)))
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}