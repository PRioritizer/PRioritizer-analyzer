package cache

import git._
import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.MTable
import cache.CacheSchema.{Tables, TableNames}
import cache.models.CachedPullRequestPair

/**
 * An info getter implementation for the JGit library.
 * @param provider The JGit provider.
 */
class CachePairwiseDecorator(base: PairwiseList, provider: CacheProvider) extends PairwiseDecorator(base) {
  implicit lazy val session = provider.Db
  lazy val mode = provider.mode
  lazy val insertPair = Tables.pairs.insertInvoker
  lazy val getPairsByKey = for {
    (shaOne, shaTwo) <- Parameters[(String, String)]
    p <- Tables.pairs
    if p.shaOne === shaOne
    if p.shaTwo === shaTwo
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
    insertPair.insertOrUpdate(CachedPullRequestPair(pair))
  }

  def init(): Unit = {
    // Create table
    if (MTable.getTables(TableNames.pairs).list.isEmpty)
      Tables.pairs.ddl.create
  }
}
