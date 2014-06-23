package git

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class MemoryCache(base: PullRequestList) extends PullRequestDecorator(base) {
  var cache: List[PullRequest] = _

  override def get: Future[List[PullRequest]] = Future {
    if (cache == null)
      cache = Await.result(base.get, Duration.Inf)
    cache
  }
}
