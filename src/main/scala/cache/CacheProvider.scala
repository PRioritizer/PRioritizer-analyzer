package cache

import java.io.File
import git._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * A provider implementation for the disk cache.
 * @param cacheDirectory The path to the directory of the cache.
 */
abstract class CacheProvider(cacheDirectory: String) extends Provider {
  val defaultDbName = "cache.db"
  lazy val cachePath = _cachePath
  lazy val defaultDbPath = cachePath + java.io.File.separator + defaultDbName

  protected var _owner: String = _
  protected var _repository: String = _
  protected var _cachePath: String = _

  protected var _decorators: List[CacheDecorator] = List()
  protected var _pairwiseDecorators: List[CachePairwiseDecorator] = List()

  override def repositoryProvider: Option[RepositoryProvider] = None
  override def pullRequestProvider: Option[PullRequestProvider] = None

  override def init(provider: PullRequestProvider = null): Future[Unit] = Future {
    if (provider != null) {
      _owner = provider.owner
      _repository = provider.repository
    }

    val ownerDir = safeFileName(_owner)
    val repoDir = safeFileName(_repository)
    val file: File = new File(new File(cacheDirectory, ownerDir), repoDir)
    file.mkdirs()

    _cachePath = file.getAbsolutePath
  }

  override def dispose(): Unit = {
    _decorators.foreach(_.dispose())
    _pairwiseDecorators.foreach(_.dispose())
  }

  private def safeFileName(file: String): String = {
    val safe = file.replaceAll("[\\\\/:*?\"<>|]+", "-")
    trim(safe, List(' ', '-'))
  }

  private def trim(str: String, chars: List[Char]): String =
    str.dropWhile(c => chars.contains(c)).reverse.dropWhile(c => chars.contains(c)).reverse
}
