package git

import scala.concurrent.Future

trait TotalList {
  def get: Future[List[PullRequest]]
}
