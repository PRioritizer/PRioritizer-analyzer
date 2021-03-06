package git

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Offers the functionality to get data about the repository.
 */
abstract class PairwiseDecorator(val base: PairwiseList) extends PairwiseList {
  override def get: List[Future[PullRequestPair]] = {
    base.get.map {future => for (pr <- future) yield decorate(pr) }
  }

  def decorate(pair: PullRequestPair): PullRequestPair = pair
}
