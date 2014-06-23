package jgit.decorate

import git.{PullRequestList, PullRequest}
import git.decorate.PullRequestDecorator
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
class JGitDecorator(base: PullRequestList, val provider: JGitProvider) extends PullRequestDecorator(base) {
  val repo = provider.repository

  override def get: Future[List[PullRequest]] = {
    for(list <- base.get) yield list.map(decorate)
  }

  def decorate(pullRequest: PullRequest): PullRequest = {
    if (!hasStats(pullRequest))
      enrichStats(pullRequest)
    else
      pullRequest
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
