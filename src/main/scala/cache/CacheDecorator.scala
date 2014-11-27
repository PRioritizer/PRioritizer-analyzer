package cache

import cache.CacheSchema.{TableNames, Tables}
import cache.models.{EmptyPairwiseList, CachedPullRequest}
import git._

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.MTable

/**
 * An info getter implementation that read/writes from the cache.
 * @param provider The cache provider.
 */
class CacheDecorator(base: PullRequestList, val provider: CacheProvider) extends PullRequestDecorator(base) {
  implicit lazy val session = provider.Db
  lazy val mode = provider.mode
  lazy val pairCache = provider.getPairwiseDecorator(new EmptyPairwiseList).asInstanceOf[CachePairwiseDecorator]
  lazy val insertPull = Tables.pullRequests.insertInvoker
  lazy val getPullsByKey = for {
    sha <- Parameters[String]
    p <- Tables.pullRequests
    if p.sha === sha
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

    // Retrieve or write merge status
    val tmpPair = PullRequestPair(pullRequest, PullRequest(0, "author", pullRequest.shaTarget, "shaTarget", "source", "target"), pullRequest.isMergeable)
    val pair = pairCache.decorate(tmpPair)
    pullRequest.isMergeable = pair.isMergeable

    pullRequest
  }

  private def get(pullRequest: PullRequest): Option[CachedPullRequest] = {
    getPullsByKey(pullRequest.sha).firstOption
  }

  private def insert(pullRequest: PullRequest): Unit = {
    insertPull.insertOrUpdate(CachedPullRequest(pullRequest))
  }

  def init(): Unit = {
    val table = MTable.getTables(TableNames.pullRequests).list.headOption

    // (Re)create table
    table match {
      case Some(t) => if (!t.getColumns.list.map(c => c.name).sameElements(CacheSchema.ColumnNames.pullRequests)) {
          Tables.pullRequests.ddl.drop
          Tables.pullRequests.ddl.create
        }
      case None => Tables.pullRequests.ddl.create
    }

    pairCache.init()
  }
}
