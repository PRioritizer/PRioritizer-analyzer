package jgit.merge

import git.{PullRequestProvider, MergeProvider, PullRequest}
import git.MergeResult._
import jgit.JGitExtensions._
import jgit.JGitProvider._

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.TextProgressMonitor
import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * A merge tester implementation for the JGit library.
 * @param git The git repository.
 * @param inMemoryMerge Whether to merge tester has to simulate merges on disk or in-memory.
 */
class JGitMergeProvider(val git: Git, val inMemoryMerge: Boolean) extends MergeProvider {
  val remote = "pulls"

  def fetch(provider: PullRequestProvider): Future[Unit] = {
    // Add pull requests to config
    val config = git.getRepository.getConfig
    val pulls = s"+${provider.remotePullHeads}:${pullRef("*")}"
    val heads = s"+${provider.remoteHeads}:${targetRef("*")}"
    config.setString("remote", remote, "url", provider.ssh)
    config.setStringList("remote", remote, "fetch", List(heads, pulls).asJava)

    // Fetch pull requests from remote
    val monitor = new TextProgressMonitor()
    val cmd = git.fetch.setRemote(remote).setProgressMonitor(monitor)
    Future { cmd.call }
  }

  def clean(garbageCollect: Boolean): Unit = {
    // Remove pull requests from config
    val config = git.getRepository.getConfig
    config.unsetSection("remote", remote)

    // Remove pull request refs
    val refs = git.getRepository.getRefDatabase.getRefs(pullRef("")).values.asScala ++
               git.getRepository.getRefDatabase.getRefs(targetRef("")).values.asScala
    val uRefs = refs map {
      ref => git.getRepository.updateRef(ref.getName)
    }
    uRefs.foreach(_.forceDelete())

    if (garbageCollect)
      git.gc.call
  }

  def merge(branch: String, into: String): Future[MergeResult] = {
    if (inMemoryMerge)
      Future { git.isMergeable(branch, into) }
    else
      Future { git.simulate(branch, into) }
  }

  def merge(pr: PullRequest): Future[MergeResult] = {
    if (inMemoryMerge)
      Future { git.isMergeable(pullRef(pr), into = targetRef(pr)) }
    else
      Future { git.simulate(pullRef(pr), into = targetRef(pr)) }
  }

  def merge(pr1: PullRequest, pr2: PullRequest): Future[MergeResult] = {
    if (inMemoryMerge)
      Future { git.isMergeable(pullRef(pr2), into = pullRef(pr1)) }
    else
      Future { git.simulate(pullRef(pr2), into = pullRef(pr1)) }
  }
}
