package jgit

import git.MergeResult._
import jgit.merge.MemoryMerger
import scala.collection.JavaConverters._
import org.eclipse.jgit.api.{Git, CheckoutResult, ResetCommand}
import org.eclipse.jgit.api.{MergeResult => JGitMergeResult}
import org.eclipse.jgit.lib._
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.gitective.core.{CommitFinder, CommitUtils}
import org.gitective.core.filter.commit.{DiffLineCountFilter, CommitCountFilter}

/**
 * Extensions for the JGit library
 */
object JGitExtensions {

  /**
   * Enrichment of the [[org.eclipse.jgit.api.Git]] class.
   * @param git The git object.
   */
  implicit class RichGit(git: Git) {
    /**
     * Checks if `branch` can be merged into `head`. The merge is done in-memory.
     * @param branch The branch to be merged.
     * @param head The head branch, where `branch` is merged into.
     * @return True iff the merge was successful.
     */
    def isMergeable(branch: String, head: String): MergeResult = {
      val repo = git.getRepository

      val branchId = repo resolve branch
      val headId = repo resolve head

      if (branchId == null || headId == null)
        return Error

      isMergeable(branchId, headId)
    }

    /**
     * Checks if `branch` can be merged into `head`. The merge is done in-memory.
     * @param branch The branch to be merged.
     * @param head The head branch, where `branch` is merged into.
     * @return True iff the merge was successful.
     */
    def isMergeable(branch: ObjectId, head: ObjectId): MergeResult = {
      val repo = git.getRepository
      val revWalk = new RevWalk(repo)

      val branchCommit = revWalk.lookupCommit(branch)
      val headCommit = revWalk.lookupCommit(head)

      // Check if already up-to-date
      if (revWalk.isMergedInto(branchCommit, headCommit))
        return Merged

      // Check for fast-forward
      if (revWalk.isMergedInto(headCommit, branchCommit))
        return Merged

      try {
        // Do the actual merge here (in memory)
        val merger = new MemoryMerger(repo)
        // merger.(getMergeResults|getFailingPaths|getUnmergedPaths)
        val result = merger.merge(headCommit, branchCommit)
        if (result) Merged else Conflict
      } catch {
        case _: Exception => Error
      }
    }

    /**
     * Calculates the number of commits between two commits.
     * @param objectId One end of the chain.
     * @param otherId The other end of the chain.
     * @return The distance.
     */
    def distance(objectId: ObjectId, otherId: ObjectId): Long = {
      val repo = git.getRepository
      val base: RevCommit = CommitUtils.getBase(repo, objectId, otherId)
      val count = new CommitCountFilter
      val finder = new CommitFinder(repo).setFilter(count)

      finder.findBetween(objectId, base)
      val num = count.getCount
      count.reset()

      finder.findBetween(otherId, base)
      num + count.getCount
    }

    /**
     * Calculates the number of diff lines between two commits.
     * @param objectId One end of the chain.
     * @param otherId The other end of the chain.
     * @return The number of added/deleted/changed lines.
     */
    def diffSize(objectId: ObjectId, otherId: ObjectId): Long = {
      val repo = git.getRepository
      val base: RevCommit = CommitUtils.getBase(repo, objectId, otherId)
      val count = new DiffLineCountFilter
      val finder = new CommitFinder(repo).setFilter(count)

      finder.findBetween(objectId, base)
      val num = count.getTotal
      count.reset()

      finder.findBetween(otherId, base)
      num + count.getTotal
    }
  }

  /**
   * Enrichment of the [[org.eclipse.jgit.lib.RefUpdate]] class.
   * @param update The ref update.
   */
  implicit class RichRefUpdate(update: RefUpdate) {
    /**
     * Forces the deletion of this [[org.eclipse.jgit.lib.RefUpdate]].
     * @return The result of the deletion.
     */
    def forceDelete(): RefUpdate.Result = {
      update.setForceUpdate(true)
      update.delete
    }
  }
}
