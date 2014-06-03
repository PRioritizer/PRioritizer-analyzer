package github.pulls

import git.{PullRequest, PullRequestProvider}
import dispatch.github.GhPullRequest
import scala.concurrent.Future
import dispatch.Defaults._
import github.GitHubProvider

class GitHubPullRequestProvider(val provider: GitHubProvider) extends PullRequestProvider {
  val host = "github.com"
  val owner = provider.owner
  val repository = provider.repository

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
      val p = PullRequest(pr.number, pr.user.login, pr.head.ref, pr.base.ref)
      p.createdAt = pr.created_at
      p.updatedAt = pr.updated_at
      p
    }
  }
}
