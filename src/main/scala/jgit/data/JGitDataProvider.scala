package jgit.data

import git.{PullRequest, DataProvider}
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
  override def enrich(pullRequest: PullRequest): Future[PullRequest] = {
    Future {
      val head = repo resolve pullRef(pullRequest)
      val target = repo resolve targetRef(pullRequest)
      val base = if (head != null && target != null) CommitUtils.getBase(repo, head, target) else null

      // Check if commits are resolved
      if (head != null && base != null)
        pullRequest.lineCount = repo.diffSize(head, base)

      pullRequest
    }
  }
}
