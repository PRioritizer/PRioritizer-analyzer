package cache

import git.PullRequest

case class CachedPullRequest(sha: String, isMergeable: Boolean, linesAdded: Long, linesDeleted: Long, filesChanged: Long, commits: Long) {
  def fill(pullRequest: PullRequest): PullRequest = {
    pullRequest.isMergeable = Some(isMergeable)
    pullRequest.linesAdded = Some(linesAdded)
    pullRequest.linesDeleted = Some(linesDeleted)
    pullRequest.filesChanged = Some(filesChanged)
    pullRequest.commits = Some(commits)
    pullRequest
  }

  def represents(pullRequest: PullRequest): Boolean = {
    this == CachedPullRequest(pullRequest)
  }
}

object CachedPullRequest extends ((String, Boolean, Long, Long, Long, Long) => CachedPullRequest) {
  def apply(pr: PullRequest): CachedPullRequest = {
    CachedPullRequest(pr.sha,
      pr.isMergeable.getOrElse(false),
      pr.linesAdded.getOrElse(0),
      pr.linesDeleted.getOrElse(0),
      pr.filesChanged.getOrElse(0),
      pr.commits.getOrElse(0))
  }
}
