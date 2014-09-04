package git

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Total(list: List[PullRequest]) extends TotalList {
  override def get: Future[List[PullRequest]] = Future(list)
}
