package ghtorrent

import git._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.StaticQuery

/**
 * A provider implementation for GHTorrent.
 */
class GHTorrentProvider extends Provider {

  val dbUrl = s"jdbc:mysql://${GHTorrentSettings.host}:${GHTorrentSettings.port}/${GHTorrentSettings.database}"
  val dbDriver = "com.mysql.jdbc.Driver"

  if (!GHTorrentSettings.validate)
    throw new IllegalArgumentException("Invalid GHTorrent configuration.")

  lazy val Db = Database.forURL(dbUrl, GHTorrentSettings.username, GHTorrentSettings.password, driver = dbDriver).createSession()
  lazy val mongoDb = new GHTorrentMongoDb(GHTorrentMongoSettings.host, GHTorrentMongoSettings.port, GHTorrentMongoSettings.username, GHTorrentMongoSettings.password, GHTorrentMongoSettings.database)

  private var _owner: String = _
  private var _repository: String = _

  def owner = _owner
  def repository = _repository

  override val repositoryProvider: Option[RepositoryProvider] = Some(new GHTorrentRepositoryProvider(this))
  override val commitProvider: Option[CommitProvider] = Some(new GHTorrentCommitProvider(this))
  override def getDecorator(list: PullRequestList): PullRequestList = new GHTorrentDecorator(list, this)

  override def init(provider: Provider): Future[Unit] = Future {
    if (provider != null && provider.pullRequestProvider.orNull != null) {
      _owner = provider.pullRequestProvider.get.owner
      _repository = provider.pullRequestProvider.get.repository
    }

    // Execute test query
    test()

    // Open MongoDb
    mongoDb.open()
  }

  def test(): Boolean = {
    val status = StaticQuery.query[Unit, String]("SHOW SESSION STATUS;").list(Db)
    status.length > 0
  }

  override def dispose(): Unit = {
    try {
      Db.close()
      mongoDb.close()
    } catch {
      case _ :Exception =>
    }
  }
}
