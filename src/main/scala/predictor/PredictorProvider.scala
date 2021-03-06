package predictor

import java.io.File

import git._
import utils.Extensions._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process._

/**
 * A provider implementation for the predictor.
 */
class PredictorProvider extends Provider {

  if (!PredictorSettings.validate)
    throw new IllegalArgumentException("Invalid predictor configuration.")

  private var _owner: String = _
  private var _repository: String = _

  def owner = _owner
  def repository = _repository
  def modelDirectory = {
    val ownerDir = owner.toLowerCase.safeFileName
    val repoDir = repository.toLowerCase.safeFileName
    new File(new File(PredictorSettings.directory, ownerDir), repoDir).getPath
  }

  override def getTotalDecorator(list: TotalList): TotalList = new PredictorTotalDecorator(list, this)

  override def init(provider: Provider): Future[Unit] = {
    if (provider != null && provider.pullRequestProvider.orNull != null) {
      _owner = provider.pullRequestProvider.get.owner
      _repository = provider.pullRequestProvider.get.repository
    }

    // Make sure the model is trained
    train map { _ => }
  }

  def train = Future(parseCommand("train").! == 0)

  def predict = Future(parseCommand("predict").! == 0)

  private def parseCommand(action: String) = PredictorSettings.command
    .replace("$action", action)
    .replace("$owner", owner)
    .replace("$repository", repository)
}
