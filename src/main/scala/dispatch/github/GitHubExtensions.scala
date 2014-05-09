package dispatch.github

import git.PullRequest

object GitHubExtensions {
  implicit class RichGhPullRequest(pull: GhPullRequest) {
    def asPullRequest: PullRequest =
      PullRequest(pull.number, pull.head.label, pull.base.ref)
  }
}
