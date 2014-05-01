package merge

import git.PullRequest

trait MergeTester {
  def merge(branch: String): GitMergeOperation =
    new GitMergeOperation(this, branch)

  def fetch(): Unit

  def clean(): Unit

  def merge(branch: String, into: String): Boolean

  def merge(pull: PullRequest): Boolean

  def merge(pullLeft: PullRequest, pullRight: PullRequest): Boolean
}

class GitMergeOperation(merger: MergeTester, branchToMerge: String) {
  def into(branch: String): Boolean =
    merger.merge(branchToMerge, branch)
}
