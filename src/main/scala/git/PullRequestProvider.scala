package git

import scala.concurrent.Future

/**
 * Offers the functionality to get pull requests.
 */
trait PullRequestProvider {
  def get: Future[List[PullRequest]]

  def ssh: String

  def https: String

  def remotePullHeads: String
}
