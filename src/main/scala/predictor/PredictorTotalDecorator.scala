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
class PredictorTotalDecorator(base: TotalList, val provider: PredictorProvider) extends TotalDecorator(base) {
  val inputFileName = "input.csv"
  val outputFileName = "output.csv"

  // Don't invoke the process for every PR, but for the whole list at once
  override def get: Future[List[PullRequest]] = {
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

  private def getImportance(pulls: Future[List[PullRequest]]): Future[List[Boolean]] = {
    val inputFile = new File(provider.modelDirectory, inputFileName)
    val outputFile = new File(provider.modelDirectory, outputFileName)

    val importance = pulls map { list =>
      Csv.write(inputFile, list)
      Await.ready(provider.predict, Duration.Inf)
      inputFile.delete

      // Something went wrong, return false
      if (!outputFile.exists)
        return pulls map { list => list.map { p => false } }

      // Select first column
      val data = Csv.readAsBoolean(outputFile)
      outputFile.delete
      data map { r => r(0) }
    }

    importance
  }
}
