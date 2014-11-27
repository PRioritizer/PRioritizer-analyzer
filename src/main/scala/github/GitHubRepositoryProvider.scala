package github

import dispatch.github.GhRepository
import git.RepositoryProvider

import scala.concurrent.duration.Duration
import scala.concurrent.Await

/**
 * An info getter implementation for the GHTorrent database.
 * @param provider The GHTorrent provider.
 */
class GitHubRepositoryProvider(val provider: GitHubProvider) extends RepositoryProvider {
  lazy val defaultBranch = getDefaultBranch
  lazy val branchTips = getBranchTips
  lazy val repo = getRepository

  private def getDefaultBranch: String = repo.default_branch

  private def getRepository: GhRepository = {
    val repository = GhRepository.get_repository(provider.owner, provider.repository)
    Await.result(repository, Duration.Inf)
  }

  private def getBranchTips: Map[String, String] = {
    val req = GhRepository.get_branches(provider.owner, provider.repository)
    val branches = Await.result(req, Duration.Inf)
    val tmp = branches map { b => (b.name, b.commit.sha) }
    tmp.toMap
  }
}
