package merge.jgit

import org.eclipse.jgit.merge.RecursiveMerger
import org.eclipse.jgit.lib.Repository

class MemoryMerger(local: Repository) extends RecursiveMerger(local, true)
