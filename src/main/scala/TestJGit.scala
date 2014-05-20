import git._
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
      timer.start()
      logger info s"Setup providers..."
      loader = new ProviderLoader
      val git: MergeProvider = loader.merger.orNull
      val prs: PullRequestProvider = loader.pullRequests.orNull
      val data: DataProvider = loader.data.orNull
      timer.logLap()

      logger info s"Fetching pull requests..."
      git.fetch(prs)
      timer.logLap()

      logger info s"Fetching pull request meta data..."
      val simplePullRequests = Await.result(prs.get, Duration.Inf)
      logger info s"Got ${simplePullRequests.length} open pull requests"
      timer.logLap()

      logger info s"Enriching pull request meta data..."
      val pullRequests = simplePullRequests map data.enrich
      timer.logLap()

      logger info s"Check for conflicts in PRs (${pullRequests.length})"
      mergePullRequests(git, pullRequests)
      timer.logLap()

      val pairs = PullRequest.getPairs(pullRequests)
      logger info s"Check for conflicts among PRs (${pairs.size})"
      mergePullRequestPairs(git, pairs)
      timer.logLap()
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
        logger info s"MERGED: #${pr1.number} '${pr1.branch}' into #${pr2.number} '${pr2.branch}'"
      else
        logger error s"CONFLICT: #${pr1.number} '${pr1.branch}' into #${pr2.number} '${pr2.branch}'"
    }
  }
}
