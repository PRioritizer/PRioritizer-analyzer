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

  override def pullRequests: GitHubPullRequestProvider = new GitHubPullRequestProvider(owner, repository)
  override def merger: MergeProvider = ???
  override def data: DataProvider = ???
}
