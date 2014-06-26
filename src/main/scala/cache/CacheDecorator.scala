package cache

import git._
import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.MTable
import cache.CacheSchema.PullRequestCache

/**
 * An info getter implementation that read/writes from the cache.
 * @param provider The cache provider.
 */
class CacheDecorator(base: PullRequestList, val provider: CacheProvider, mode: CacheMode.CacheMode) extends PullRequestDecorator(base) {
  val dbDriver = "org.sqlite.JDBC"
  lazy val dbUrl = s"jdbc:sqlite:${provider.defaultDbPath}"
  lazy val Db = Database.forURL(dbUrl, driver = dbDriver).createSession()
  lazy val pulls = TableQuery[PullRequestCache]

  init()

  override def decorate(pullRequest: PullRequest): PullRequest = {
    val cachedPullOption = get(pullRequest)
    if (mode == CacheMode.Read)
      cachedPullOption match {
        case Some(cachedPull) if mode == CacheMode.Read => cachedPull.fill(pullRequest)
        case _ =>
      }
    else if (mode == CacheMode.Write)
      cachedPullOption match {
        case Some(cachedPull) if !cachedPull.represents(pullRequest) =>
          insert(pullRequest)
        case None =>
          insert(pullRequest)
        case _ => // Cache already up-to-date
      }

    pullRequest
  }

  private def get(pullRequest: PullRequest): Option[CachedPullRequest] = {
    implicit val session = Db
    pulls.filter(p => p.sha === pullRequest.sha).firstOption
  }

  private def insert(pullRequest: PullRequest): Unit = {
    implicit val session = Db

    // Delete old record(s)
    val query = pulls.filter(p => p.sha === pullRequest.sha)
    query.delete

    // Insert
    pulls += CachedPullRequest(pullRequest)
  }

  def init(): Unit = {
    implicit val session = Db

    // Create table
    if (MTable.getTables(PullRequestCache.tableName).list.isEmpty)
      pulls.ddl.create
  }

  def dispose(): Unit = {
    try {
      Db.close()
    } catch {
      case _ : Exception =>
    }
  }
}
