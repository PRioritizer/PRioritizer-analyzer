package jgit.decorate

import git.{PullRequest, PullRequestDecorator}
import jgit.JGitProvider._
import jgit.JGitExtensions._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.gitective.core.CommitUtils
import jgit.JGitProvider

/**
 * An info getter implementation for the JGit library.
 * @param provider The JGit provider.
 */
class JGitDecorator(val provider: JGitProvider) extends PullRequestDecorator {
  val repo = provider.repository

  override def decorate(pullRequest: PullRequest): Future[PullRequest] = {
    Future {
      if (!hasStats(pullRequest))
        enrichStats(pullRequest)
      else
        pullRequest
    }
  }

  private def hasStats(pullRequest: PullRequest): Boolean = pullRequest.commits > 0

  private def enrichStats(pullRequest: PullRequest): PullRequest = {
    val head = repo resolve pullRef(pullRequest)
    val target = repo resolve targetRef(pullRequest)
    val base = if (head != null && target != null) CommitUtils.getBase(repo, head, target) else null

    // Check if commits are resolved
    if (head != null && base != null) {
      val Stats(added, edited, deleted, numFiles, numCommits) = repo.stats(head, base)
      pullRequest.linesAdded = added + edited
      pullRequest.linesDeleted = deleted + edited
      pullRequest.filesChanged = numFiles
      pullRequest.commits = numCommits
    }

    pullRequest
  }
}
