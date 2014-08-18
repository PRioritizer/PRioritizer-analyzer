package cache

import cache.models.{CachedPullRequest, CachedPullRequestPair}

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.lifted.ProvenShape._

object CacheSchema {
  object Tables {
    lazy val pairs = TableQuery[PairCache]
    lazy val pullRequests = TableQuery[PullRequestCache]
  }

  object TableNames {
    val pairs = "pair_cache"
    val pullRequests = "pr_cache"
  }

  class PairCache(tag: Tag) extends Table[CachedPullRequestPair](tag, TableNames.pairs) {
    def shaOne = column[String]("sha_one")
    def shaTwo = column[String]("sha_two")
    def isMergeable = column[Boolean]("is_mergeable")

    def * = (shaOne, shaTwo, isMergeable) <> (CachedPullRequestPair.tupled, CachedPullRequestPair.unapply)

    def pk = primaryKey("sha", (shaOne, shaTwo))
  }

  class PullRequestCache(tag: Tag) extends Table[CachedPullRequest](tag, TableNames.pullRequests) {
    def sha = column[String]("sha", O.PrimaryKey)
    def isMergeable = column[Boolean]("is_mergeable")
    def linesAdded = column[Long]("lines_added")
    def linesDeleted = column[Long]("lines_deleted")
    def filesChanged = column[Long]("files_changed")
    def commits = column[Long]("commits")

    def * = (sha, isMergeable, linesAdded, linesDeleted, filesChanged, commits) <> (CachedPullRequest.tupled, CachedPullRequest.unapply)
  }
}
