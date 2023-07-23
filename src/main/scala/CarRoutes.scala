import cats.effect.Concurrent
import cats.implicits._
import cats.data.Validated._
import doobie.implicits._
import doobie.Transactor
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl._
import org.http4s.dsl.impl._
import java.time._
import scala.util.Try

object CarRoutes {

  private object ManufacturerQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("manufacturer")

  private object ColorQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("color")

  private object YearQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Year]("year")

  implicit val yearQueryParamDecoder: QueryParamDecoder[Year] =
    QueryParamDecoder[Int]
      .emap(i => Try(Year.of(i))
      .toEither
      .leftMap(t => ParseFailure(t.getMessage, t.getMessage)))

  def routes[F[_] : Concurrent](xa: Transactor[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case req @ POST -> Root / "cars" =>
        req.decode[Car]{
          car => {
            val transaction = for {
              cars <- CarRepository.addCar(car)
              stats <- StatRepository.addStatRecord(StatRecord(car.id, Instant.now()))
            } yield cars + stats
            transaction.transact(xa).flatMap(_ => Created())
          }
        }.handleErrorWith(e => BadRequest(e.getMessage))
      case DELETE -> Root / "cars" / id =>
        CarRepository.deleteCarByID(id).transact(xa).flatMap(_ => NoContent()).handleErrorWith(e => BadRequest(e.getMessage))
      case GET -> Root / "cars"
        :? ManufacturerQueryParamMatcher(maybeManufacturer)
        +& ColorQueryParamMatcher(maybeColor)
        +& YearQueryParamMatcher(validatedYear) =>
          validatedYear match {
            case Some(Invalid(_)) => BadRequest("Invalid year format")
            case maybeYear =>
              CarRepository
                .listCarsWithProperties(maybeManufacturer, maybeColor, maybeYear.flatMap(_.toOption))
                .transact(xa)
                .flatMap(cars => Ok(cars))
          }
      case GET -> Root / "cars" / id =>
        CarRepository.getCarByID(id).transact(xa).flatMap(car => Ok(car))
      case GET -> Root / "cars" =>
        CarRepository.listAllCars.transact(xa).flatMap(cars => Ok(cars))
    }
  }
}
