package git

import scala.concurrent.Future

/**
 * Offers the functionality to get data about the repository.
 */
trait DataProvider {
  def enrich(pullRequest: PullRequest): Future[PullRequest]
}
