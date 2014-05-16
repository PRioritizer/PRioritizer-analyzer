package jgit

import git.Provider
import org.slf4j.LoggerFactory
import java.io.File
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git
import jgit.info.JGitInfoGetter
import jgit.merge.JGitMergerTester

/**
 * A provider implementation for the JGit library.
 * @param workingDirectory The path to the working directory of the git repository.
 * @param remote The name of the GitHub remote.
 * @param inMemoryMerge Whether to merge tester has to simulate merges on disk or in-memory.
 */
class JGitProvider(workingDirectory: String, val remote: String = "origin", inMemoryMerge: Boolean = true) extends Provider {
  val logger = LoggerFactory.getLogger(this.getClass)
  val dotGit = ".git"
  val gitDir = if (workingDirectory.endsWith(dotGit)) workingDirectory else workingDirectory + File.separator + dotGit
  val repository = new FileRepositoryBuilder().setGitDir(new File(gitDir))
    .readEnvironment // scan environment GIT_* variables
    .findGitDir // scan the file system tree
    .build

  // Create git client
  val git: Git = new Git(repository)

  override def info: JGitInfoGetter = new JGitInfoGetter(git, remote)
  override def merger: JGitMergerTester = new JGitMergerTester(git, remote, inMemoryMerge)
}
