package jgit.merge

import git.{MergeTester, PullRequest}
import jgit.JGitExtensions._

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.TextProgressMonitor
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory

/**
 * A merge tester implementation for the JGit library.
 * @param git The git repository.
 * @param remote The name of the GitHub remote.
 * @param inMemoryMerge Whether to merge tester has to simulate merges on disk or in-memory.
 */
class JGitMergerTester(val git: Git, val remote: String, val inMemoryMerge: Boolean) extends MergeTester {
  val logger = LoggerFactory.getLogger(this.getClass)
  var hasPullRefs: Boolean = _

  def fetch(): Unit = {
    // Add pull requests to config, remember previous setting
    hasPullRefs = !configurePullRefs()

    // Fetch pull requests from remote
    val monitor = new TextProgressMonitor()
    git.fetch.setRemote(remote).setProgressMonitor(monitor).call
  }

  def clean(force: Boolean, garbageCollect: Boolean): Unit = {
    // Check if repo already had pull requests
    if (!force && hasPullRefs)
      return

    // Remove pull requests from config
    configurePullRefs(remove = true)

    // Remove pull request refs
    val refs = git.getRepository.getRefDatabase.getRefs(pullRef("")).values.asScala map {
      ref => git.getRepository.updateRef(ref.getName)
    }
    refs.foreach(_.forceDelete())

    if (garbageCollect)
      git.gc.call
  }

  def merge(branch: String, into: String): Boolean = {
    logger info s"Merge $branch into $into"
    if (inMemoryMerge)
      git.isMergeable(branch, into)
    else
      git.simulate(branch, into)
  }

  def merge(pr: PullRequest): Boolean = {
    logger info s"Merge $pr"
    if (inMemoryMerge)
      git.isMergeable(pullRef(pr), into = pr.base)
    else
      git.simulate(pullRef(pr), into = pr.base)
  }

  def merge(pr1: PullRequest, pr2: PullRequest): Boolean = {
    logger info s"Merge #${pr1.number} '${pr1.branch}' into #${pr2.number} '${pr2.branch}'"
    if (inMemoryMerge)
      git.isMergeable(pullRef(pr2), into = pullRef(pr1))
    else
      git.simulate(pullRef(pr2), into = pullRef(pr1))
  }

  /**
   * Returns the ref string for the given pull request. The ref consists of `pr`
   * prefixed with the remote ref path.
   * E.g. `"refs/pull/origin/``*``"`.
   * @param pr The name or number of the pull request or a wildcard (`*`).
   * @return The ref path to pull request.
   */
  private def pullRef(pr: String): String = s"refs/pull/$remote/$pr"

  /**
   * Returns the ref string for the given pull request. The ref consists of the
   * number of the pull request prefixed with the remote ref path.
   * E.g. `"refs/pull/origin/123"`.
   * @param pr The pull request.
   * @return The ref path to pull request.
   */
  private def pullRef(pr: PullRequest): String = pullRef(pr.number.toString)

  /**
   * Adds or removes the pull request fetch configuration.
   * If the configuration is already present when adding or if the configuration
   * is absent when deleting the return value is false.
   * More info about fetching pull requests:
   *   https://help.github.com/articles/checking-out-pull-requests-locally
   * @param remove Add or remove the configuration (default: add).
   * @return True iff the action succeeds, otherwise false.
   */
  private def configurePullRefs(remove: Boolean = false): Boolean = {
    val config = git.getRepository.getConfig
    val pulls = s"+refs/pull/*/head:${pullRef("*")}"

    // Change setting
    val res = if (remove)
      config.removeString("remote", remote, "fetch", pulls)
    else
      config.addString("remote", remote, "fetch", pulls)
    config.save()

    // Return true iff succeeded
    res
  }
}
