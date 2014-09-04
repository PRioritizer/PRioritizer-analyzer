package git

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Offers the functionality to get data about the repository.
 */
abstract class TotalDecorator(val base: TotalList) extends TotalList {
  override def get: Future[List[PullRequest]] = base.get.map(decorate)

  def decorate(pullRequests: List[PullRequest]): List[PullRequest] = pullRequests
}
