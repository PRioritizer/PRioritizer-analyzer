package git

import scala.concurrent.Future

/**
 * Offers the functionality to get pull requests.
 */
trait PullRequestProvider extends PullRequestList {
  def get: Future[List[PullRequest]]

  def source: String

  def owner: String

  def repository: String

  def ssh: String

  def https: String

  def remotePullHeads: String

  def remoteHeads: String
}
