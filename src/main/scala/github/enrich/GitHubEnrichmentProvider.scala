package github.enrich

import git.{PullRequestType, PullRequest, EnrichmentProvider}
import dispatch.github.{GhPullRequest, GhIssue}
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import github.GitHubProvider
import scala.concurrent.duration.Duration
import git.PullRequestType.PullRequestType

/**
 * An info getter implementation for the GitHub API.
 * @param provider The GitHub API provider.
 */
class GitHubEnrichmentProvider(val provider: GitHubProvider) extends EnrichmentProvider {
  val owner = provider.owner
  val repository = provider.repository

  override def enrich(pullRequest: PullRequest): Future[PullRequest] = {
    Future {
      val pr = if (!hasStats(pullRequest))
        enrichStats(pullRequest)
      else
        pullRequest

      enrichWithIssue(pr)
    }
  }

  private def hasStats(pullRequest: PullRequest): Boolean = pullRequest.commits > 0

  private def enrichWithIssue(pullRequest: PullRequest): PullRequest = {
    val waitIssue = GhIssue.get_issue(owner, repository, pullRequest.number)
    val issue = Await.result(waitIssue, Duration.Inf)

    val labels = issue.labels.map{l => l.name}.mkString(", ")
    val words = labels + ", " + issue.title
    pullRequest.`type` = PullRequestType.parse(words) // (security fixes > bug fixes > refactoring > features > documentation)
    pullRequest.comments = issue.comments
    pullRequest
  }

  private def enrichStats(pullRequest: PullRequest): PullRequest = {
    val waitPr = GhPullRequest.get_pull_request(owner, repository, pullRequest.number)
    val pr = Await.result(waitPr, Duration.Inf)

    pullRequest.comments = pr.comments
    pullRequest.isMergeable = pr.mergeable
    pullRequest.commits = pr.commits
    pullRequest.linesAdded = pr.additions
    pullRequest.linesDeleted = pr.deletions
    pullRequest.filesChanged = pr.changed_files
    pullRequest
  }
}
