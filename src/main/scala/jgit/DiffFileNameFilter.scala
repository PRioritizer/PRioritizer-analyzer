package jgit

import java.util

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.gitective.core.filter.commit.{CommitDiffFilter, CommitFilter, DiffFileCountFilter}

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * Filter that tracks the file names
 */
class DiffFileNameFilter(detectRenames: Boolean = false) extends CommitDiffFilter(detectRenames) {

  private val files: mutable.MutableList[String] = mutable.MutableList()

  /**
   * @return files
   */
  def getFiles: List[String] = files.distinct.toList

  override def include(commit: RevCommit, diffs: util.Collection[DiffEntry]): Boolean = {
    val newFiles = diffs.toList.map(d => List(d.getOldPath, d.getNewPath)).flatten
    files ++= newFiles
    true
  }

  override def reset: CommitFilter = {
    files.clear()
    super.reset
  }

  override def clone: RevFilter = new DiffFileCountFilter(detectRenames)
}
