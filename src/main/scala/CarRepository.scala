import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.fragment.Fragment
import doobie.util.meta.Meta

import java.time.Year
import scala.annotation.tailrec
object CarRepository {
  implicit val yearMeta: Meta[Year] = Meta[Int].timap(Year.of)(_.getValue)
  def addCar(car: Car): doobie.ConnectionIO[Int] = {
    sql"""
         |INSERT INTO cars (
         |  id,
         |  manufacturer,
         |  color,
         |  releaseYear
         |)
         |VALUES (
         |  ${car.id},
         |  ${car.manufacturer},
         |  ${car.color},
         |  ${car.releaseYear}
         |)
    """
      .stripMargin
      .update
      .run
  }
  def deleteCarByID(id: String): doobie.ConnectionIO[Int] = {
    sql"""
         |DELETE FROM cars
         |WHERE id = $id
       """
      .stripMargin
      .update
      .run
  }
  def listAllCars: doobie.ConnectionIO[List[Car]] = {
    sql"""
         |SELECT * FROM cars
       """
      .stripMargin
      .query[Car]
      .to[List]
  }

  def getCarByID(id: String): doobie.ConnectionIO[Car] = {
    sql"""
         |SELECT * FROM cars
         |WHERE id = $id
       """
      .stripMargin
      .query[Car]
      .unique
  }
  def listCarsWithProperties(
      maybeManufacturer: Option[String],
      maybeColor: Option[String],
      maybeYear: Option[Year]
    ): doobie.ConnectionIO[List[Car]] = {
    val maybeManufacturerCondition = maybeManufacturer.map(manufacturer => fr"manufacturer = $manufacturer")
    val maybeColorCondition = maybeColor.map(color => fr"color = $color")
    val maybeYearCondition = maybeYear.map(year => fr"releaseYear = $year")
    @tailrec
    def combineConditions(result: Fragment, conditions: List[Option[Fragment]], empty: Boolean): Option[Fragment] = {
      conditions match {
        case Some(fragment) :: tail =>
          combineConditions(result ++ (if (empty) fr0"" else fr"AND") ++ fragment, tail, empty = false)
        case None :: tail =>
          combineConditions(result, tail, empty)
        case Nil => if (empty) None else Some(result)
      }
    }
    val maybeConditions = combineConditions(fr0"",
      List(maybeManufacturerCondition,
        maybeColorCondition,
        maybeYearCondition
      ),
      empty = true
    )
    maybeConditions match {
      case None => listAllCars
      case Some(conditions) =>
        sql"""
             |SELECT * FROM cars
             |WHERE $conditions
            """
          .stripMargin
          .query[Car]
          .to[List]
    }
  }
}
