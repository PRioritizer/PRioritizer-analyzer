package ghtorrent

import ghtorrent.models._
import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.lifted.ProvenShape._

object GHTorrentSchema {
  object Tables {
    val pullRequests = TableQuery[PullRequests]
    val users = TableQuery[Users]
    val commits = TableQuery[Commits]
    val issues = TableQuery[Issues]
    val pullRequestHistory = TableQuery[PullRequestHistory]
    val projectCommits = TableQuery[ProjectCommits]
    val projectMembers = TableQuery[ProjectMembers]
    val comments = TableQuery[Comments]
    val reviewComments = TableQuery[ReviewComments]
  }

  object TableNames {
    val pullRequests = "pull_requests"
    val users = "users"
    val commits = "commits"
    val issues = "issues"
    val pullRequestHistory = "pull_request_history"
    val projectCommits = "project_commits"
    val projectMembers = "project_members"
    val comments = "issue_comments"
    val reviewComments = "pull_request_comments"
  }

  class PullRequests(tag: Tag) extends Table[PullRequest](tag, TableNames.pullRequests) {
    def id = column[Int]("id", O.PrimaryKey)
    def baseRepoId = column[Int]("base_repo_id")
    def number = column[Int]("pullreq_id")

    def * = (id, number, baseRepoId) <> (PullRequest.tupled, PullRequest.unapply)
  }

  class Users(tag: Tag) extends Table[User](tag, TableNames.users) {
    def id = column[Int]("id", O.PrimaryKey)
    def login = column[String]("login")

    def * = (id, login) <> (User.tupled, User.unapply)
  }

  class Commits(tag: Tag) extends Table[Commit](tag, TableNames.commits) {
    def id = column[Int]("id", O.PrimaryKey)
    def authorId = column[Int]("author_id")

    def * = (id, authorId) <> (Commit.tupled, Commit.unapply)
  }

  class Issues(tag: Tag) extends Table[Issue](tag, TableNames.issues) {
    def id = column[Int]("id", O.PrimaryKey)
    def pullRequestId = column[Int]("pull_request_id")

    def * = (id, pullRequestId) <> (Issue.tupled, Issue.unapply)
  }

  class PullRequestHistory(tag: Tag) extends Table[PullRequestAction](tag, TableNames.pullRequestHistory) {
    def pullRequestId = column[Int]("pull_request_id")
    def userId = column[Int]("actor_id")
    def action = column[String]("action")

    def * = (pullRequestId, userId, action) <> (PullRequestAction.tupled, PullRequestAction.unapply)
  }

  class ProjectCommits(tag: Tag) extends Table[ProjectCommit](tag, TableNames.projectCommits) {
    def projectId = column[Int]("project_id")
    def commitId = column[Int]("commit_id")

    def * = (projectId, commitId) <> (ProjectCommit.tupled, ProjectCommit.unapply)
    def pk = primaryKey("key", (projectId, commitId))
  }

  class ProjectMembers(tag: Tag) extends Table[ProjectMember](tag, TableNames.projectMembers) {
    def repoId = column[Int]("repo_id")
    def userId = column[Int]("user_id")

    def * = (repoId, userId) <> (ProjectMember.tupled, ProjectMember.unapply)
    def pk = primaryKey("key", (repoId, userId))
  }

  class Comments(tag: Tag) extends Table[Comment](tag, TableNames.comments) {
    def id = column[Int]("comment_id", O.PrimaryKey)
    def issueId = column[Int]("issue_id")

    def * = (id, issueId) <> (Comment.tupled, Comment.unapply)
  }

  class ReviewComments(tag: Tag) extends Table[ReviewComment](tag, TableNames.reviewComments) {
    def id = column[Int]("comment_id", O.PrimaryKey)
    def pullRequestId = column[Int]("pull_request_id")

    def * = (id, pullRequestId) <> (ReviewComment.tupled, ReviewComment.unapply)
  }
}
