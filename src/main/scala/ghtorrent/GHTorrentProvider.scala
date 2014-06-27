package ghtorrent

import git._
import scala.concurrent.Future
import scala.slick.driver.MySQLDriver.simple._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.slick.jdbc.StaticQuery

/**
 * A provider implementation for GHTorrent.
 * @param host The database host.
 * @param port The database port number.
 * @param user The database user name.
 * @param password The database password.
 * @param database The database name.
 */
class GHTorrentProvider(val host: String, val port: Int, val user: String, val password: String, val database: String) extends Provider {
  val dbUrl = s"jdbc:mysql://$host:$port/$database"
  val dbDriver = "com.mysql.jdbc.Driver"
  lazy val Db = Database.forURL(dbUrl, user, password, driver = dbDriver).createSession()

  private var _owner: String = _
  private var _repository: String = _

  def owner = _owner
  def repository = _repository

  override val repositoryProvider: Option[RepositoryProvider] = Some(new GHTorrentRepositoryProvider(this))
  override val pullRequestProvider: Option[PullRequestProvider] = None
  override def getDecorator(list: PullRequestList): PullRequestList = new GHTorrentDecorator(list, this)
  override def getPairwiseDecorator(list: PairwiseList): PairwiseList = list

  override def init(provider: PullRequestProvider = null): Future[Unit] = Future {
    if (provider != null) {
      _owner = provider.owner
      _repository = provider.repository
    }

    // Execute test query
    test()
  }

  def test(): Boolean = {
    val status = StaticQuery.query[Unit, String]("SHOW SESSION STATUS;").list(Db)
    status.length > 0
  }

  override def dispose(): Unit = {
    try {
      Db.close()
    } catch {
      case _ :Exception =>
    }
  }
}
