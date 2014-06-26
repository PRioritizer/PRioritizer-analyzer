package jgit

import git.{PullRequest, PullRequestDecorator, PullRequestList}
import jgit.JGitExtensions._
import jgit.JGitProvider._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * An info getter implementation for the JGit library.
 * @param provider The JGit provider.
 */
class JGitMerger(base: PullRequestList, val provider: JGitProvider) extends PullRequestDecorator(base) {
  lazy val repo = provider.repository

  override def decorate(pullRequest: PullRequest): PullRequest = {
    if (!hasMergeInfo(pullRequest)) {
      val result = repo.isMergeable(pullRef(pullRequest), targetRef(pullRequest))
      pullRequest.isMergeable = Some(MergeResult.isSuccess(result))
    }

    pullRequest
  }

  def hasMergeInfo(pullRequest: PullRequest): Boolean = pullRequest.isMergeable.isDefined
}
