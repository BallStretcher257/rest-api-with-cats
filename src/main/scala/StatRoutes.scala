import cats.effect.Concurrent
import cats.implicits._
import doobie.implicits._
import doobie.Transactor
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object StatRoutes {
  def routes[F[_] : Concurrent](xa: Transactor[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F]{
      case GET -> Root / "stats" =>
        StatRepository.getStats.transact(xa).flatMap(stats => Ok(stats))
    }
  }
}
