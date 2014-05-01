package merge.jgit

import org.eclipse.jgit.api._
import org.eclipse.jgit.lib.{RefUpdate, StoredConfig, ObjectId, Ref}
import scala.collection.JavaConverters._

object GitExtensions {
  implicit class RichGit(git: Git) {
    def isMergeable(branch: Ref, into: Ref): Boolean =
      isMergeable(branch.getName, into.getName)

    def isMergeable(branch: String, into: String): Boolean = {
      val prevBranch = git.getRepository.getBranch
      val beforeMerge = git.getRepository.resolve(into)

      if (prevBranch == null || beforeMerge == null)
        return false

      // Checkout new branch
      forceCheckout(into)

      try {
        // Do the actual merge here
        val result = merge(branch)
        return result.isOK
      } finally {
        // Reset merged branch
        resetHard(beforeMerge)

        // Checkout previous branch
        forceCheckout(prevBranch)
      }

      // An exception occurred
      false
    }

    def forceCheckout(name: String): CheckoutResult = {
      val cmd = git.checkout.setForce(true).setName(name)
      cmd.call
      cmd.getResult
    }

    def merge(name: String): MergeResult =
      git.merge.include(name, git.getRepository.resolve(name)).call

    def resetHard(name: String): Unit =
      git.reset.setMode(ResetCommand.ResetType.HARD).setRef(name).call

    def resetHard(obj: ObjectId): Unit =
      resetHard(obj.getName)
  }

  implicit class RichMergeStatus(result: MergeResult) {
    def isOK: Boolean = result.getMergeStatus match {
      case MergeResult.MergeStatus.ABORTED => false
      case MergeResult.MergeStatus.CHECKOUT_CONFLICT => false
      case MergeResult.MergeStatus.CONFLICTING => false
      case MergeResult.MergeStatus.FAILED => false
      case MergeResult.MergeStatus.NOT_SUPPORTED => false
      case _ => true
    }
  }

  implicit class RichStoredConfig(config: StoredConfig) {
    def addString(section: String, subsection: String, name: String, value: String): Boolean = {
      val current = config.getStringList(section, subsection, name).toList
      val contains = current contains value
      if (!contains)
        config.setStringList(section, subsection, name, (current :+ value).asJava)
      !contains
    }
    def removeString(section: String, subsection: String, name: String, value: String): Boolean = {
      val current = config.getStringList(section, subsection, name).toList
      val contains = current contains value
      if (contains)
        config.setStringList(section, subsection, name, (current diff List(value)).asJava)
      contains
    }
  }

  implicit class RichRefUpdate(update: RefUpdate) {
    def forceDelete(): RefUpdate.Result = {
      update.setForceUpdate(true)
      update.delete
    }
  }
}
