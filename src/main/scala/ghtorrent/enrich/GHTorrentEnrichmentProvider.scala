package ghtorrent.enrich

import git.{PullRequest, EnrichmentProvider}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import ghtorrent.GHTorrentProvider
import scala.slick.jdbc.StaticQuery

/**
 * An info getter implementation for the GHTorrent database.
 * @param provider The GHTorrent provider.
 */
class GHTorrentEnrichmentProvider(val provider: GHTorrentProvider) extends EnrichmentProvider {
  private val pullCache = scala.collection.mutable.Map[String,(Int,Int)]()
  private val commitCache = scala.collection.mutable.Map[String,Int]()
  //private val numCommits = getCommitCount()

  override def enrich(pullRequest: PullRequest): Future[PullRequest] = {
    Future {
      val (total, accepted) = getOtherPullRequests(pullRequest.author)
      pullRequest.contributedCommits = getCommitCount(pullRequest.author)
      pullRequest.totalPullRequests = total
      pullRequest.acceptedPullRequests = accepted
      pullRequest
    }
  }

  def getOtherPullRequests(author: String): (Int, Int) = {
    implicit val session = provider.Db
    val owner = provider.owner
    val repo = provider.repository

    // Check cache first (may be bypassed due to parallel execution)
    if (pullCache.get(author).isDefined)
      return pullCache.get(author).get

    val query = StaticQuery.query[(String, String, String), (Int)]("""SELECT
        pull_requests.merged
        FROM
        users
        INNER JOIN pull_requests ON users.id = pull_requests.user_id
        INNER JOIN projects ON pull_requests.base_repo_id = projects.id
        INNER JOIN users AS owners ON owners.id = projects.owner_id
        WHERE
        owners.login = ? AND
        projects.`name` = ? AND
        users.login = ?""")

    // Execute query
    val list = query.apply(owner, repo, author).list(session)
    val total = list.length
    val accepted = list.sum

    // Save in cache and return
    this.synchronized {
      pullCache += author -> (total, accepted)
    }
    (total, accepted)
  }

  def getCommitCount(author: String = null): Int = {
    implicit val session = provider.Db
    val owner = provider.owner
    val repo = provider.repository
    val authorX = if (author != null) author else "%"

    // Check cache first (may be bypassed due to parallel execution)
    if (commitCache.get(author).isDefined)
      return commitCache.get(author).get

    val query = StaticQuery.query[(String, String, String), (Int)]("""SELECT
      COUNT(commits.author_id)
      FROM
      users
      INNER JOIN commits ON users.id = commits.author_id
      INNER JOIN projects ON commits.project_id = projects.id
      INNER JOIN users AS owners ON owners.id = projects.owner_id
      WHERE
      owners.login = ? AND
      projects.`name` = ? AND
      users.login LIKE ?""")

    // Execute query
    val count = query.apply(owner, repo, authorX).list(session).head

    // Save in cache and return
    this.synchronized {
      commitCache += authorX -> count
    }
    count
  }
}
