package git

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ProviderCache(provider: PullRequestProvider) extends PullRequestDecorator(null) {
  var cache: List[PullRequest] = _

  override def get: List[Future[PullRequest]] = {
    if (cache == null) {
      throw new Exception("Cache not initialized. Call init() before using.")
    }

    cache map { pr => Future {pr} }
  }

  def init(): Future[List[PullRequest]] = Future {
    cache = Await.result(provider.get, Duration.Inf)
    cache
  }
}
