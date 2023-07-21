import cats.effect.{Async, Resource}
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.transactor.Transactor

object Database {
  def transactor[F[_] : Async](databaseConfig: DatabaseConfig): Resource[F, HikariTransactor[F]] = {
    val config = new HikariConfig()
    config.setJdbcUrl(databaseConfig.url)
    config.setUsername(databaseConfig.username)
    config.setPassword(databaseConfig.password)

    HikariTransactor.fromHikariConfig[F](config)
  }

  def initialize[F[_] : Async](xa: Transactor[F]): F[Int] = {
    sql"""
         |CREATE TABLE IF NOT EXISTS cars (
         |  id VARCHAR(100) PRIMARY KEY,
         |  manufacturer VARCHAR(100),
         |  color VARCHAR(100),
         |  releaseYear Int
         |)
       """
      .stripMargin
      .update
      .run
      .transact(xa)
  }
}
