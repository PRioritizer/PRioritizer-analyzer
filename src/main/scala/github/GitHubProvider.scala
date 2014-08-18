package github

import dispatch.github.GitHub
import git._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * A provider implementation for GitHub.
 * @param owner The name of the owner.
 * @param repository The name of the repository.
 * @param token The GitHub API access token.
 */
class GitHubProvider(val owner: String, val repository: String, token: String) extends Provider {

  if (owner == null || owner == "" || repository == null || repository == "" || token == null || token == "")
    throw new IllegalArgumentException("Invalid GitHub configuration.")

  override val repositoryProvider: Option[RepositoryProvider] = None
  override val pullRequestProvider: Option[GitHubPullRequestProvider] = Some(new GitHubPullRequestProvider(this))
  override def getDecorator(list: PullRequestList): PullRequestList = new GitHubDecorator(list, this)
  override def getPairwiseDecorator(list: PairwiseList): PairwiseList = list

  private var _loadedRepositoryProvider: RepositoryProvider = _
  def loadedRepositoryProvider = _loadedRepositoryProvider

  override def init(provider: Provider): Future[Unit] = Future {
    // Set global access token
    GitHub.accessToken = token
    _loadedRepositoryProvider = provider.repositoryProvider.orNull
  }

  override def dispose(): Unit = {
    GitHub.shutdown()
  }
}
