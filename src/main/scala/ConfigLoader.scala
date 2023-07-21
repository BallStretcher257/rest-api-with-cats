import cats.effect.Concurrent
import cats.implicits._
import io.circe.config.parser
import io.circe.generic.auto._

case class ServerConfig(address: String, port: Int)

case class DatabaseConfig(url: String, username: String, password: String)

case class Config(serverConfig: ServerConfig, databaseConfig: DatabaseConfig)

object ConfigLoader {
  def load[F[_] : Concurrent](): F[Config] = {
    for {
      serverConfig <- parser.decodePathF[F, ServerConfig]("server")
      databaseConfig <- parser.decodePathF[F, DatabaseConfig] ("database")
    } yield Config(serverConfig, databaseConfig)
  }

}
