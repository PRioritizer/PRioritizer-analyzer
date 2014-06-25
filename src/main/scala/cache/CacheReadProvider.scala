package cache

import git._

/**
 * A provider implementation for the disk cache.
 * @param cacheDirectory The path to the directory of the cache.
 */
class CacheReadProvider(cacheDirectory: String) extends CacheProvider(cacheDirectory) {
  override def getDecorator(list: PullRequestList): PullRequestList = list
  override def getPairwiseDecorator(list: PairwiseList): PairwiseList = {
    val decorator = new CachePairwiseDecorator(list, this, CacheMode.Read)
    _pairwiseDecorators ++= List(decorator)
    decorator
  }
}
