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

  def main(args: Array[String]): Unit = {
    var loader: Provider = null
    val skipDifferentTargets = Settings.get("settings.pairs.skipDifferentTargets").get.toBoolean

    try {
      timer.start()
      logger info s"Setup providers..."
      loader = new ProviderLoader
      val simplePulls = new ProviderCache(loader.pullRequestProvider.orNull)
      logger info s"Setup done"
      timer.logLap()

      logger info s"Fetching pull requests..."
      logger info s"Fetching pull request meta data..."
      val fetchGit: Future[Unit] = loader.init()
      val fetchPulls = simplePulls.init()

      // Wait for fetch to complete
      Await.ready(fetchGit, Duration.Inf)
      Await.ready(fetchPulls, Duration.Inf)
      logger info s"Fetching done"
      logger info s"Got ${simplePulls.length} open pull requests"
      timer.logLap()

      logger info s"Enriching pull request meta data..."
      logger info s"Merging PRs... (${simplePulls.length})"
      val pullDecorator: PullRequestList = loader.getDecorator(simplePulls)
      val decorationOfPulls = pullDecorator.get

      // Wait for enrichment to complete
      val pullRequests = Await.result(Future.sequence(decorationOfPulls), Duration.Inf)
      logger info s"Enriching done"
      timer.logLap()

      val largePullRequests = getLargePullRequests(pullRequests)
      val simplePairs = new Pairwise(pullRequests.diff(largePullRequests), skipDifferentTargets)
      val pairDecorator: PairwiseList = loader.getPairwiseDecorator(simplePairs)

      logger info s"Pairwise merging PRs... (${simplePairs.length})"
      logger info s"Skip too large pairs (${largePullRequests.length})"
      val decorationOfPairs = pairDecorator.get

      // Wait for merges to complete
      val pairs = Await.result(Future.sequence(decorationOfPairs), Duration.Inf)
      logger info s"Merging done"
      timer.logLap()

      // Output pull requests
      val unpairedPullRequests = Pairwise.unpair(pairs)
      JsonWriter.writePullRequests("pull-requests.json", loader, unpairedPullRequests)
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
