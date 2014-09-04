import cache.{CacheReadProvider, CacheWriteProvider}
import ghtorrent.GHTorrentProvider
import git._
import github.GitHubProvider
import jgit.JGitProvider
import predictor.PredictorProvider
import settings._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

  override def init(provider: Provider = null): Future[Unit] = {
    loadAll()
    val future = Future.sequence(providers.values.map(p => p.init(this)))
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
      case "cache-read" => Some(new CacheReadProvider)
      case "cache-write" => Some(new CacheWriteProvider)
      case "ghtorrent" => Some(new GHTorrentProvider)
      case "github" => Some(new GitHubProvider)
      case "jgit" => Some(new JGitProvider)
      case "predictor" => Some(new PredictorProvider)
      case "none" => Some(new EmptyProvider)
      case _ => None
    }
  }
}
