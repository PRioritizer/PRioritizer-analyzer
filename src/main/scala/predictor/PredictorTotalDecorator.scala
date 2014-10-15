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
  override def get: Future[List[PullRequest]] = base.get.map { list =>
    val importance = getImportance(list)
    val paired = list.zip(importance)

    paired.map { case (pr, i) =>
      pr.important = Some(i)
      pr
    }
    list
  }

  private def getImportance(pulls: List[PullRequest]): List[Double] = {
    val inputFile = new File(provider.modelDirectory, inputFileName)
    val outputFile = new File(provider.modelDirectory, outputFileName)

    Csv.write(inputFile, pulls)
    Await.ready(provider.predict, Duration.Inf)
    //inputFile.delete

    // Something went wrong, return false
    if (!outputFile.exists)
      return pulls map { p => 0D }

    // Select first column
    val data = Csv.readAsDouble(outputFile)
    //outputFile.delete
    val importance = data map { r => r(0) }

    importance
  }
}
