import cache.{CacheWriteProvider, CacheReadProvider}
import git._
import ghtorrent.GHTorrentProvider
import github.GitHubProvider
import jgit.JGitProvider
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ProviderLoader extends Provider {
  object ProviderSettings {
    lazy val repository = Settings.get("provider.repository")
    lazy val pullRequests = Settings.get("provider.requests")

    lazy val single = Settings.get("decorators.single") match {
      case Some(list) => list.split(',').toList; case _ => List()
    }

    lazy val pairwise = Settings.get("decorators.pairwise") match {
      case Some(list) => list.split(',').toList; case _ => List()
    }

    lazy val all = List(
      repository match { case Some(p) => List(p); case _ => List() },
      pullRequests match { case Some(p) => List(p); case _ => List() },
      single, pairwise).flatten
  }

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
    val cacheDir = Settings.get("cache.directory").orNull
    new CacheReadProvider(cacheDir)
  }

  private def createCacheWriteProvider: CacheWriteProvider = {
    val cacheDir = Settings.get("cache.directory").orNull
    new CacheWriteProvider(cacheDir)
  }

  private def createGHTorrentProvider: GHTorrentProvider = {
    val host = Settings.get("ghtorrent.host").orNull
    val port = Settings.get("ghtorrent.port").fold(3306)(p => p.toInt)
    val user = Settings.get("ghtorrent.user").orNull
    val pass = Settings.get("ghtorrent.password").orNull
    val db = Settings.get("ghtorrent.database").orNull
    new GHTorrentProvider(host, port, user, pass, db)
  }

  private def createGitHubProvider: GitHubProvider = {
    val owner = Settings.get("github.owner").orNull
    val repository = Settings.get("github.repository").orNull
    val token = Settings.get("github.token").orNull
    new GitHubProvider(owner, repository, token)
  }

  private def createJGitProvider: JGitProvider = {
    val workingDir = Settings.get("jgit.directory").orNull
    val clean = Settings.get("jgit.clean").fold(false)(c => c.toBoolean)
    new JGitProvider(workingDir, clean)
  }
}
