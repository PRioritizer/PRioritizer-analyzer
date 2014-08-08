package predictor

import java.io.File

import git._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * An predictor implementation that asks an external program to predict the importance.
 * @param provider The cache provider.
 */
class PredictorDecorator(base: PullRequestList, val provider: PredictorProvider) extends PullRequestDecorator(base) {
  val inputFile = "input.csv"
  val outputFile = "output.csv"

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
    val importance = Future.sequence(pulls) map { list =>
      Csv.write(new File(provider.modelDirectory, inputFile), list)
      Await.ready(provider.predict, Duration.Inf)
      val data = Csv.readAsBoolean(new File(provider.modelDirectory, outputFile))

      // Select first column
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
