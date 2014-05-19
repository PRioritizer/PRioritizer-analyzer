package github

import git.{MergeProvider, DataProvider, Provider}
import github.pulls.GitHubPullRequestProvider
import dispatch.github.GitHub

/**
 * A provider implementation for GitHub.
 * @param owner The name of the owner.
 * @param repository The name of the repository.
 * @param token The GitHub API access token.
 */
class GitHubProvider(val owner: String, val repository: String, token: String) extends Provider {
  // Set global access token
  GitHub.accessToken = token

  override def pullRequests: Option[GitHubPullRequestProvider] =
    Some(new GitHubPullRequestProvider(owner, repository))
  override def merger: Option[MergeProvider] = None
  override def data: Option[DataProvider] = None

  override def dispose(): Unit = {
    GitHub.shutdown()
  }
}
