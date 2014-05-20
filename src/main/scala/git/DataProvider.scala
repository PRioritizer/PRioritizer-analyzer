package git

/**
 * Offers the functionality to get data about the repository.
 */
trait DataProvider {
  def enrich(pullRequest: PullRequest): RichPullRequest
}
