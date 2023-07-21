import doobie.implicits._
import doobie.util.fragment.Fragment

import scala.annotation.tailrec

object CarQueries {
  def createTableQuery = {
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
  }

  def addCarQuery(car: Car) = {
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
  }

  def deleteCarByIDQuery(id: String) = {
    sql"""
         |DELETE FROM cars
         |WHERE id = $id
       """
        .stripMargin
        .update
  }

  def listAllCarsQuery = {
    sql"""
         |SELECT * FROM cars
       """
        .stripMargin
        .query[Car]
  }

  def listCarsWithPropertiesQuery[T](propertyNames: List[String], propertyValues: List[T]) =
    if (propertyNames.isEmpty || propertyValues.isEmpty) listAllCarsQuery
    else  {
      @tailrec
      def makeConditions(conditions: Fragment, nameValuePairs: List[(String, T)]): Fragment = {
        nameValuePairs match {
          case List((name, value)) => conditions ++ fr"$name = ${value.toString}"
          case (name, value) :: tail => makeConditions(conditions ++ fr"$name = ${value.toString} AND", tail)
        }
      }
      val condition =  makeConditions(fr"WHERE", propertyNames.zip(propertyValues))
      (fr"SELECT * FROM cars" ++ condition).query[Car]
  }

}
