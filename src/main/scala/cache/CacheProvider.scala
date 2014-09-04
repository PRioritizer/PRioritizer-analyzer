package cache

import java.io.File

import git._
import utils.Extensions._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.slick.driver.SQLiteDriver.simple._

/**
 * A provider implementation for the disk cache.
 */
abstract class CacheProvider extends Provider {
  val dbName = "cache.db"
  val dbDriver = "org.sqlite.JDBC"
  val mode = CacheMode.None
  lazy val cachePath = _cachePath
  lazy val dbPath = cachePath + java.io.File.separator + dbName
  lazy val dbUrl = s"jdbc:sqlite:$dbPath"
  lazy val Db = Database.forURL(dbUrl, driver = dbDriver).createSession()

  protected var _cachePath: String = _

  protected var _decorators: List[CacheDecorator] = List()
  protected var _pairwiseDecorators: List[CachePairwiseDecorator] = List()

  override val repositoryProvider: Option[RepositoryProvider] = None
  override val pullRequestProvider: Option[PullRequestProvider] = None

  override def init(provider: Provider): Future[Unit] = Future {
    if (provider == null || provider.pullRequestProvider.orNull == null)
      throw new IllegalArgumentException("Need a pull request provider to initialize other providers.")

    val ownerDir = provider.pullRequestProvider.get.owner.safeFileName
    val repoDir = provider.pullRequestProvider.get.repository.safeFileName
    val file: File = new File(new File(CacheSettings.directory, ownerDir), repoDir)
    file.mkdirs()

    _cachePath = file.getAbsolutePath
  }

  override def dispose(): Unit = {
    try {
      Db.close()
    } catch {
      case _ : Exception =>
    }
  }
}
