package jgit.data

import git.{RichPullRequest, PullRequest, DataProvider}
import org.eclipse.jgit.lib.Repository
import jgit.JGitProvider._
import jgit.JGitExtensions._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.gitective.core.CommitUtils

/**
 * An info getter implementation for the JGit library.
 * @param repo The git repository.
 */
class JGitDataProvider(val repo: Repository) extends DataProvider {
  override def enrich(pullRequest: PullRequest): Future[RichPullRequest] = {
    Future {
      val head = repo resolve pullRef(pullRequest)
      val target = repo resolve targetRef(pullRequest)
      //val base = repo resolve pullRequest.base
      val base = if (head != null && target != null) CommitUtils.getBase(repo, head, target) else null

      // Check if commits are resolved
      if (head == null || base == null)
        RichPullRequest(pullRequest)
      else {
        val lineCount = repo.diffSize(head, base)
        RichPullRequest(pullRequest, lineCount)
      }
    }
  }
}
