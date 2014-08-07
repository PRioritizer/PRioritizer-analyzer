package predictor

import git._
import sys.process._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * A provider implementation for the predictor.
 * @param command The location of the predictor script.
 */
class PredictorProvider(val command: String) extends Provider {

  if (command == null || command == "")
    throw new IllegalArgumentException("Invalid predictor configuration.")

  private var _owner: String = _
  private var _repository: String = _

  def owner = _owner
  def repository = _repository

  override val repositoryProvider: Option[RepositoryProvider] = None
  override val pullRequestProvider: Option[PullRequestProvider] = None
  override def getDecorator(list: PullRequestList): PullRequestList = list
  override def getPairwiseDecorator(list: PairwiseList): PairwiseList = list

  override def init(provider: PullRequestProvider = null): Future[Unit] = Future {
    if (provider != null) {
      _owner = provider.owner
      _repository = provider.repository

      // Make sure the model is trained
      trainCommand.!
    }
  }

  private def trainCommand = parseCommand("train")

  private def predictCommand = parseCommand("predict")

  private def parseCommand(action: String) = command
    .replace("$action", action)
    .replace("$owner", owner)
    .replace("$repository", repository)
}
