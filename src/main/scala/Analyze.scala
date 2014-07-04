import git._
import org.slf4j.LoggerFactory
import output.JsonWriter
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import utils.ProgressMonitor
import utils.Extensions._
import scala.concurrent.ExecutionContext.Implicits.global

object Analyze {
  val logger = LoggerFactory.getLogger("Analyzer")

  def main(args: Array[String]): Unit = {
    var loader: Provider = null

    try {
      loader = new ProviderLoader
      val monitor = newMonitor
      val skipDifferentTargets = Settings.get("pairs.targets.equal").get.toBoolean
      val outputDir = Settings.get("output.directory").get
      val prProvider = loader.pullRequestProvider.orNull
      val simplePulls = new ProviderToList(prProvider)
      logger info s"Setup - Done"

      logger info s"Fetch - Start"
      val fetchGit: Future[Unit] = loader.init()
      val fetchPulls = simplePulls.init()

      // Wait for fetch to complete
      Await.result(fetchGit, Duration.Inf)
      Await.result(fetchPulls, Duration.Inf)
      logger info s"Fetch - End"

      logger info s"Single - Start"
      val pullDecorator: PullRequestList = loader.getDecorator(simplePulls)
      val decorationOfPulls = pullDecorator.get
      monitor.setTotal(simplePulls.length)
      monitor.incrementWhen(decorationOfPulls)

      // Wait for decoration to complete
      val pullRequests = Await.result(Future.sequence(decorationOfPulls), Duration.Inf)
      logger info s"Single - End"

      logger info s"Pairwise - Start"
      val simplePairs = new Pairwise(pullRequests, skipDifferentTargets)
      val pairDecorator: PairwiseList = loader.getPairwiseDecorator(simplePairs)
      val decorationOfPairs = pairDecorator.get
      monitor.reset()
      monitor.setTotal(simplePairs.length)
      monitor.incrementWhen(decorationOfPairs)

      // Wait for decoration to complete
      val pairs = Await.result(Future.sequence(decorationOfPairs), Duration.Inf)
      logger info s"Pairwise - End"

      // Output pull requests
      val unpairedPullRequests = Pairwise.unpair(pairs)
      JsonWriter.writePullRequests(outputDir, loader, unpairedPullRequests)
      logger info s"Output - Done"
    } catch {
      case e: Exception =>
        logger error s"Error - ${e.getMessage}"
        logger error s"Stace trace - Begin\n${e.stackTraceToString}"
        logger error s"Stace trace - End"
    } finally {
      if (loader != null)
        loader.dispose()
    }
  }

  def newMonitor: ProgressMonitor = {
    val intervalType = Settings.get("monitor.interval.type").get
    val intervalValue = Settings.get("monitor.interval.value").get.toInt
    new ProgressMonitor(intervalType, intervalValue)
  }
}
