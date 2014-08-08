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
  override def getDecorator(list: PullRequestList): PullRequestList = list
  override def getPairwiseDecorator(list: PairwiseList): PairwiseList = list

  override def init(provider: PullRequestProvider = null): Future[Unit] = Future {
    if (provider != null) {
      _owner = provider.owner
      _repository = provider.repository

      // Make sure the model is trained
      train
    }
  }

  def train = trainCommand.!

  def predict: Future[List[Boolean]] = Future {
    val (result, output, _) = runWithOutput(trainCommand)

    // Parse output
    if (result)
      output.trim.split('\n').map(b => b.trim.toBoolean).toList
    else
      List()
  }

  private def trainCommand = parseCommand("train")

  private def predictCommand = parseCommand("predict")

  private def parseCommand(action: String) = command
    .replace("$action", action)
    .replace("$owner", owner)
    .replace("$repository", repository)

  private def runWithOutput(command: String): (Boolean, String, String) = {
    val stdout = new ByteArrayOutputStream
    val stderr = new ByteArrayOutputStream
    val stdoutWriter = new PrintWriter(stdout)
    val stderrWriter = new PrintWriter(stderr)

    // Start process
    val exitValue = command ! ProcessLogger(stdoutWriter.println, stderrWriter.println)
    stdoutWriter.close()
    stderrWriter.close()
    (exitValue == 0, stdout.toString, stderr.toString)
  }
}
