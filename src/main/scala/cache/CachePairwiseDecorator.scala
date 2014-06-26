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
  lazy val pairs = TableQuery[PairCache]

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
        case Some(cachedPair) if !cachedPair.represents(pair) =>
          insert(pair)
        case None => insert(pair)
        case _ => // Cache already up-to-date
      }

    pair
  }

  private def get(pair: PullRequestPair): Option[CachedPullRequestPair] = {
    implicit val session = Db
    val key = CachedPullRequestPair(pair)

    // Select
    val query = pairs.filter(p => p.shaOne === key.shaOne && p.shaTwo === key.shaTwo)
    query.firstOption
  }

  private def insert(pair: PullRequestPair): Unit = {
    implicit val session = Db
    val newPair = CachedPullRequestPair(pair)

    // Delete old record(s)
    val query = pairs.filter(p => p.shaOne === newPair.shaOne && p.shaTwo === newPair.shaTwo)
    query.delete

    // Insert
    pairs += newPair
  }

  def init(): Unit = {
    implicit val session = Db

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
