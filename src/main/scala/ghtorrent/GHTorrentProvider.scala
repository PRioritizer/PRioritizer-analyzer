package ghtorrent

import ghtorrent.repo.GHTorrentRepositoryProvider
import git._
import ghtorrent.pull.GHTorrentPullRequestProvider
import scala.slick.driver.MySQLDriver.simple._
import ghtorrent.enrich.GHTorrentEnrichmentProvider

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
  val Db = Database.forURL(dbUrl, user, password, driver = dbDriver).createSession()

  override def repositoryProvider: Option[RepositoryProvider] =
    Some(new GHTorrentRepositoryProvider(this))
  override def pullRequestProvider: Option[PullRequestProvider] =
    Some(new GHTorrentPullRequestProvider(this))
  override def mergeProvider: Option[MergeProvider] = None
  override def enrichmentProvider: Option[EnrichmentProvider] =
    Some(new GHTorrentEnrichmentProvider(this))

  override def dispose(): Unit = {
    Db.close()
  }
}
