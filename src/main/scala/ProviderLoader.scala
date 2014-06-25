import git._
import ghtorrent.GHTorrentProvider
import github.GitHubProvider
import jgit.JGitProvider
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ProviderLoader extends Provider {
  private val providers = scala.collection.mutable.Map[String,Provider]()
  private val settings = Map(
    "Repository" -> Settings.get("provider.RepositoryProvider"),
    "PullRequest" -> Settings.get("provider.PullRequestProvider"),
    "Single" -> Settings.get("provider.SingleDecorators"),
    "Pairwise" -> Settings.get("provider.PairwiseDecorators")
  )

  override val repositoryProvider: Option[RepositoryProvider] = for {
    name <- settings.get("Repository").flatten
    provider <- getProvider(name)
    repo <- provider.repositoryProvider
  } yield repo

  override val pullRequestProvider: Option[PullRequestProvider] = for {
    name <- settings.get("PullRequest").flatten
    provider <- getProvider(name)
    pullRequests <- provider.pullRequestProvider
  } yield pullRequests

  override def getDecorator(list: PullRequestList): PullRequestList = {
    val providers = for { name <- settings.get("Single").flatten } yield
      name.split(',').toList.map(getProvider).flatMap(o => o)

    var decorator = list
    for(plist <- providers) {
      plist.foreach { provider =>
        decorator = provider.getDecorator(decorator)
      }
    }
    decorator
  }

  override def getPairwiseDecorator(list: PairwiseList): PairwiseList = {
    val providers = for { name <- settings.get("Pairwise").flatten } yield
      name.split(',').toList.map(getProvider).flatMap(o => o)

    var decorator = list
    for(plist <- providers) {
      plist.foreach { provider =>
        decorator = provider.getPairwiseDecorator(decorator)
      }
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
    settings.values.flatten.map { list => list.split(',').toList.map(getProvider) }
  }

  private def getProvider(name: String): Option[Provider] = {
    if (!providers.contains(name))
      for (provider <- createProvider(name))
        providers += name -> provider

    providers.get(name)
  }

  private def createProvider(name: String): Option[Provider] = {
    name match {
      case "ghtorrent" => Some(createGHTorrentProvider)
      case "github" => Some(createGitHubProvider)
      case "jgit" => Some(createJGitProvider)
      case _ => None
    }
  }

  private def createCacheProvider: CacheProvider = {
    val cacheDir = Settings.get("cache.Directory").orNull
    new CacheProvider(cacheDir)
  }

  private def createGHTorrentProvider: GHTorrentProvider = {
    val host = Settings.get("ghtorrent.Host").orNull
    val port = Settings.get("ghtorrent.Port").fold(3306)(p => p.toInt)
    val user = Settings.get("ghtorrent.User").orNull
    val pass = Settings.get("ghtorrent.Password").orNull
    val db = Settings.get("ghtorrent.Database").orNull
    new GHTorrentProvider(host, port, user, pass, db)
  }

  private def createGitHubProvider: GitHubProvider = {
    val owner = Settings.get("github.Owner").orNull
    val repository = Settings.get("github.Repository").orNull
    val token = Settings.get("github.PersonalAccessToken").orNull
    new GitHubProvider(owner, repository, token)
  }

  private def createJGitProvider: JGitProvider = {
    val workingDir = Settings.get("jgit.Directory").orNull
    val clean = Settings.get("jgit.Clean").fold(false)(c => c.toBoolean)
    new JGitProvider(workingDir, clean)
  }
}
