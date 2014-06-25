package github

import dispatch.Defaults._
import dispatch.github.GhPullRequest
import git.{PullRequest, PullRequestProvider, PullRequestType}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class GitHubPullRequestProvider(val provider: GitHubProvider) extends PullRequestProvider {
  val host = "github.com"
  val source = "github"
  lazy val owner = provider.owner
  lazy val repository = provider.repository

  override val https: String = s"https://$host/$owner/$repository.git"

  override val ssh: String = s"git@$host:$owner/$repository.git"

  override val remotePullHeads: String = "refs/pull/*/head"

  override val remoteHeads: String = "refs/heads/*"

  override def get: Future[List[PullRequest]] = {
    val req = GhPullRequest.get_pull_requests(owner, repository)

    // Convert GhPullRequests to PullRequests
    for {
      list <- req
    } yield for {
      pr <- list
    } yield {
      val p = PullRequest(pr.number, pr.user.login, pr.head.label, pr.base.ref)
      p.title = pr.title
      p.`type` = PullRequestType.parse(pr.title)
      p.createdAt = pr.created_at
      p.updatedAt = pr.updated_at
      p
    }
  }
}
