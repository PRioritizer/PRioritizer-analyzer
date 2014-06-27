package ghtorrent

import git.{PullRequestDecorator, PullRequest, PullRequestList}
import utils.Stopwatch
import scala.slick.jdbc.{StaticQuery => Q}


import scala.slick.driver.MySQLDriver.simple._
import scala.slick.lifted.ProvenShape._

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

  // Tables
  lazy val users = TableQuery[Users]
  lazy val commits = TableQuery[Commits]
  lazy val pullRequests = TableQuery[PullRequests]
  lazy val pullRequestHistory = TableQuery[PullRequestHistory]
  lazy val projectMembers = TableQuery[ProjectMembers]
  lazy val projectCommits = TableQuery[ProjectCommits]

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
        u <- users
        c <- commits
        pc <- projectCommits
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
        u <- users
        p <- pullRequests
        pHis <- pullRequestHistory
        aHis <- pullRequestHistory
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
    u <- users
    p <- projectMembers
    // Join
    if u.id === p.userId
    // Where
    if p.repoId === repoId
    if u.login === userLogin
  } yield u.id
}

class PullRequests(tag: Tag) extends Table[PullRequest2](tag, "pull_requests") {
  def id = column[Int]("id", O.PrimaryKey)
  def baseRepoId = column[Int]("base_repo_id")

  def * = (id, baseRepoId) <> (PullRequest2.tupled, PullRequest2.unapply)
}

class Users(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Int]("id", O.PrimaryKey)
  def login = column[String]("login")

  def * = (id, login) <> (User.tupled, User.unapply)
}

class Commits(tag: Tag) extends Table[Commit](tag, "commits") {
  def id = column[Int]("id", O.PrimaryKey)
  def authorId = column[Int]("author_id")

  def * = (id, authorId) <> (Commit.tupled, Commit.unapply)
}

class PullRequestHistory(tag: Tag) extends Table[PullRequestAction](tag, "pull_request_history") {
  def pullRequestId = column[Int]("pull_request_id")
  def userId = column[Int]("actor_id")
  def action = column[String]("action")

  def * = (pullRequestId, userId, action) <> (PullRequestAction.tupled, PullRequestAction.unapply)
}

class ProjectCommits(tag: Tag) extends Table[ProjectCommit](tag, "project_commits") {
  def projectId = column[Int]("project_id")
  def commitId = column[Int]("commit_id")

  def * = (projectId, commitId) <> (ProjectCommit.tupled, ProjectCommit.unapply)
  def pk = primaryKey("key", (projectId, commitId))
}

class ProjectMembers(tag: Tag) extends Table[ProjectMember](tag, "project_members") {
  def repoId = column[Int]("repo_id")
  def userId = column[Int]("user_id")

  def * = (repoId, userId) <> (ProjectMember.tupled, ProjectMember.unapply)
  def pk = primaryKey("key", (repoId, userId))
}

case class PullRequest2(id: Int, baseRepoId: Int)

case class User(id: Int, login: String)

case class Commit(id: Int, authorId: Int)

case class PullRequestAction(pullRequestId: Int, userId: Int, action: String)

case class ProjectMember(repoId: Int, userId: Int)

case class ProjectCommit(repoId: Int, userId: Int)
