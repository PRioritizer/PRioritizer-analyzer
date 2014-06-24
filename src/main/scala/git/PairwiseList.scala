package git

import scala.concurrent.Future

trait PairwiseList {
  def get: List[Future[PullRequestPair]]
}
