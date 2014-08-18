package predictor

import java.io.File

import git._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * An predictor implementation that asks an external program to predict the importance.
 * @param provider The cache provider.
 */
class PredictorDecorator(base: PullRequestList, val provider: PredictorProvider) extends PullRequestDecorator(base) {
  val inputFileName = "input.csv"
  val outputFileName = "output.csv"

  // Don't invoke the process for every PR, but for the whole list at once
  override def get: List[Future[PullRequest]] = {
    val pulls = base.get
    val importance = getImportance(pulls)
    val paired = pulls.zip(importance)

    paired.map { case (prFuture, iFuture) =>
      for {
        pr <- prFuture
        i <- iFuture
      } yield {
        pr.important = Some(i)
        pr
      }
    }
  }

  private def getImportance(pulls: List[Future[PullRequest]]): List[Future[Boolean]] = {
    val inputFile = new File(provider.modelDirectory, inputFileName)
    val outputFile = new File(provider.modelDirectory, outputFileName)

    val importance = Future.sequence(pulls) map { list =>
      Csv.write(inputFile, list)
      Await.ready(provider.predict, Duration.Inf)
      inputFile.delete

      // Something went wrong, return false
      if (!outputFile.exists)
        return pulls map { p => Future(false) }

      // Select first column
      val data = Csv.readAsBoolean(outputFile)
      outputFile.delete
      data map { r => r(0) }
    }

    toListOfFutures(importance, pulls.length)
  }

  private def toListOfFutures[T](list: Future[List[T]], count: Int): List[Future[T]] = {
    (1 to count).toList map { i =>
      list map (_.apply(i-1))
    }
  }
}
