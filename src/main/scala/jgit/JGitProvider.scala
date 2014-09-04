package jgit

import java.io.{File, FileNotFoundException}

import git._
import jgit.JGitExtensions._
import jgit.JGitProvider._
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{ConfigConstants, TextProgressMonitor}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * A provider implementation for the JGit library.
 */
class JGitProvider extends Provider {
  val remote = "pulls"
  val dotGit = ".git"
  val repoDirectory = JGitSettings.directory
  val gitDir = if (repoDirectory != null && repoDirectory.endsWith(dotGit)) repoDirectory else repoDirectory + File.separator + dotGit

  // Check if directory exists
  if (!JGitSettings.validate)
    throw new IllegalArgumentException("Invalid jgit configuration.")

  // Check if directory exists
  if (gitDir == null || gitDir == "" || !new File(gitDir).isDirectory)
    throw new FileNotFoundException(s"Repository directory $repoDirectory not found.")

  lazy val repository = new FileRepositoryBuilder().setGitDir(new File(gitDir))
    .readEnvironment // scan environment GIT_* variables
    .findGitDir // scan the file system tree
    .build

  // Create git client
  lazy val git: Git = new Git(repository)

  override def getDecorator(list: PullRequestList): PullRequestList = new JGitDecorator(new JGitMerger(list, this), this)
  override def getPairwiseDecorator(list: PairwiseList): PairwiseList = new JGitPairwiseMerger(list, this)

  override def init(provider: Provider): Future[Unit] = {
    // Force lazy value evaluation
    git
    fetch(provider.pullRequestProvider.orNull)
  }

  override def dispose(): Unit = {
    if (JGitSettings.clean)
      clean()

    git.close()
    repository.close()
  }

  def fetch(provider: PullRequestProvider): Future[Unit] = {
    // Add pull requests to config
    val config = repository.getConfig
    val pulls = s"+${provider.remotePullHeads}:${pullRef("*")}"
    val heads = s"+${provider.remoteHeads}:${targetRef("*")}"
    config.setString(ConfigConstants.CONFIG_REMOTE_SECTION, remote,
      ConfigConstants.CONFIG_KEY_URL, provider.ssh)
    config.setStringList(ConfigConstants.CONFIG_REMOTE_SECTION, remote,
      ConfigConstants.CONFIG_FETCH_SECTION, List(heads, pulls).asJava)

    // Fetch pull requests from remote
    val monitor = new TextProgressMonitor()
    val cmd = git.fetch.setRemote(remote).setProgressMonitor(monitor)
    Future { cmd.call }
  }

  def clean(garbageCollect: Boolean = false): Unit = {
    // Remove pull requests from config
    val config = repository.getConfig
    config.unsetSection(ConfigConstants.CONFIG_REMOTE_SECTION, remote)

    // Remove pull request refs
    val refs = repository.getRefDatabase.getRefs(pullRef("")).values.asScala ++
      repository.getRefDatabase.getRefs(targetRef("")).values.asScala
    val uRefs = refs map {
      ref => repository.updateRef(ref.getName)
    }
    uRefs.foreach(_.forceDelete())

    if (garbageCollect)
      git.gc.call
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
