package github

import dispatch.github.GitHub
import git._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * A provider implementation for GitHub.
 */
class GitHubProvider extends Provider {
  if (!GitHubSettings.validate)
    throw new IllegalArgumentException("Invalid GitHub configuration.")

  lazy val owner = GitHubSettings.owner
  lazy val repository = GitHubSettings.repository

  override val pullRequestProvider: Option[GitHubPullRequestProvider] = Some(new GitHubPullRequestProvider(this))
  override def getDecorator(list: PullRequestList): PullRequestList = new GitHubDecorator(list, this)

  private var _loadedRepositoryProvider: RepositoryProvider = _
  def loadedRepositoryProvider = _loadedRepositoryProvider

  override def init(provider: Provider): Future[Unit] = Future {
    // Set global access token
    GitHub.accessToken = GitHubSettings.token
    _loadedRepositoryProvider = provider.repositoryProvider.orNull
  }

  override def dispose(): Unit = {
    GitHub.shutdown()
  }
}
