import cats.effect.{IO, Resource}
import doobie.implicits._
import doobie.util.transactor.Transactor
class QueryRunner(transactorRecourse: Resource[IO, Transactor[IO]]) {
  def addCar(car: Car) = {
    transactorRecourse.use {
      xa => CarQueries.addCarQuery(car).run.transact(xa)
    }
  }
  def deleteCarByID(id: String) = {
    transactorRecourse.use {
      xa => CarQueries.deleteCarByIDQuery(id).run.transact(xa)
    }
  }
  def listAllCars = {
    transactorRecourse.use {
      xa => CarQueries.listAllCarsQuery.to[List].transact(xa)
    }
  }
  def listCarsWithProperties[T](propertyNames: List[String], propertyValues: List[T]) = {
    transactorRecourse.use {
      xa => CarQueries.listCarsWithPropertiesQuery(propertyNames, propertyValues).to[List].transact(xa)
    }
  }
}
