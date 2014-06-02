package git

import git.MergeResult.MergeResult
import scala.concurrent.Future

/**
 * Offers the functionality to test if a specific merge between branches or pull requests is possible or not.
 */
trait MergeProvider {

  /**
   * Creates a builder for merging two branches.
   * @param branch The branch name that is going to be merged.
   * @return A [[git.MergeBuilder]] where a branch can be merged into with [[git.MergeBuilder#into(String)]].
   */
  def merge(branch: String): MergeBuilder =
    new MergeBuilder(this, branch)

  /**
   * Fetches the pull requests from the remote to the local repository.
   */
  def fetch(provider: PullRequestProvider): Future[Unit]

  /**
   * Cleans the local repository. There are two cleaning tasks: removing the local pull requests and garbage collecting
   * the repository.
   */
  def clean(): Unit

  /**
   * Merges two branches.
   * @param branch The branch to be merged.
   * @param into The base branch, where `branch` is merged into.
   * @return True iff the merge was successful.
   */
  def merge(branch: String, into: String): Future[MergeResult]

  /**
   * Merges a pull request into its target branch.
   * @param pull The pull request.
   * @return True iff the merge was successful.
   */
  def merge(pull: PullRequest): Future[MergeResult]

  /**
   * Merges two pull requests.
   * @param pullLeft The pull request to be merged.
   * @param pullRight The pull request, where `pullLeft` is merged into.
   * @return True iff the merge was successful.
   */
  def merge(pullLeft: PullRequest, pullRight: PullRequest): Future[MergeResult]
}

/**
 * A builder for constructing a merge action.
 * @param merger The merger that performs the actual merge.
 * @param branchToMerge The branch that is going to be merged.
 */
class MergeBuilder(merger: MergeProvider, branchToMerge: String) {
  /**
   * Merge the previous branch into `branch`.
   * @param branch The base branch.
   * @return True iff the merge was successful.
   */
  def into(branch: String): Future[MergeResult] =
    merger.merge(branchToMerge, branch)
}

/**
 * An enum type for merge results.
 */
object MergeResult extends Enumeration {
  type MergeResult = Value
  val Merged, Conflict, Error = Value
}
