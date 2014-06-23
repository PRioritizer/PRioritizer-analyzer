package jgit

import git.decorate.PullRequestDecorator
import git._
import java.io.File
import jgit.merge.JGitMergeProvider
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git
import jgit.decorate.JGitDecorator

/**
 * A provider implementation for the JGit library.
 * @param repoDirectory The path to the directory of the git repository. It can either be a working directory or a bare git directory.
 */
class JGitProvider(repoDirectory: String, cleanUp: Boolean = true) extends Provider {
  val dotGit = ".git"
  val gitDir = if (repoDirectory.endsWith(dotGit)) repoDirectory else repoDirectory + File.separator + dotGit
  val repository = new FileRepositoryBuilder().setGitDir(new File(gitDir))
    .readEnvironment // scan environment GIT_* variables
    .findGitDir // scan the file system tree
    .build

  // Create git client
  val git: Git = new Git(repository)

  override def repositoryProvider: Option[RepositoryProvider] = None
  override def pullRequestProvider: Option[PullRequestProvider] = None
  override def mergeProvider: Option[JGitMergeProvider] =
    Some(new JGitMergeProvider(this))
  override def getDecorator(list: PullRequestList): Option[PullRequestList] =
    Some(new JGitDecorator(list, this))

  override def dispose(): Unit = {
    for (m <- mergeProvider)
      if (cleanUp)
        m.clean()

    git.close()
    repository.close()
  }
}

object JGitProvider {
  /**
   * Returns the ref string for the head of given pull request.
   * E.g. `"refs/pulls/``*``"`.
   * @param pr The name or number of the pull request or a wildcard (`*`).
   * @return The ref path to pull request.
   */
  def pullRef(pr: String): String = s"refs/pulls/$pr"

  /**
   * Returns the ref string for the head of given pull request.
   * E.g. `"refs/pulls/123"`.
   * @param pr The pull request.
   * @return The ref path to pull request.
   */
  def pullRef(pr: PullRequest): String = pullRef(pr.number.toString)

  /**
   * Returns the ref string for the merge target of the given pull request.
   * E.g. `"refs/pulls/targets/``*``"`.
   * @param pr The name or number of the pull request or a wildcard (`*`).
   * @return The ref path to pull request.
   */
  def targetRef(pr: String): String = s"refs/pulls/targets/$pr"

  /**
   * Returns the ref string for the merge target of the given pull request.
   * E.g. `"refs/pulls/targets/master"`.
   * @param pr The pull request.
   * @return The ref path to pull request.
   */
  def targetRef(pr: PullRequest): String = targetRef(pr.target)
}
