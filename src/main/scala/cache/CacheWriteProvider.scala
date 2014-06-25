package cache

import java.io.File

import git._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * A provider implementation for the disk cache.
 * @param cacheDirectory The path to the directory of the cache.
 */
class CacheWriteProvider(cacheDirectory: String) extends CacheProvider(cacheDirectory) {
  override def getDecorator(list: PullRequestList): PullRequestList = list
  override def getPairwiseDecorator(list: PairwiseList): PairwiseList = {
    val decorator = new CachePairwiseDecorator(list, this, CacheMode.Write)
    _pairwiseDecorators ++= List(decorator)
    decorator
  }
}
