package git

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ProviderToList(provider: PullRequestProvider) extends PullRequestList {
  private var _list: List[PullRequest] = _
  def length = _list.length
  def list = _list

  override def get: List[Future[PullRequest]] = {
    if (_list == null) {
      throw new Exception("List not initialized. Call init() before using.")
    }

    _list map { pr => Future {pr} }
  }

  def init(): Future[List[PullRequest]] = Future {
    _list = Await.result(provider.get, Duration.Inf)
    _list
  }
}
