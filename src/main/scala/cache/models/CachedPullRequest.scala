package cache.models

import git.PullRequest

case class CachedPullRequest(sha: String, linesAdded: Long, linesDeleted: Long, filesChanged: Long, commits: Long, hasTestCode: Boolean) {
  def fill(pullRequest: PullRequest): PullRequest = {
    pullRequest.linesAdded = Some(linesAdded)
    pullRequest.linesDeleted = Some(linesDeleted)
    pullRequest.filesChanged = Some(filesChanged)
    pullRequest.commits = Some(commits)
    pullRequest.hasTestCode = Some(hasTestCode)
    pullRequest
  }

  def represents(pullRequest: PullRequest): Boolean = {
    this == CachedPullRequest(pullRequest)
  }
}

object CachedPullRequest extends ((String, Long, Long, Long, Long, Boolean) => CachedPullRequest) {
  def apply(pr: PullRequest): CachedPullRequest = {
    CachedPullRequest(pr.sha,
      pr.linesAdded.getOrElse(0),
      pr.linesDeleted.getOrElse(0),
      pr.filesChanged.getOrElse(0),
      pr.commits.getOrElse(0),
      pr.hasTestCode.getOrElse(false))
  }
}
