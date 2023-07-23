import doobie.implicits._
import doobie.postgres.implicits._
object StatRepository {
  def addStatRecord(statRecord: StatRecord): doobie.ConnectionIO[Int] = {
    sql"""
         |INSERT INTO statistics (
         |  id, added_at
         |)
         |VALUES (
         |  ${statRecord.id},
         |  ${statRecord.addedAt}
         |)
         |"""
      .stripMargin
      .update
      .run
  }

  def getStats: doobie.ConnectionIO[Stats] = {
    sql"""
         |SELECT count(id), min(added_at), max(added_at)
         |FROM statistics
       """
      .stripMargin
      .query[Stats]
      .unique
  }

}
