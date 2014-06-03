package jgit.merge

import git.{PullRequestProvider, MergeProvider, PullRequest}
import git.MergeResult._
import jgit.JGitExtensions._
import jgit.JGitProvider._

import org.eclipse.jgit.lib.{ConfigConstants, TextProgressMonitor}
import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import jgit.JGitProvider

/**
 * A merge tester implementation for the JGit library.
 * @param provider The JGit provider.
 */
class JGitMergeProvider(val provider: JGitProvider) extends MergeProvider {
  val remote = "pulls"
  val git = provider.git
  val repo = provider.repository
  val merger = new MemoryMerger(repo)

  override def fetch(provider: PullRequestProvider): Future[Unit] = {
    // Add pull requests to config
    val config = repo.getConfig
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

  override def clean(): Unit = clean(garbageCollect = false)

  def clean(garbageCollect: Boolean): Unit = {
    // Remove pull requests from config
    val config = repo.getConfig
    config.unsetSection(ConfigConstants.CONFIG_REMOTE_SECTION, remote)

    // Remove pull request refs
    val refs = repo.getRefDatabase.getRefs(pullRef("")).values.asScala ++
               repo.getRefDatabase.getRefs(targetRef("")).values.asScala
    val uRefs = refs map {
      ref => repo.updateRef(ref.getName)
    }
    uRefs.foreach(_.forceDelete())

    if (garbageCollect)
      git.gc.call
  }

  override def merge(branch: String, into: String): Future[MergeResult] =
    Future { repo.isMergeable(branch, into) }

  override def merge(pr: PullRequest): Future[MergeResult] =
    Future { repo.isMergeable(pullRef(pr), targetRef(pr)) }

  override def merge(pr1: PullRequest, pr2: PullRequest): Future[MergeResult] =
    Future { repo.isMergeable(pullRef(pr2), pullRef(pr1)) }
}
