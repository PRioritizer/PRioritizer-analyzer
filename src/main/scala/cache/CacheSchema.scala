package cache

import java.sql.Date
import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.lifted.ProvenShape._

object CacheSchema {
  // Table definition
  class PairCache(tag: Tag) extends Table[CachedPullRequestPair](tag, PairCache.tableName) {
    def date = column[Date]("date")
    def shaOne = column[String]("sha_one")
    def shaTwo = column[String]("sha_two")
    def mergeable = column[Boolean]("mergable")

    def * = (date, shaOne, shaTwo, mergeable) <> (CachedPullRequestPair.tupled, CachedPullRequestPair.unapply)

    def pk = primaryKey("sha", (shaOne, shaTwo))
  }

  object PairCache {
    val tableName = "pair_cache"
  }
}
