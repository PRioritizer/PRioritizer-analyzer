package ghtorrent.enrich

import git.{PullRequest, EnrichmentProvider}
import org.eclipse.jgit.lib.Repository
import jgit.JGitProvider._
import jgit.JGitExtensions._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.gitective.core.CommitUtils
import ghtorrent.GHTorrentProvider

/**
 * An info getter implementation for the GHTorrent database.
 * @param provider The GHTorrent provider.
 */
class GHTorrentEnrichmentProvider(val provider: GHTorrentProvider) extends EnrichmentProvider {
  override def enrich(pullRequest: PullRequest): Future[PullRequest] = {
    Future {

      pullRequest
    }
  }
}
