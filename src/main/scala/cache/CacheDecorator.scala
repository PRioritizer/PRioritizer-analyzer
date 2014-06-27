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
  implicit lazy val session = Db
  lazy val pulls = TableQuery[PullRequestCache]
  lazy val insertPull = pulls.insertInvoker
  lazy val getPullsByKey = for {
    sha <- Parameters[String]
    p <- pulls if p.sha === sha
  } yield p

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
        case Some(cachedPull) if !cachedPull.represents(pullRequest) => insert(pullRequest)
        case None => insert(pullRequest)
        case _ => // Cache already up-to-date
      }

    pullRequest
  }

  private def get(pullRequest: PullRequest): Option[CachedPullRequest] = {
    getPullsByKey(pullRequest.sha).firstOption
  }

  private def insert(pullRequest: PullRequest): Unit = {
    insertPull.insertOrUpdate(CachedPullRequest(pullRequest))
  }

  def init(): Unit = {
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
