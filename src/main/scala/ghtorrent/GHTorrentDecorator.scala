package ghtorrent

import git.{PullRequestDecorator, PullRequest, PullRequestList}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.slick.jdbc.StaticQuery

/**
 * An info getter implementation for the GHTorrent database.
 * @param provider The GHTorrent provider.
 */
class GHTorrentDecorator(base: PullRequestList, val provider: GHTorrentProvider) extends PullRequestDecorator(base) {
  val owner = provider.owner
  val repo = provider.repository
  lazy val repoId = provider.repositoryProvider match {
    case Some(p: GHTorrentRepositoryProvider) => p.getRepoId
    case _ => -1
  }

  override def get: Future[List[PullRequest]] = {
    for(list <- base.get) yield list.map(decorate)
  }

  def decorate(pullRequest: PullRequest): PullRequest = {
    val (total, accepted) = getOtherPullRequests(pullRequest.author)
    pullRequest.contributedCommits = getCommitCount(pullRequest.author)
    pullRequest.totalPullRequests = total
    pullRequest.acceptedPullRequests = accepted
    pullRequest.coreMember = isCoreMember(pullRequest.author)
    pullRequest
  }

  def getOtherPullRequests(author: String): (Int, Int) = {
    implicit val session = provider.Db

    // Execute query
    val query = getPullRequestCountQuery
    val total = query.apply(repoId, author, "opened").list(session).sum
    val accepted = query.apply(repoId, author, "merged").list(session).sum
    (total, accepted)
  }

  def getCommitCount(author: String): Int = {
    implicit val session = provider.Db

    // Execute query
    val query = getCommitCountQuery
    val count = query.apply(repoId, author).list(session).head
    count
  }

  def isCoreMember(author: String): Boolean = {
    implicit val session = provider.Db

    // Execute query
    val query = getCoreMemberQuery
    val coreMember = query.apply(repoId, author).list(session).nonEmpty
    coreMember
  }

  def getCommitCountQuery: StaticQuery[(Int, String), Int] = {
    StaticQuery.query[(Int, String), Int](
      """SELECT
        |COUNT(project_commits.commit_id)
        |FROM
        |project_commits
        |INNER JOIN commits ON commits.id = project_commits.commit_id
        |INNER JOIN users ON users.id = commits.author_id
        |WHERE
        |project_commits.project_id = ? AND
        |users.login = ?""".stripMargin)
  }

  def getPullRequestCountQuery: StaticQuery[(Int, String, String), Int] = {
    StaticQuery.query[(Int, String, String), Int](
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
        |pull_history.action = ?""".stripMargin)
  }

  def getCoreMemberQuery: StaticQuery[(Int, String), Int] = {
    StaticQuery.query[(Int, String), Int](
      """SELECT
        |users.id
        |FROM
        |project_members
        |INNER JOIN users ON users.id = project_members.user_id
        |WHERE
        |project_members.repo_id = ? AND
        |users.login = ?""".stripMargin)
  }
}
