package cache

import git._
import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.MTable
import scala.slick.lifted.ProvenShape._
import java.sql.Date

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

    // Smallest sha first
    val sha1 = if (pair.pr1.sha < pair.pr2.sha) pair.pr1.sha else pair.pr2.sha
    val sha2 = if (pair.pr1.sha < pair.pr2.sha) pair.pr2.sha else pair.pr1.sha

    // Select
    val query = pairs.filter(p => p.shaOne === sha1 && p.shaTwo === sha2)
    query.firstOption.map { q => q._4 }
  }

  private def insert(pair: PullRequestPair): Unit = {
    implicit val session = Db

    // Smallest sha first
    val sha1 = if (pair.pr1.sha < pair.pr2.sha) pair.pr1.sha else pair.pr2.sha
    val sha2 = if (pair.pr1.sha < pair.pr2.sha) pair.pr2.sha else pair.pr1.sha

    // Delete old record(s)
    val query = pairs.filter(p => p.shaOne === sha1 && p.shaTwo === sha2)
    query.delete

    // Insert
    pairs += (now(), sha1, sha2, pair.isMergeable.getOrElse(false))
  }

  def now(): java.sql.Date = new java.sql.Date(new java.util.Date().getTime)

  def init(): Unit = {
    implicit val session = Db

    val test = pairs.ddl.createStatements

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

class PairCache(tag: Tag) extends Table[(Date, String, String, Boolean)](tag, PairCache.tableName) {
  def date = column[Date]("date")
  def shaOne = column[String]("sha_one")
  def shaTwo = column[String]("sha_two")
  def mergeable = column[Boolean]("mergable")

  def * = (date, shaOne, shaTwo, mergeable)

  def pk = primaryKey("sha", (shaOne, shaTwo))
}

object PairCache {
  val tableName = "pair_cache"
}

/**
 * An enum type for merge results.
 */
object CacheMode extends Enumeration {
  type CacheMode = Value
  val Read, Write = Value
}
