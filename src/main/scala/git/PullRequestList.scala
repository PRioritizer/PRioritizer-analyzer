package git

import scala.concurrent.Future

trait PullRequestList {
  def get: Future[List[PullRequest]]
}
