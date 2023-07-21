import cats.effect.Concurrent
import cats.implicits._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object CarRoutes {
  def routes[F[_] : Concurrent](queryRunner: CarRepository[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "cars" =>
        req.decode[Car]{
          car => queryRunner.addCar(car).flatMap(id => Created(id))
        }.handleErrorWith(e => BadRequest(e.getMessage))
      case GET -> Root / "cars" =>
        queryRunner.listAllCars.flatMap(cars => Ok(cars))
      case DELETE -> Root / "cars" / id =>
        queryRunner.deleteCarByID(id).flatMap(_ => NoContent()).handleErrorWith(e => BadRequest(e.getMessage))
    }
  }
}
