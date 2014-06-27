package ghtorrent

import git.{PullRequestDecorator, PullRequest, PullRequestList}
import scala.slick.jdbc.{StaticQuery => Q}

/**
 * An info getter implementation for the GHTorrent database.
 * @param provider The GHTorrent provider.
 */
class GHTorrentDecorator(base: PullRequestList, val provider: GHTorrentProvider) extends PullRequestDecorator(base) {
  implicit lazy val session = provider.Db
  lazy val owner = provider.owner
  lazy val repo = provider.repository
  lazy val repoId = provider.repositoryProvider match {
    case Some(p: GHTorrentRepositoryProvider) => p.repoId
    case _ => -1
  }

  override def decorate(pullRequest: PullRequest): PullRequest = {
    if (!pullRequest.contributedCommits.isDefined)
      pullRequest.contributedCommits = Some(getCommitCount(pullRequest.author))

    if (!pullRequest.totalPullRequests.isDefined) {
      val (total, accepted) = getOtherPullRequests(pullRequest.author)
      pullRequest.totalPullRequests = Some(total)
      pullRequest.acceptedPullRequests = Some(accepted)
    }

    if (!pullRequest.coreMember.isDefined)
      pullRequest.coreMember = Some(isCoreMember(pullRequest.author))

    pullRequest
  }

  private def getOtherPullRequests(author: String): (Int, Int) = {
    val total = getPullRequestCount(repoId, author, "opened").firstOption.getOrElse(0)
    val accepted = getPullRequestCount(repoId, author, "merged").firstOption.getOrElse(0)
    (total, accepted)
  }

  private def getCommitCount(author: String): Int =
    getCommitCount(repoId, author).firstOption.getOrElse(0)

  private def isCoreMember(author: String): Boolean =
    getCoreMember(repoId, author).firstOption.isDefined

  private lazy val getCommitCount: Q[(Int, String), Int] =
    Q[(Int, String), Int] +
      """SELECT
        |COUNT(project_commits.commit_id)
        |FROM
        |project_commits
        |INNER JOIN commits ON commits.id = project_commits.commit_id
        |INNER JOIN users ON users.id = commits.author_id
        |WHERE
        |project_commits.project_id = ? AND
        |users.login = ?""".stripMargin

  private lazy val getPullRequestCount: Q[(Int, String, String), Int] =
    Q[(Int, String, String), Int] +
      """SELECT
        |COUNT(DISTINCT pull_requests.id)
        |FROM
        |pull_requests
        |INNER JOIN pull_request_history AS actor_history ON actor_history.pull_request_id = pull_requests.id
        |INNER JOIN pull_request_history AS pull_history ON pull_history.pull_request_id = pull_requests.id
        |INNER JOIN users AS actor ON actor.id = actor_history.actor_id
        |WHERE
        |pull_requests.base_repo_id = ? AND
        |actor_history.action = 'opened' AND
        |actor.login = ? AND
        |pull_history.action = ?""".stripMargin

  private lazy val getCoreMember: Q[(Int, String), Int] =
    Q[(Int, String), Int] +
      """SELECT
        |users.id
        |FROM
        |project_members
        |INNER JOIN users ON users.id = project_members.user_id
        |WHERE
        |project_members.repo_id = ? AND
        |users.login = ?""".stripMargin
}
