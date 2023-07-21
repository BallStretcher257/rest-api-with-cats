import cats.effect.IO
import io.circe.config.parser
import io.circe.generic.auto._

case class ServerConfig(address: String, port: Int)

case class DatabaseConfig(url: String, username: String, password: String)

case class Config(serverConfig: ServerConfig, databaseConfig: DatabaseConfig)

object ConfigLoader {
  def load(): IO[Config] = {
    for {
      serverConfig <- parser.decodePathF[IO, ServerConfig]("server")
      databaseConfig <- parser.decodePathF[IO, DatabaseConfig] ("database")
    } yield Config(serverConfig, databaseConfig)
  }

}
