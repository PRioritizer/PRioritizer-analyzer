package git.decorate

import git.{PullRequestList, PullRequest}

import scala.concurrent.Future

/**
 * Offers the functionality to get data about the repository.
 */
abstract class PullRequestDecorator(val base: PullRequestList) extends PullRequestList
