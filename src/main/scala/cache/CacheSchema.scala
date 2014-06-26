package cache

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.lifted.ProvenShape._

object CacheSchema {
  // Table definition
  class PairCache(tag: Tag) extends Table[CachedPullRequestPair](tag, PairCache.tableName) {
    def shaOne = column[String]("sha_one")
    def shaTwo = column[String]("sha_two")
    def isMergeable = column[Boolean]("is_mergeable")

    def * = (shaOne, shaTwo, isMergeable) <> (CachedPullRequestPair.tupled, CachedPullRequestPair.unapply)

    def pk = primaryKey("sha", (shaOne, shaTwo))
  }

  // Table definition
  class PullRequestCache(tag: Tag) extends Table[CachedPullRequest](tag, PullRequestCache.tableName) {
    def sha = column[String]("sha", O.PrimaryKey)
    def isMergeable = column[Boolean]("is_mergeable")
    def linesAdded = column[Long]("lines_added")
    def linesDeleted = column[Long]("lines_deleted")
    def filesChanged = column[Long]("files_changed")
    def commits = column[Long]("commits")

    def * = (sha, isMergeable, linesAdded, linesDeleted, filesChanged, commits) <> (CachedPullRequest.tupled, CachedPullRequest.unapply)
  }

  object PairCache {
    val tableName = "pair_cache"
  }

  object PullRequestCache {
    val tableName = "pr_cache"
  }
}
