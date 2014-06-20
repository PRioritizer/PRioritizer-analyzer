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
  val owner = provider.owner
  val repo = provider.repository
  //private val numCommits = getCommitCount()

  override def enrich(pullRequest: PullRequest): Future[PullRequest] = Future {
    val (total, accepted) = getOtherPullRequests(pullRequest.author)
    pullRequest.contributedCommits = getCommitCount(pullRequest.author)
    pullRequest.totalPullRequests = total
    pullRequest.acceptedPullRequests = accepted
    pullRequest
  }

  def getOtherPullRequests(author: String): (Int, Int) = {
    implicit val session = provider.Db

    // Execute query
    val query = getPullRequestCountQuery
    val total = query.apply("opened", owner, repo, author).list(session).sum
    val accepted = query.apply("merged", owner, repo, author).list(session).sum
    (total, accepted)
  }

  def getCommitCount(author: String = null): Int = {
    implicit val session = provider.Db
    val authorX = if (author != null) author else "%"

    // Execute query
    val query = getCommitCountQuery
    val count = query.apply(owner, repo, authorX).list(session).head
    count
  }

  def getCommitCountQuery: StaticQuery[(String, String, String), (Int)] = {
    StaticQuery.query[(String, String, String), (Int)](
      """SELECT
        |COUNT(commits.author_id)
        |FROM
        |users
        |INNER JOIN commits ON users.id = commits.author_id
        |INNER JOIN projects ON commits.project_id = projects.id
        |INNER JOIN users AS owners ON owners.id = projects.owner_id
        |WHERE
        |owners.login = ? AND
        |projects.`name` = ? AND
        |users.login LIKE ?""".stripMargin)
  }

  def getPullRequestCountQuery: StaticQuery[(String, String, String, String), (Int)] = {
    StaticQuery.query[(String, String, String, String), (Int)](
      """SELECT
        |COUNT(DISTINCT pull_requests.id)
        |FROM
        |pull_requests
        |INNER JOIN projects ON pull_requests.base_repo_id = projects.id
        |INNER JOIN users AS owners ON owners.id = projects.owner_id
        |INNER JOIN pull_request_history AS actor_history ON actor_history.pull_request_id = pull_requests.id
        |INNER JOIN pull_request_history AS pull_history ON pull_history.pull_request_id = pull_requests.id
        |INNER JOIN users AS actor ON actor.id = actor_history.actor_id
        |WHERE
        |actor_history.action = 'opened' AND
        |pull_history.action = ? AND
        |owners.login = ? AND
        |projects.`name` = ? AND
        |actor.login = ?""".stripMargin)
  }
}
