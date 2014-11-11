import git._
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import output.JsonWriter
import settings.GeneralSettings
import utils.Extensions._
import utils.{ProgressMonitor, Stopwatch}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Analyzer {
  val logger = LoggerFactory.getLogger("Analyzer")
  val stopwatch = new Stopwatch

  def main(args: Array[String]): Unit = {
    var loader: ProviderLoader = null

    try {
      stopwatch.start()
      loader = new ProviderLoader
      val monitor = new ProgressMonitor(GeneralSettings.monitorIntervalType, GeneralSettings.monitorIntervalValue)
      val prProvider = loader.pullRequestProvider.orNull
      val simplePulls = new ProviderToList(prProvider)
      logger info s"Setup - Done"

      // Check for update interval
      val file = JsonWriter.getFile(GeneralSettings.outputDirectory, prProvider)
      val fileMod = new DateTime(file.lastModified)
      val expires = fileMod.plusSeconds(GeneralSettings.updateInterval)
      if (DateTime.now.isBefore(expires)) {
        logger warn s"Skip - Already recently updated"
        return
      } else if (GeneralSettings.updateTimestamp > 0 && fileMod.isAfter(GeneralSettings.updateTimestamp * 1000)) {
        logger warn s"Skip - Already up-to-date with respect to the provided timestamp"
        return
      }

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

      logger info s"Total - Start"
      // Apply other set of decorators on the complete list
      val total = new Total(pullRequests)
      val totalDecorator: TotalList = loader.getTotalDecorator(total)
      val decorationOfTotal = totalDecorator.get

      // Wait for decoration to complete
      val totalPullRequests = Await.result(decorationOfTotal, Duration.Inf)
      logger info s"Total - End"

      logger info s"Pairwise - Start"
      val simplePairs = new Pairwise(totalPullRequests, GeneralSettings.pairTargetsEqual)
      val pairDecorator: PairwiseList = loader.getPairwiseDecorator(simplePairs)
      val decorationOfPairs = pairDecorator.get
      monitor.reset()
      monitor.setTotal(simplePairs.length)
      monitor.incrementWhen(decorationOfPairs)

      // Wait for decoration to complete
      val pairs = Await.result(Future.sequence(decorationOfPairs), Duration.Inf)
      logger info s"Pairwise - End"

      // Output pull requests
      logger info s"Write - Start"
      val unpairedPullRequests = Pairwise.unpair(pairs)
      JsonWriter.writePullRequests(GeneralSettings.outputDirectory, loader, unpairedPullRequests)
      if (GeneralSettings.outputIndex)
        JsonWriter.writeIndex(GeneralSettings.outputDirectory)
      logger info s"Write - End"
      stopwatch.logMinutes()
    } catch {
      case e: Exception =>
        logger error s"Error - ${e.getMessage}"
        logger error s"Stack trace - Begin\n${e.stackTraceToString}"
        logger error s"Stack trace - End"
    } finally {
      if (loader != null)
        loader.dispose()
    }
  }
}
