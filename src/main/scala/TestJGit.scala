import dispatch.github.GitHub
import git._
import jgit.JGitProvider
import github.GitHubProvider
import org.slf4j.LoggerFactory
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import utils.Stopwatch

object TestJGit {
  val timer = new Stopwatch
  val logger = LoggerFactory.getLogger("Application")
  val inMemoryMerge = true

  def main(args: Array[String]): Unit = {
    var loader: Provider = null

    try {
      logger info s"Setup providers..."
      timer.start()
      loader = new ProviderLoader
      val git: MergeProvider = loader.merger.orNull
      val prs: PullRequestProvider = loader.pullRequests.orNull
      timer.log()

      logger info s"Fetching pull request meta data..."
      val pullRequests = Await.result(prs.get, Duration.Inf)
      logger info s"Got ${pullRequests.length} open pull requests"

      logger info s"Fetching pull requests..."
      git.fetch()

      logger info s"Check for conflicts in PRs (${pullRequests.length})"
      mergePullRequests(git, pullRequests)

      val pairs = PullRequest.getPairs(pullRequests)
      logger info s"Check for conflicts among PRs (${pairs.size})"
      mergePullRequestPairs(git, pairs)

    } finally {
      if (loader != null)
        loader.dispose()
    }
  }

  def mergePullRequests(git: MergeProvider, pullRequests: Traversable[PullRequest]): Unit = {
    pullRequests foreach { pr =>
      if (git merge pr)
        logger info s"MERGED: $pr"
      else
        logger error s"CONFLICT: $pr"
    }
  }

  def mergePullRequestPairs(git: MergeProvider, pairs: Traversable[(PullRequest, PullRequest)]): Unit = {
    pairs foreach { case (pr1, pr2) =>
      if (git merge (pr1, pr2))
        logger info s"MERGED: $pr1 into $pr2"
      else
        logger error s"CONFLICT: $pr1 into $pr2"
    }
  }
}
