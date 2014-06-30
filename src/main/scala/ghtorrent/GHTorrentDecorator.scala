package ghtorrent

import ghtorrent.GHTorrentSchema.Tables
import git.{PullRequestDecorator, PullRequest, PullRequestList}
import scala.slick.driver.MySQLDriver.simple._

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
    val total = queryPullRequestCount(repoId, author, "opened").run
    val accepted = queryPullRequestCount(repoId, author, "merged").run
    (total, accepted)
  }

  private def getCommitCount(author: String): Int =
    queryCommitCount(repoId, author).run

  private def isCoreMember(author: String): Boolean =
    queryCoreMember(repoId, author).firstOption.isDefined

  private lazy val queryCommitCount = {
    def commitCount(repoId: Column[Int], userLogin: Column[String]) =
      (for {
        u <- Tables.users
        c <- Tables.commits
        pc <- Tables.projectCommits
        // Join
        if c.id === pc.commitId
        if u.id === c.authorId
        // Where
        if pc.projectId === repoId
        if u.login === userLogin
      } yield c.id).length

    Compiled(commitCount _)
  }

  private lazy val queryPullRequestCount = {
    def pullRequestCount(repoId: Column[Int], userLogin: Column[String], action: Column[String]) =
      (for {
        u <- Tables.users
        p <- Tables.pullRequests
        pHis <- Tables.pullRequestHistory
        aHis <- Tables.pullRequestHistory
        // Join
        if p.id === aHis.pullRequestId
        if p.id === pHis.pullRequestId
        if u.id === aHis.userId
        // Where
        if p.baseRepoId === repoId
        if aHis.action === "opened"
        if u.login === userLogin
        if pHis.action === action
      } yield p.id).countDistinct

    Compiled(pullRequestCount _)
  }

  private lazy val queryCoreMember = for {
    (repoId, userLogin) <- Parameters[(Int, String)]
    u <- Tables.users
    p <- Tables.projectMembers
    // Join
    if u.id === p.userId
    // Where
    if p.repoId === repoId
    if u.login === userLogin
  } yield u.id
}
