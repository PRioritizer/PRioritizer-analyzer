package git

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Offers the functionality to get data about the repository.
 */
abstract class PullRequestDecorator(val base: PullRequestList) extends PullRequestList {
  override def get: List[Future[PullRequest]] = {
    base.get.map {future => for (pr <- future) yield decorate(pr) }
  }

  def decorate(pullRequest: PullRequest): PullRequest = pullRequest
}
