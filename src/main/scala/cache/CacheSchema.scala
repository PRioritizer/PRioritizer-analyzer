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

  object ColumnNames {
    val pairs = List("sha_one", "sha_two", "is_mergeable")
    val pullRequests = List("sha", "lines_added", "lines_deleted", "files_changed", "commits", "has_test_code")
  }

  class PairCache(tag: Tag) extends Table[CachedPullRequestPair](tag, TableNames.pairs) {
    def shaOne = column[String]("sha_one", O.Length(40, varying = false))
    def shaTwo = column[String]("sha_two", O.Length(40, varying = false))
    def isMergeable = column[Boolean]("is_mergeable")

    def * = (shaOne, shaTwo, isMergeable) <> (CachedPullRequestPair.tupled, CachedPullRequestPair.unapply)

    def pk = primaryKey("sha", (shaOne, shaTwo))
  }

  class PullRequestCache(tag: Tag) extends Table[CachedPullRequest](tag, TableNames.pullRequests) {
    def sha = column[String]("sha", O.PrimaryKey, O.Length(40, varying = false))
    def linesAdded = column[Long]("lines_added")
    def linesDeleted = column[Long]("lines_deleted")
    def filesChanged = column[Long]("files_changed")
    def commits = column[Long]("commits")
    def hasTestCode = column[Boolean]("has_test_code")

    def * = (sha, linesAdded, linesDeleted, filesChanged, commits, hasTestCode) <> (CachedPullRequest.tupled, CachedPullRequest.unapply)
  }
}
