package jgit.merge

import git.{PullRequestProvider, MergeProvider, PullRequest}
import jgit.JGitExtensions._
import jgit.JGitProvider._

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.TextProgressMonitor
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory

/**
 * A merge tester implementation for the JGit library.
 * @param git The git repository.
 * @param inMemoryMerge Whether to merge tester has to simulate merges on disk or in-memory.
 */
class JGitMergeProvider(val git: Git, val inMemoryMerge: Boolean) extends MergeProvider {
  val logger = LoggerFactory.getLogger(this.getClass)
  val remote = "pulls"

  def fetch(provider: PullRequestProvider): Unit = {
    // Add pull requests to config
    val config = git.getRepository.getConfig
    val pulls = s"+${provider.remotePullHeads}:${pullRef("*")}"
    config.setString("remote", remote, "url", provider.ssh)
    config.setString("remote", remote, "fetch", pulls)

    // Fetch pull requests from remote
    val monitor = new TextProgressMonitor()
    git.fetch.setRemote(remote).setProgressMonitor(monitor).call
  }

  def clean(garbageCollect: Boolean): Unit = {
    // Remove pull requests from config
    val config = git.getRepository.getConfig
    config.unsetSection("remote", remote)

    // Remove pull request refs
    val refs = git.getRepository.getRefDatabase.getRefs(pullRef("")).values.asScala map {
      ref => git.getRepository.updateRef(ref.getName)
    }
    refs.foreach(_.forceDelete())

    if (garbageCollect)
      git.gc.call
  }

  def merge(branch: String, into: String): Boolean = {
    logger trace s"Merge $branch into $into"
    if (inMemoryMerge)
      git.isMergeable(branch, into)
    else
      git.simulate(branch, into)
  }

  def merge(pr: PullRequest): Boolean = {
    logger trace s"Merge $pr"
    if (inMemoryMerge)
      git.isMergeable(pullRef(pr), into = pr.target)
    else
      git.simulate(pullRef(pr), into = pr.target)
  }

  def merge(pr1: PullRequest, pr2: PullRequest): Boolean = {
    logger trace s"Merge #${pr1.number} '${pr1.branch}' into #${pr2.number} '${pr2.branch}'"
    if (inMemoryMerge)
      git.isMergeable(pullRef(pr2), into = pullRef(pr1))
    else
      git.simulate(pullRef(pr2), into = pullRef(pr1))
  }
}
