package github

import dispatch.Defaults._
import dispatch.github.{GhAuthor, GhPullRequest}
import git.{PullRequest, PullRequestProvider, PullRequestType}

import scala.concurrent.Future

class GitHubPullRequestProvider(val provider: GitHubProvider) extends PullRequestProvider {
  val host = "github.com"
  val source = "github"
  lazy val owner = provider.owner
  lazy val repository = provider.repository
  lazy val branches = provider.loadedRepositoryProvider.branchTips

  override lazy val https: String = s"https://$host/$owner/$repository.git"

  override lazy val ssh: String = s"git@$host:$owner/$repository.git"

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
      val user = if (pr.user == null || pr.user.login == null) GhAuthor(null, null, "Unknown user", null, -1) else pr.user
      val targetSha = branches(pr.base.ref)
      val p = PullRequest(pr.number, user.login, pr.head.sha, targetSha, pr.head.label, pr.base.ref)
      p.commitProvider = Some(provider.loadedCommitProvider)
      p.title = Some(pr.title)
      p.`type` = Some(PullRequestType.parse(pr.title))
      p.createdAt = Some(pr.created_at)
      p.updatedAt = Some(pr.updated_at)
      p.avatar = Option(user.avatar_url)
      p.intraBranch = Some(pr.base != null && pr.head != null &&
                           pr.base.repo != null && pr.head.repo != null &&
                           pr.base.repo.id == pr.head.repo.id)
      p
    }
  }
}
