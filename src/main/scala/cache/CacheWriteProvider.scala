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
  override val mode = CacheMode.Write

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
