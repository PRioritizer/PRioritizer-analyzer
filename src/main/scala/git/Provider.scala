package git

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Provider {
  val repositoryProvider: Option[RepositoryProvider]
  val pullRequestProvider: Option[PullRequestProvider]
  def getDecorator(list: PullRequestList): PullRequestList
  def getPairwiseDecorator(list: PairwiseList): PairwiseList

  def init(provider: Provider): Future[Unit] = Future {}
  def dispose(): Unit = {}
}
