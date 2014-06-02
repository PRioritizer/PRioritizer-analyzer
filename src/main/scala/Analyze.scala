import git._
import git.MergeResult._
import org.slf4j.LoggerFactory
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import utils.Stopwatch
import scala.concurrent.ExecutionContext.Implicits.global

object Analyze {
  val timer = new Stopwatch
  val logger = LoggerFactory.getLogger("Application")
  val inMemoryMerge = true

  def main(args: Array[String]): Unit = {
    var loader: Provider = null
    var pullRequests: List[PullRequest] = null

    try {
      timer.start()
      logger info s"Setup providers..."
      loader = new ProviderLoader
      val git: MergeProvider = loader.merger.orNull
      val prs: PullRequestProvider = loader.pullRequests.orNull
      val data: DataProvider = loader.data.orNull
      logger info s"Setup done"
      timer.logLap()

      logger info s"Fetching pull requests..."
      logger info s"Fetching pull request meta data..."
      val fetch: Future[Unit] = git.fetch(prs)
      val fetchFutures = prs.get

      // Wait for fetch to complete
      Await.ready(fetch, Duration.Inf)
      pullRequests = Await.result(fetchFutures, Duration.Inf)
      logger info s"Fetching done"
      logger info s"Got ${pullRequests.length} open pull requests"
      timer.logLap()

      logger info s"Enriching pull request meta data..."

      val enrichFutures = Future.sequence(pullRequests map data.enrich)

      // Wait for enrichment to complete
      pullRequests = Await.result(enrichFutures, Duration.Inf)
      logger info s"Enriching done"
      timer.logLap()

      logger info s"Merging PRs... (${pullRequests.length})"
      val mergeFutures = mergePullRequests(git, pullRequests)

      val largePullRequests = getLargePullRequests(pullRequests)
      val pairs = getPairs(pullRequests.diff(largePullRequests))
      logger info s"Skip too large PRs (${largePullRequests.length})"
      logger info s"Pairwise merging PRs... (${pairs.size})"
      val pairFutures = mergePullRequestPairs(git, pairs)

      // Wait for merges to complete
      pullRequests = Await.result(mergeFutures, Duration.Inf)
      val pairResults = Await.result(pairFutures, Duration.Inf)
      logger info s"Merging done"
      timer.logLap()

      logger info s"Combining results..."
      pullRequests = combineResults(pullRequests, pairResults)
      logger info s"Combining done"
      timer.logLap()

      // TODO: Output pull requests
    } finally {
      if (loader != null)
        loader.dispose()
    }
  }

  def getLargePullRequests(pullRequests: List[PullRequest]): List[PullRequest] = {
    val large = Settings.get("settings.large").get.toInt
    val skipLarge = Settings.get("settings.pairs.skipLarge").get.toBoolean

    if (skipLarge)
      pullRequests filter {pr => pr.lineCount > large}
    else
      List()
  }

  /**
   * Get the pull request pairs.
   * Reduce the number of pairs by filtering out pairs with PRs that target two different branches
   * @param pullRequests The pull requests
   * @return Pairwise combination of the pull requests.
   */
  def getPairs(pullRequests: List[PullRequest]): List[(PullRequest, PullRequest)] = {
    val skipDifferentTargets = Settings.get("settings.pairs.skipDifferentTargets").get.toBoolean

    if (skipDifferentTargets)
      PullRequest.getPairs(pullRequests) filter { case (pr1, pr2) => pr1.target == pr2.target }
    else
      PullRequest.getPairs(pullRequests)
  }

  def mergePullRequests(git: MergeProvider, pullRequests: List[PullRequest]): Future[List[PullRequest]] = {
    val results = pullRequests map { pr =>
      for (res <- git.merge(pr)) yield {
        pr.isMergeable = res == Merged
        pr
      }
    }
    Future.sequence(results)
  }

  def mergePullRequestPairs(git: MergeProvider, pairs: List[(PullRequest, PullRequest)]): Future[List[(PullRequest, PullRequest, MergeResult)]] = {
    val results = pairs map { case (pr1, pr2) =>
      for (res <- git merge (pr1, pr2))
      yield (pr1, pr2, res)
    }
    Future.sequence(results)
  }

  def combineResults(pullRequests: List[PullRequest], results: List[(PullRequest, PullRequest, MergeResult)]): List[PullRequest] = {
    pullRequests.foreach { pr =>
      pr.conflictsWith = results filter {
        case (pr1, pr2, res) =>
          res != Merged && (pr1 == pr || pr2 == pr)
      } map {
        case (pr1, pr2, res) =>
          if (pr1 == pr) pr2 else pr1
      }
    }
    pullRequests
  }
}
