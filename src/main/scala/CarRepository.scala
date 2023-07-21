import cats.effect.Concurrent
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.fragment.Fragment

import scala.annotation.tailrec
class CarRepository[F[_] : Concurrent](xa: Transactor[F]) {
  def addCar(car: Car): F[Int] = {
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
      .transact(xa)
  }
  def deleteCarByID(id: String): F[Int] = {
    sql"""
         |DELETE FROM cars
         |WHERE id = $id
       """
      .stripMargin
      .update
      .run
      .transact(xa)
  }
  def listAllCars: F[List[Car]] = {
    sql"""
         |SELECT * FROM cars
       """
      .stripMargin
      .query[Car]
      .to[List]
      .transact(xa)
  }
  def listCarsWithProperties[T](propertyNames: List[String], propertyValues: List[T]): F[List[Car]] = {
    if (propertyNames.isEmpty || propertyValues.isEmpty) listAllCars
    else {
      @tailrec
      def makeConditions(conditions: Fragment, nameValuePairs: List[(String, T)]): Fragment = {
        nameValuePairs match {
          case List((name, value)) => conditions ++ fr"$name = ${value.toString}"
          case (name, value) :: tail => makeConditions(conditions ++ fr"$name = ${value.toString} AND", tail)
        }
      }
      val condition = makeConditions(fr"WHERE", propertyNames.zip(propertyValues))
      (fr"SELECT * FROM cars" ++ condition)
        .query[Car]
        .to[List]
        .transact(xa)
    }
  }
}
