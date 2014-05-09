package merge

import git.PullRequest

trait MergeTester {
  def merge(branch: String): GitMergeOperation =
    new GitMergeOperation(this, branch)

  def fetch(): Unit

  def clean(force: Boolean = false): Unit

  def merge(branch: String, into: String): Boolean

  def merge(pull: PullRequest): Boolean

  def merge(pullLeft: PullRequest, pullRight: PullRequest): Boolean

  def gitHubInfo: Option[(String, String)]
}

class GitMergeOperation(merger: MergeTester, branchToMerge: String) {
  def into(branch: String): Boolean =
    merger.merge(branchToMerge, branch)
}
