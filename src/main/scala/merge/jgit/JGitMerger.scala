package merge.jgit

import java.io.File

import merge.jgit.GitExtensions._
import merge.MergeTester
import git.PullRequest

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.lib.{Ref, TextProgressMonitor}
import scala.collection.JavaConverters._

class JGitMerger(workingDirectory: String, remote: String = "origin") extends MergeTester {
  var hasPullRefs: Boolean = _
  var pullRefs: Traversable[Ref] = _

  val dotGit = ".git"
  val gitDir = if (workingDirectory.endsWith(dotGit)) workingDirectory else workingDirectory + File.separator + dotGit
  val repository = new FileRepositoryBuilder().setGitDir(new File(gitDir))
    .readEnvironment // scan environment GIT_* variables
    .findGitDir // scan up the file system tree
    .build

  // Create git client
  val git: Git = new Git(repository)

  def fetch(): Unit = {
    // Add pull requests to config, remember previous setting
    hasPullRefs = !configurePullRefs()

    // Fetch pull requests from remote
    val monitor = new TextProgressMonitor()
    git.fetch.setRemote(remote).setProgressMonitor(monitor).call
  }

  def clean(): Unit = {
    // Check if repo already had pull requests
    if (hasPullRefs)
      return

    // Remove pull requests from config
    configurePullRefs(remove = true)

    // Remove pull request refs
    val refs = git.getRepository.getRefDatabase.getRefs(pullRef("")).values.asScala map {
      ref => git.getRepository.updateRef(ref.getName)
    }
    refs.foreach(_.forceDelete())

    git.gc.call
  }

  def merge(branch: String, into: String): Boolean =
    git.isMergeable(branch, into)

  def merge(pr: PullRequest): Boolean =
    git.isMergeable(pullRef(pr), into = pr.base)

  def merge(pr1: PullRequest, pr2: PullRequest): Boolean =
    git.isMergeable(pullRef(pr1), into = pullRef(pr2))

  private def pullRef(pr: String): String = s"refs/pull/$remote/$pr"
  private def pullRef(pr: PullRequest): String = pullRef(pr.number.toString)

  /**
   * Adds or removes the pull request fetch configuration.
   * If the configuration is already present when adding or if the configuration
   * is absent when deleting the return value is false.
   * More info about fetching pull requests:
   *   https://help.github.com/articles/checking-out-pull-requests-locally
   * @param remove Add or remove the configuration (default: add)
   * @return True iff the action succeeds, otherwise false
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
