import git._
import org.slf4j.LoggerFactory
import output.JsonWriter
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import utils.{ProgressMonitor, Stopwatch}
import scala.concurrent.ExecutionContext.Implicits.global

object Analyze {
  val timer = new Stopwatch
  val monitor = new ProgressMonitor
  val logger = LoggerFactory.getLogger("Application")
  val inMemoryMerge = true

  def main(args: Array[String]): Unit = {
    var loader: Provider = null
    var pullRequests: List[PullRequest] = null
    val skipDifferentTargets = Settings.get("settings.pairs.skipDifferentTargets").get.toBoolean

    try {
      timer.start()
      logger info s"Setup providers..."
      loader = new ProviderLoader
      val provider: PullRequestProvider = loader.pullRequestProvider.orNull
      val prs: PullRequestList = new MemoryCache(provider)
      logger info s"Setup done"
      timer.logLap()

      logger info s"Fetching pull requests..."
      logger info s"Fetching pull request meta data..."
      val fetch: Future[Unit] = loader.init()
      val fetchFutures = prs.get

      // Wait for fetch to complete
      Await.ready(fetch, Duration.Inf)
      pullRequests = Await.result(fetchFutures, Duration.Inf)
      logger info s"Fetching done"
      logger info s"Got ${pullRequests.length} open pull requests"
      timer.logLap()

      logger info s"Enriching pull request meta data..."
      logger info s"Merging PRs... (${pullRequests.length})"
      val data: PullRequestList = loader.getDecorator(prs)
      val enrichFutures = data.get

      // Wait for enrichment to complete
      pullRequests = Await.result(enrichFutures, Duration.Inf)
      logger info s"Enriching done"
      timer.logLap()

      val largePullRequests = getLargePullRequests(pullRequests)
      val pairs = new Pairwise(pullRequests.diff(largePullRequests), skipDifferentTargets)
      val git: PairwiseList = loader.getPairwiseDecorator(pairs)

      logger info s"Pairwise merging PRs... (${pairs.length})"
      logger info s"Skip too large pairs (${largePullRequests.length})"
      //monitor.total = pullRequests.length + pairs.length
      val pairFutures = git.get

      // Wait for merges to complete
      val pairResults = Await.result(pairFutures, Duration.Inf)
      logger info s"Merging done"
      timer.logLap()

      logger info s"Combining results..."
      pullRequests = Pairwise.unpair(pairResults)
      logger info s"Combining done"
      timer.logLap()

      // Output pull requests
      JsonWriter.writePullRequests("pull-requests.json", loader, pullRequests)
    } finally {
      if (loader != null)
        loader.dispose()
    }
  }

  def getLargePullRequests(pullRequests: List[PullRequest]): List[PullRequest] = {
    val large = Settings.get("settings.large").get.toInt
    val skipLarge = Settings.get("settings.pairs.skipLarge").get.toBoolean

    if (skipLarge)
      pullRequests filter {pr => pr.linesTotal > large}
    else
      List()
  }
}
