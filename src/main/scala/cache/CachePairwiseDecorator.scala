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
  val dbName = "pair_cache.db"
  val dbDriver = "org.sqlite.JDBC"
  lazy val cachePath = provider.cachePath
  lazy val dbPath = cachePath + java.io.File.separator + dbName
  lazy val dbUrl = s"jdbc:sqlite:$dbPath"
  lazy val Db = Database.forURL(dbUrl, driver = dbDriver).createSession()
  lazy val pairs = TableQuery[PairCache]

  init()

  override def decorate(pair: PullRequestPair): PullRequestPair = {
    if (mode == CacheMode.Read) {
      get(pair) match {
        case Some(mergeable) =>
          pair.isMergeable = Some(mergeable)
        case _ =>
      }
      pair
    } else {
      if (pair.dirty)
        insert(pair)

      pair
    }
  }

  private def get(pair: PullRequestPair): Option[Boolean] = {
    implicit val session = Db
    val key: CachedPullRequestPair = CachedPullRequestPair(pair)

    // Select
    val query = pairs.filter(p => p.shaOne === key.shaOne && p.shaTwo === key.shaTwo)
    query.firstOption.map { q => q.mergeable }
  }

  private def insert(pair: PullRequestPair): Unit = {
    implicit val session = Db
    val newPair: CachedPullRequestPair = CachedPullRequestPair(pair)

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

/**
 * An enum type for merge results.
 */
object CacheMode extends Enumeration {
  type CacheMode = Value
  val Read, Write = Value
}
