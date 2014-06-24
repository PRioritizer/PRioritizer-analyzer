package ghtorrent

import git._
import scala.concurrent.Future
import scala.slick.driver.MySQLDriver.simple._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * A provider implementation for GHTorrent.
 * @param host The database host.
 * @param port The database port number.
 * @param user The database user name.
 * @param password The database password.
 * @param database The database name.
 * @param owner The name of the owner.
 * @param repository The name of the repository.
 */
class GHTorrentProvider(val host: String, val port: Int, val user: String, val password: String, val database: String, val owner: String, val repository: String) extends Provider {
  val dbUrl = s"jdbc:mysql://$host:$port/$database"
  val dbDriver = "com.mysql.jdbc.Driver"
  lazy val Db = Database.forURL(dbUrl, user, password, driver = dbDriver).createSession()

  override def repositoryProvider: Option[RepositoryProvider] = Some(new GHTorrentRepositoryProvider(this))
  override def pullRequestProvider: Option[PullRequestProvider] = Some(new GHTorrentPullRequestProvider(this))
  override def getDecorator(list: PullRequestList): PullRequestList = new GHTorrentDecorator(list, this)
  override def getPairwiseDecorator(list: PairwiseList): PairwiseList = list

  override def init(provider: PullRequestProvider = null): Future[Unit] = Future {
    // Force lazy value evaluation
    Db
  }

  override def dispose(): Unit = {
    Db.close()
  }
}
