package git

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ProviderToList(provider: PullRequestProvider) extends PullRequestList {
  private var list: List[PullRequest] = _
  def length = list.length

  override def get: List[Future[PullRequest]] = {
    if (list == null) {
      throw new Exception("List not initialized. Call init() before using.")
    }

    list map { pr => Future {pr} }
  }

  def init(): Future[List[PullRequest]] = Future {
    list = Await.result(provider.get, Duration.Inf)
    list
  }
}
