import cats.Monad
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

  def initialize[F[_] : Async : Monad](xa: Transactor[F]): F[Int] = {
    val transaction = for {
      cars <- sql"""
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
      stats <- sql"""
             | CREATE TABLE IF NOT EXISTS statistics (
             |  id VARCHAR(100) PRIMARY KEY,
             |  added_at timestamp,
             |  FOREIGN KEY (id)
             |    REFERENCES cars (id)
             |    ON DELETE CASCADE
             | )
         """
          .stripMargin
          .update
          .run
    } yield cars + stats

    transaction.transact(xa)
  }
}
