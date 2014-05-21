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
     * Checks if `branch` can be merged into `into`. The merge is done on-disk.
     * @param branch The branch to be merged.
     * @param into The base branch, where `branch` is merged into.
     * @return True iff the merge was successful.
     */
    def simulate(branch: String, into: String): MergeResult = {
      val prevBranch = git.getRepository.getBranch
      val beforeMerge = git.getRepository.resolve(into)
      val alreadyOnBranch = git.alreadyOnBranch(into)

      if (prevBranch == null || beforeMerge == null)
        return Error

      // Checkout new branch
      if (!alreadyOnBranch)
        forceCheckout(into)

      try {
        // Do the actual merge here
        val result = merge(branch)
        if (result.isOK) Merged else Conflict
      } catch {
        case _: Exception => Error
      } finally {
        // Reset merged branch
        resetHard(beforeMerge)

        // Checkout previous branch
        if (!alreadyOnBranch)
          forceCheckout(prevBranch)
      }
    }

    /**
     * Forces a checkout of the given branch.
     * @param name The branch name.
     * @return Teh checkout result.
     */
    def forceCheckout(name: String): CheckoutResult = {
      val cmd = git.checkout.setForce(true).setName(name)
      cmd.call
      cmd.getResult
    }

    /**
     * Merges the given branch into the currently checkout branch.
     * @param name The branch to be merged.
     * @return The merge result.
     */
    def merge(name: String): JGitMergeResult =
      git.merge
        .include(name, git.getRepository.resolve(name))
        .call

    /**
     * Resets the current branch to the given commit.
     * @param name The commit name.
     */
    def resetHard(name: String): Unit =
      git.reset
        .setMode(ResetCommand.ResetType.HARD)
        .setRef(name)
        .call

    /**
     * Resets the current branch to the given commit.
     * @param obj The commit id.
     */
    def resetHard(obj: ObjectId): Unit =
      resetHard(obj.getName)

    /**
     * @param name The branch name.
     * @return True iff the current HEAD is the same as the given branch.
     */
    def alreadyOnBranch(name: String): Boolean =
    alreadyOnBranch(git.getRepository.resolve(name))

    /**
     * @param obj The commit id.
     * @return True iff the current HEAD is the same as the given commit.
     */
    def alreadyOnBranch(obj: ObjectId): Boolean =
      git.getRepository
        .resolve(Constants.HEAD) equals obj

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
   * Enrichment of the [[org.eclipse.jgit.api.MergeResult]] class.
   * @param result The merge result.
   */
  implicit class RichMergeStatus(result: JGitMergeResult) {
    /**
     * @return True iff the merge results was positive.
     */
    def isOK: Boolean = result.getMergeStatus match {
      case JGitMergeResult.MergeStatus.ABORTED => false
      case JGitMergeResult.MergeStatus.CHECKOUT_CONFLICT => false
      case JGitMergeResult.MergeStatus.CONFLICTING => false
      case JGitMergeResult.MergeStatus.FAILED => false
      case JGitMergeResult.MergeStatus.NOT_SUPPORTED => false
      case _ => true
    }
  }

  /**
   * Enrichment of the [[org.eclipse.jgit.lib.StoredConfig]] class.
   * @param config The config object.
   */
  implicit class RichStoredConfig(config: StoredConfig) {
    /**
     * Adds a string value to the given config section.
     * @param section The config section.
     * @param subsection The config subsection.
     * @param name The config name.
     * @param value The config value.
     * @return True iff the value was added (i.e. was not already present).
     */
    def addString(section: String, subsection: String, name: String, value: String): Boolean = {
      val current = config.getStringList(section, subsection, name).toList
      val contains = current contains value
      if (!contains)
        config.setStringList(section, subsection, name, (current :+ value).asJava)
      !contains
    }

    /**
     * Removes a string value from the given config section.
     * @param section The config section.
     * @param subsection The config subsection.
     * @param name The config name.
     * @param value The config value.
     * @return True iff the value was removed (i.e. was present).
     */
    def removeString(section: String, subsection: String, name: String, value: String): Boolean = {
      val current = config.getStringList(section, subsection, name).toList
      val contains = current contains value
      if (contains)
        config.setStringList(section, subsection, name, (current diff List(value)).asJava)
      contains
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
