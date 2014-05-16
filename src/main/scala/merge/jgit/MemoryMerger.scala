package merge.jgit

import org.eclipse.jgit.merge.RecursiveMerger
import org.eclipse.jgit.lib.Repository

/**
 * A merger object which performs merges in-memory instead of on the disk. This results in faster merges.
 * @param local The local repository.
 */
class MemoryMerger(local: Repository) extends RecursiveMerger(local, true)
