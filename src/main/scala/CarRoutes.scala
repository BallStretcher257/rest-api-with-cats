import cats.effect.IO
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object CarRoutes {
  def routes(queryRunner: QueryRunner): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] {
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
