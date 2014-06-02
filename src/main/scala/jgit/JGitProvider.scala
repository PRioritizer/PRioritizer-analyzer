package jgit

import git.{PullRequest, PullRequestProvider, Provider}
import org.slf4j.LoggerFactory
import java.io.File
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git
import jgit.data.JGitDataProvider
import jgit.merge.JGitMergeProvider

/**
 * A provider implementation for the JGit library.
 * @param repoDirectory The path to the directory of the git repository. It can either be a working directory or a bare git directory.
 */
class JGitProvider(repoDirectory: String) extends Provider {
  val dotGit = ".git"
  val gitDir = if (repoDirectory.endsWith(dotGit)) repoDirectory else repoDirectory + File.separator + dotGit
  val repository = new FileRepositoryBuilder().setGitDir(new File(gitDir))
    .readEnvironment // scan environment GIT_* variables
    .findGitDir // scan the file system tree
    .build

  // Create git client
  val git: Git = new Git(repository)

  override def pullRequests: Option[PullRequestProvider] = None
  override def merger: Option[JGitMergeProvider] =
    Some(new JGitMergeProvider(git))
  override def data: Option[JGitDataProvider] =
    Some(new JGitDataProvider(repository))

  override def dispose(): Unit = {
    for (m <- merger)
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