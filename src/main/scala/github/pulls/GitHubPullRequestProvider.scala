package github.pulls

import git.{PullRequest, PullRequestProvider}
import dispatch.github.GhPullRequest
import scala.concurrent.Future
import dispatch.Defaults._

class GitHubPullRequestProvider(val owner: String, val repository: String) extends PullRequestProvider {
  val host = "github.com"

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
    } yield PullRequest(pr.number, pr.head.label, pr.base.ref)
  }
}
