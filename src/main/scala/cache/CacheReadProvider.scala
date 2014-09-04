package cache

import git._

/**
 * A provider implementation for the disk cache.
 */
class CacheReadProvider extends CacheProvider {
  override val mode = CacheMode.Read

  override def getDecorator(list: PullRequestList): PullRequestList = {
    val decorator = new CacheDecorator(list, this)
    _decorators ++= List(decorator)
    decorator
  }

  override def getPairwiseDecorator(list: PairwiseList): PairwiseList = {
    val decorator = new CachePairwiseDecorator(list, this)
    _pairwiseDecorators ++= List(decorator)
    decorator
  }
}
