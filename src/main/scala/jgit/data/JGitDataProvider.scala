package jgit.data

import git.{RichPullRequest, PullRequest, DataProvider}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import jgit.JGitProvider._
import org.gitective.core.filter.commit.DiffLineCountFilter
import org.gitective.core.{CommitUtils, CommitFinder}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * An info getter implementation for the JGit library.
 * @param git The git repository.
 */
class JGitDataProvider(val git: Git) extends DataProvider {
  override def enrich(pullRequest: PullRequest): Future[RichPullRequest] = {
    val repo = git.getRepository
    val head = pullRef(pullRequest)
    val target = targetRef(pullRequest)

    Future {
      val lineCount: DiffLineCountFilter = new DiffLineCountFilter
      val base: RevCommit = CommitUtils.getBase(repo, target, head)
      new CommitFinder(repo).setFilter(lineCount).findBetween(head, base)

      RichPullRequest(pullRequest, lineCount.getTotal)
    }
  }
}
