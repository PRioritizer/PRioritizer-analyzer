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
  override def enrich(pullRequest: PullRequest): Future[PullRequest] = {
    Future {
      val (total, accepted) = getPreviousPullRequests(pullRequest)
      pullRequest.previouslyCreatedPullRequests = total
      pullRequest.previouslyAcceptedPullRequests = accepted
      pullRequest
    }
  }

  def getPreviousPullRequests(pullRequest: PullRequest): (Int, Int) = {
    val owner = provider.owner
    val repo = provider.repository
    val author = pullRequest.author
    implicit val session = provider.Db

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

    val list = query.apply(owner, repo, author).list(session)
    val total = list.length
    val accepted = list.sum
    (total, accepted)
  }
}
