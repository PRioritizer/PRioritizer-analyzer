package cache.models

import git.{PullRequestPair, PairwiseList}

import scala.concurrent.Future

class EmptyPairwiseList extends PairwiseList {
  override def get: List[Future[PullRequestPair]] = List()
}
