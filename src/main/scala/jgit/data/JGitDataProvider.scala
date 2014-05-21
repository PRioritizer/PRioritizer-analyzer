package jgit.data

import git.{RichPullRequest, PullRequest, DataProvider}
import org.eclipse.jgit.api.Git
import jgit.JGitProvider._
import jgit.JGitExtensions._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * An info getter implementation for the JGit library.
 * @param git The git repository.
 */
class JGitDataProvider(val git: Git) extends DataProvider {
  override def enrich(pullRequest: PullRequest): Future[RichPullRequest] = {
    val repo = git.getRepository
    val head = repo resolve pullRef(pullRequest)
    val base = repo resolve pullRequest.base

    // Check if commit are resolved
    if (head == null || base == null)
      return Future { RichPullRequest(pullRequest) }

    Future {
      val lineCount = git.diffSize(head, base)
      RichPullRequest(pullRequest, lineCount)
    }
  }
}
