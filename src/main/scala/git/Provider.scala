package git

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Provider {
  def repositoryProvider: Option[RepositoryProvider]
  def pullRequestProvider: Option[PullRequestProvider]
  def getDecorator(list: PullRequestList): PullRequestList
  def getPairwiseDecorator(list: PairwiseList): PairwiseList

  def init(provider: PullRequestProvider = null): Future[Unit] = Future {}
  def dispose(): Unit = {}
}
