package predictor

import java.io.{File, PrintWriter, ByteArrayOutputStream}
import git._
import sys.process._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * A provider implementation for the predictor.
 * @param command The location of the predictor script.
 * @param directory The location of the models.
 */
class PredictorProvider(val command: String, val directory: String) extends Provider {

  if (command == null || command == "")
    throw new IllegalArgumentException("Invalid predictor configuration.")

  private var _owner: String = _
  private var _repository: String = _

  def owner = _owner
  def repository = _repository
  def modelDirectory = new File(new File(directory, owner), repository).getPath

  override val repositoryProvider: Option[RepositoryProvider] = None
  override val pullRequestProvider: Option[PullRequestProvider] = None
  override def getDecorator(list: PullRequestList): PullRequestList = new PredictorDecorator(list, this)
  override def getPairwiseDecorator(list: PairwiseList): PairwiseList = list

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

  private def parseCommand(action: String) = command
    .replace("$action", action)
    .replace("$owner", owner)
    .replace("$repository", repository)
}
