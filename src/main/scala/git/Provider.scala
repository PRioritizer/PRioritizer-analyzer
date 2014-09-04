package git

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Provider {
  val repositoryProvider: Option[RepositoryProvider] = None
  val pullRequestProvider: Option[PullRequestProvider] = None
  def getDecorator(list: PullRequestList): PullRequestList = list
  def getTotalDecorator(list: TotalList): TotalList = list
  def getPairwiseDecorator(list: PairwiseList): PairwiseList = list

  def init(provider: Provider): Future[Unit] = Future {}
  def dispose(): Unit = {}
}
