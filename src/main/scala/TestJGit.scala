import git._
import git.MergeResult._
import org.slf4j.LoggerFactory
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import utils.Stopwatch
import scala.concurrent.ExecutionContext.Implicits.global

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
      logger info s"Setup done"
      timer.logLap()

      logger info s"Fetching pull requests..."
      val fetch = git.fetch(prs)

      logger info s"Fetching pull request meta data..."
      val fetchPulls = prs.get

      // Wait for fetch to complete
      Await.ready(fetch, Duration.Inf)
      logger info s"Fetching done"
      val simplePullRequests = Await.result(fetchPulls, Duration.Inf)
      logger info s"Got ${simplePullRequests.length} open pull requests"
      timer.logLap()

      logger info s"Enriching pull request meta data..."
      val enrichPulls = dispatch.Future.sequence(simplePullRequests map data.enrich)

      // Wait for enrichment to complete
      val pullRequests = Await.result(enrichPulls, Duration.Inf)
      logger info s"Enriching done"
      timer.logLap()

      logger info s"Check for conflicts in PRs (${pullRequests.length})"
      val merges = mergePullRequests(git, pullRequests)

      val largePullRequests = getLargePullRequests(pullRequests)
      logger info s"Skip too large PRs (${largePullRequests.length})"
      val pairs = getPairs(pullRequests.diff(largePullRequests))

      logger info s"Check for conflicts among PRs (${pairs.size})"
      val pairMerges = mergePullRequestPairs(git, pairs)

      // Wait for merges to complete
      val allMerges = dispatch.Future.sequence(Seq(merges, pairMerges))
      Await.ready(allMerges, Duration.Inf)
      logger info s"Merging done"
      timer.log()
      timer.logMinutes()
    } finally {
      if (loader != null)
        loader.dispose()
    }
  }

  def getLargePullRequests(pullRequests: List[RichPullRequest]): List[RichPullRequest] = {
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
  def getPairs(pullRequests: List[RichPullRequest]): Traversable[(PullRequest, PullRequest)] = {
    val skipDifferentTargets = Settings.get("settings.pairs.skipDifferentTargets").get.toBoolean

    if (skipDifferentTargets)
      PullRequest.getPairs(pullRequests) filter { case (pr1, pr2) => pr1.target == pr2.target }
    else
      PullRequest.getPairs(pullRequests)
  }

  def mergePullRequests(git: MergeProvider, pullRequests: Traversable[PullRequest]): Future[Traversable[MergeResult]] = {
    val results = pullRequests map { pr => {
      val res = git merge pr
      res.onSuccess {
        case Merged =>
          logger info s"MERGED: $pr"
        case Conflict =>
          logger info s"CONFLICT: $pr"
        case Error =>
          logger error s"ERROR: $pr"
      }
      res
    }}

    dispatch.Future.sequence(results)
  }

  def mergePullRequestPairs(git: MergeProvider, pairs: Traversable[(PullRequest, PullRequest)]): Future[Traversable[MergeResult]] = {
    val results = pairs map { case (pr1, pr2) => {
      val res = git merge (pr1, pr2)
      res.onSuccess {
        case Merged =>
          logger info s"MERGED: #${pr1.number} '${pr1.branch}' into #${pr2.number} '${pr2.branch}'"
        case Conflict =>
          logger info s"CONFLICT: #${pr1.number} '${pr1.branch}' into #${pr2.number} '${pr2.branch}'"
        case Error =>
          logger error s"ERROR: #${pr1.number} '${pr1.branch}' into #${pr2.number} '${pr2.branch}'"
      }
      res
    }}

    dispatch.Future.sequence(results)
  }
}
