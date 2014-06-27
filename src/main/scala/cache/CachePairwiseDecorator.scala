package cache

import git._
import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.MTable
import cache.CacheSchema.PairCache

/**
 * An info getter implementation for the JGit library.
 * @param provider The JGit provider.
 */
class CachePairwiseDecorator(base: PairwiseList, provider: CacheProvider, mode: CacheMode.CacheMode) extends PairwiseDecorator(base) {
  val dbDriver = "org.sqlite.JDBC"
  lazy val dbUrl = s"jdbc:sqlite:${provider.defaultDbPath}"
  lazy val Db = Database.forURL(dbUrl, driver = dbDriver).createSession()
  implicit lazy val session = Db
  lazy val pairs = TableQuery[PairCache]
  lazy val getPairsByKey = for {
    (shaOne, shaTwo) <- Parameters[(String, String)]
    p <- pairs if p.shaOne === shaOne && p.shaTwo === shaTwo
  } yield p

  init()

  override def decorate(pair: PullRequestPair): PullRequestPair = {
    val cachedPairOption = get(pair)
    if (mode == CacheMode.Read)
      cachedPairOption match {
        case Some(cachedPair) if mode == CacheMode.Read => cachedPair.fill(pair)
        case _ =>
      }
    else if (mode == CacheMode.Write)
      cachedPairOption match {
        case Some(cachedPair) if !cachedPair.represents(pair) => insert(pair)
        case None => insert(pair)
        case _ => // Cache already up-to-date
      }

    pair
  }

  private def get(pair: PullRequestPair): Option[CachedPullRequestPair] = {
    val key = CachedPullRequestPair(pair)
    getPairsByKey(key.shaOne, key.shaTwo).firstOption
  }

  private def insert(pair: PullRequestPair): Unit = {
    pairs.insertOrUpdate(CachedPullRequestPair(pair))
  }

  def init(): Unit = {
    // Create table
    if (MTable.getTables(PairCache.tableName).list.isEmpty)
      pairs.ddl.create
  }

  def dispose(): Unit = {
    try {
      Db.close()
    } catch {
      case _ : Exception =>
    }
  }
}
