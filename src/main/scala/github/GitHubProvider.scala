package github

import git.{RepositoryProvider, MergeProvider, PullRequestDecorator, Provider}
import github.pulls.GitHubPullRequestProvider
import dispatch.github.GitHub
import github.decorate.GitHubDecorator

/**
 * A provider implementation for GitHub.
 * @param owner The name of the owner.
 * @param repository The name of the repository.
 * @param token The GitHub API access token.
 */
class GitHubProvider(val owner: String, val repository: String, token: String) extends Provider {
  // Set global access token
  GitHub.accessToken = token

  override def repositoryProvider: Option[RepositoryProvider] = None
  override def pullRequestProvider: Option[GitHubPullRequestProvider] =
    Some(new GitHubPullRequestProvider(this))
  override def mergeProvider: Option[MergeProvider] = None
  override def decorator: Option[PullRequestDecorator] =
    Some(new GitHubDecorator(this))

  override def dispose(): Unit = {
    GitHub.shutdown()
  }
}
