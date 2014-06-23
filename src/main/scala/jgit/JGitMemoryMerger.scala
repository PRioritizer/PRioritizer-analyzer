package jgit

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.merge.RecursiveMerger

/**
 * A merger object which performs merges in-memory instead of on the disk. This results in faster merges.
 * @param local The local repository.
 */
class JGitMemoryMerger(local: Repository) extends RecursiveMerger(local, true)
//  override def merge(tips: AnyObjectId*): Boolean = {
//    // Do stuff here
//    super.merge(tips: _*)
//  }
