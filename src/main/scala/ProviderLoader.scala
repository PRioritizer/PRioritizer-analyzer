import cache.{CacheWriteProvider, CacheReadProvider}
import git._
import ghtorrent.GHTorrentProvider
import github.GitHubProvider
import jgit.JGitProvider
import settings._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ProviderLoader extends Provider {
  private val providers = scala.collection.mutable.Map[String, Provider]()

  override val repositoryProvider: Option[RepositoryProvider] = for {
    name <- ProviderSettings.repository
    provider <- getProvider(name)
    repo <- provider.repositoryProvider
  } yield repo

  override val pullRequestProvider: Option[PullRequestProvider] = for {
    name <- ProviderSettings.pullRequests
    provider <- getProvider(name)
    pullRequests <- provider.pullRequestProvider
  } yield pullRequests

  override def getDecorator(list: PullRequestList): PullRequestList = {
    val providers = ProviderSettings.single.map(getProvider).flatMap(o => o)

    var decorator = list
    providers.foreach { provider =>
      decorator = provider.getDecorator(decorator)
    }
    decorator
  }

  override def getPairwiseDecorator(list: PairwiseList): PairwiseList = {
    val providers = ProviderSettings.pairwise.map(getProvider).flatMap(o => o)

    var decorator = list
    providers.foreach { provider =>
      decorator = provider.getPairwiseDecorator(decorator)
    }
    decorator
  }

  override def init(provider: PullRequestProvider = null): Future[Unit] = {
    loadAll()
    val pProvider = if (provider != null) provider else pullRequestProvider.orNull
    val future = Future.sequence(providers.values.map(p => p.init(pProvider)))
    for(f <- future) yield {}
  }

  override def dispose(): Unit = {
    providers.values
      .filter(p => p != null)
      .foreach(p => p.dispose())
  }

  private def loadAll(): Unit = {
    ProviderSettings.all.map(getProvider)
  }

  private def getProvider(name: String): Option[Provider] = {
    if (!providers.contains(name))
      for (provider <- createProvider(name))
        providers += name -> provider

    providers.get(name)
  }

  private def createProvider(name: String): Option[Provider] = {
    name match {
      case "cache-read" => Some(createCacheReadProvider)
      case "cache-write" => Some(createCacheWriteProvider)
      case "ghtorrent" => Some(createGHTorrentProvider)
      case "github" => Some(createGitHubProvider)
      case "jgit" => Some(createJGitProvider)
      case _ => None
    }
  }

  private def createCacheReadProvider: CacheReadProvider = {
    new CacheReadProvider(CacheSettings.directory)
  }

  private def createCacheWriteProvider: CacheWriteProvider = {
    new CacheWriteProvider(CacheSettings.directory)
  }

  private def createGHTorrentProvider: GHTorrentProvider = {
    new GHTorrentProvider(
      GHTorrentSettings.host,
      GHTorrentSettings.port,
      GHTorrentSettings.username,
      GHTorrentSettings.password,
      GHTorrentSettings.database
    )
  }

  private def createGitHubProvider: GitHubProvider = {
    new GitHubProvider(
      GitHubSettings.owner,
      GitHubSettings.repository,
      GitHubSettings.token
    )
  }

  private def createJGitProvider: JGitProvider = {
    new JGitProvider(
      JGitSettings.directory,
      JGitSettings.clean
    )
  }
}
