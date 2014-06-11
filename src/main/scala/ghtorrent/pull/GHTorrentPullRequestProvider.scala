package ghtorrent.pull

import git.PullRequest
import git.PullRequestProvider
import scala.concurrent.Future
import ghtorrent.GHTorrentProvider

class GHTorrentPullRequestProvider(val provider: GHTorrentProvider) extends PullRequestProvider {
  val host = "github.com"
  val source = "github"
  val owner = provider.owner
  val repository = provider.repository

  override val https: String = s"https://$host/$owner/$repository.git"

  override val ssh: String = s"git@$host:$owner/$repository.git"

  override val remotePullHeads: String = "refs/pull/*/head"

  override val remoteHeads: String = "refs/heads/*"

  override def get: Future[List[PullRequest]] = ???
}
