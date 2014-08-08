package cache

import git._

/**
 * A provider implementation for the disk cache.
 * @param cacheDirectory The path to the directory of the cache.
 */
class CacheReadProvider(cacheDirectory: String) extends CacheProvider(cacheDirectory) {
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
