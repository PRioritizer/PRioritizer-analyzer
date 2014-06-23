package git

import scala.concurrent.Future

/**
 * Offers the functionality to get data about the repository.
 */
trait PullRequestDecorator {
  def decorate(pullRequest: PullRequest): Future[PullRequest]
}
