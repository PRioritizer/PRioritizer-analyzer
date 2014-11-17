package jgit

import git.{PullRequest, PullRequestDecorator, PullRequestList}
import jgit.JGitExtensions._
import jgit.JGitProvider._
import org.gitective.core.CommitUtils

/**
 * An info getter implementation for the JGit library.
 * @param provider The JGit provider.
 */
class JGitDecorator(base: PullRequestList, val provider: JGitProvider) extends PullRequestDecorator(base) {
  lazy val repo = provider.repository

  override def decorate(pullRequest: PullRequest): PullRequest = {
    if (!hasStats(pullRequest))
      enrichStats(pullRequest)

    pullRequest
  }

  private def hasStats(pullRequest: PullRequest): Boolean =
    pullRequest.linesAdded.isDefined &&
    pullRequest.linesDeleted.isDefined &&
    pullRequest.filesChanged.isDefined &&
    pullRequest.commits.isDefined &&
    pullRequest.hasTestCode.isDefined

  private def enrichStats(pullRequest: PullRequest): PullRequest = {
    val head = repo resolve pullRef(pullRequest)
    val target = repo resolve targetRef(pullRequest)
    val base = if (head != null && target != null) CommitUtils.getBase(repo, head, target) else null

    // Check if commits are resolved
    if (head != null && base != null) {
      val Stats(added, edited, deleted, files, numCommits) = repo.stats(head, base)
      val hasTestCode = files.exists(f => f.toLowerCase.contains("test") || f.toLowerCase.contains("spec"))
      pullRequest.linesAdded = Some(added + edited)
      pullRequest.linesDeleted = Some(deleted + edited)
      pullRequest.filesChanged = Some(files.length)
      pullRequest.commits = Some(numCommits)
      pullRequest.hasTestCode = Some(hasTestCode)
    }

    pullRequest
  }
}
