import cats.effect.{IO, Resource}
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.transactor.Transactor

object Database {
  def transactor(databaseConfig: DatabaseConfig): Resource[IO, HikariTransactor[IO]] = {
    val config = new HikariConfig()
    config.setJdbcUrl(databaseConfig.url)
    config.setUsername(databaseConfig.username)
    config.setPassword(databaseConfig.password)

    HikariTransactor.fromHikariConfig[IO](config)
  }

  def initialize(transactorRecourse: Resource[IO, Transactor[IO]]): IO[Int] = {
    transactorRecourse.use{
      xa => CarQueries.createTableQuery.run.transact(xa)
    }
  }
}
