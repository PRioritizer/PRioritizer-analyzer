import git._
import ghtorrent.GHTorrentProvider
import github.GitHubProvider
import jgit.JGitProvider
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class ProviderLoader extends Provider {
  private val providers = scala.collection.mutable.Map[String,Provider]()

  override val repositoryProvider: Option[RepositoryProvider] = for {
    name <- Settings.get("provider.RepositoryProvider")
    provider <- getProvider(name)
    repo <- provider.repositoryProvider
  } yield repo

  override val pullRequestProvider: Option[PullRequestProvider] = for {
    name <- Settings.get("provider.PullRequestProvider")
    provider <- getProvider(name)
    pullRequests <- provider.pullRequestProvider
  } yield pullRequests

  override val mergeProvider: Option[MergeProvider] = for {
    name <- Settings.get("provider.MergeProvider")
    provider <- getProvider(name)
    merger <- provider.mergeProvider
  } yield merger

  override def getDecorator(list: PullRequestList): Option[PullRequestList] = {
    val providers = for { name <- Settings.get("provider.PullRequestDecorators") } yield
      name.split(',').toList.map(getProvider).flatMap(o => o)

    for (plist <- providers) yield {
      var decorator: PullRequestList = list
      plist.foreach { provider =>
        provider.getDecorator(decorator) match {
          case Some(d) => decorator = d
          case _ => throw new Exception("Provider doesn't have the appropriate decorator")
        }
      }
      decorator
    }
  }

  override def dispose(): Unit = {
    providers.values
      .filter(p => p != null)
      .foreach(p => p.dispose())
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

  private def createGHTorrentProvider: GHTorrentProvider = {
    val host = Settings.get("ghtorrent.Host").orNull
    val port = Settings.get("ghtorrent.Port").fold(3306)(p => p.toInt)
    val user = Settings.get("ghtorrent.User").orNull
    val pass = Settings.get("ghtorrent.Password").orNull
    val db = Settings.get("ghtorrent.Database").orNull
    val owner = Settings.get("github.Owner").orNull
    val repository = Settings.get("github.Repository").orNull
    new GHTorrentProvider(host, port, user, pass, db, owner, repository)
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
