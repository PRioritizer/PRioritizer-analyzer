package git

import scala.concurrent.Future

trait PairwiseList {
  def get: Future[List[PullRequestPair]]
}
