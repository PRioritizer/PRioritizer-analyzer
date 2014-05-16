package dispatch.github

import git.PullRequest

/**
 * Extensions for the dispatch GitHub api.
 */
object GitHubExtensions {

  /**
   * Enrichment of the [[dispatch.github.GhPullRequest]] class.
   * @param pull The GitHub pull request.
   */
  implicit class RichGhPullRequest(pull: GhPullRequest) {
    /**
     * Converts a [[dispatch.github.GhPullRequest]] to a [[git.PullRequest]]
     * @return The converted pull request.
     */
    def asPullRequest: PullRequest =
      PullRequest(pull.number, pull.head.label, pull.base.ref)
  }
}
