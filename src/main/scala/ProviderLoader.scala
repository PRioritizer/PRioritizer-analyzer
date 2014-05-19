import git.{DataProvider, MergeProvider, PullRequestProvider, Provider}
import github.GitHubProvider
import jgit.JGitProvider

class ProviderLoader extends Provider {
  private val providers = scala.collection.mutable.Map[String,Provider]()

  override val pullRequests: Option[PullRequestProvider] = for {
    name <- Settings.get("provider.PullRequests")
    provider <- getProvider(name)
    pullRequests <- provider.pullRequests
  } yield pullRequests

  override val merger: Option[MergeProvider] = for {
    name <- Settings.get("provider.MergeTester")
    provider <- getProvider(name)
    merger <- provider.merger
  } yield merger

  override val data: Option[DataProvider] = for {
    name <- Settings.get("provider.Data")
    provider <- getProvider(name)
    data <- provider.data
  } yield data

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
      case "github" => Some(createGitHubProvider)
      case "jgit" => Some(createJGitProvider)
      case _ => None
    }
  }

  private def createGitHubProvider: GitHubProvider = {
    val owner = Settings.get("github.Owner").orNull
    val repository = Settings.get("github.Repository").orNull
    val token = Settings.get("github.PersonalAccessToken").orNull
    new GitHubProvider(owner, repository, token)
  }

  private def createJGitProvider: JGitProvider = {
    val workingDir = Settings.get("jgit.Directory").orNull
    new JGitProvider(workingDir)
  }
}
