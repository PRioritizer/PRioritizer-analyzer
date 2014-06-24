package git

import scala.concurrent.Future

trait PullRequestList {
  def get: List[Future[PullRequest]]
}
