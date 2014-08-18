package github

import dispatch.github.{GhIssue, GhPullRequest}
import git.{PullRequest, PullRequestDecorator, PullRequestList, PullRequestType}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * An info getter implementation for the GitHub API.
 * @param provider The GitHub API provider.
 */
class GitHubDecorator(base: PullRequestList, val provider: GitHubProvider) extends PullRequestDecorator(base) {
  lazy val owner = provider.owner
  lazy val repository = provider.repository

  override def decorate(pullRequest: PullRequest): PullRequest = {
    if (!hasStats(pullRequest))
      enrichStats(pullRequest)

    if (!hasIssueStats(pullRequest))
      enrichWithIssue(pullRequest)

    pullRequest
  }

  private def hasStats(pullRequest: PullRequest): Boolean = pullRequest.commits.isDefined

  private def hasIssueStats(pullRequest: PullRequest): Boolean = pullRequest.`type`.isDefined

  private def enrichWithIssue(pullRequest: PullRequest): PullRequest = {
    val waitIssue = GhIssue.get_issue(owner, repository, pullRequest.number)
    val issue = Await.result(waitIssue, Duration.Inf)

    val labels = issue.labels.map{l => l.name}.mkString(", ")
    val words = labels + ", " + issue.title
    pullRequest.`type` = Some(PullRequestType.parse(words)) // (security fixes > bug fixes > refactoring > features > documentation)
    pullRequest.comments = Some(issue.comments)
    pullRequest.milestone = Some(issue.milestone.fold(0)(m => m.number))
    pullRequest
  }

  private def enrichStats(pullRequest: PullRequest): PullRequest = {
    val waitPr = GhPullRequest.get_pull_request(owner, repository, pullRequest.number)
    val pr = Await.result(waitPr, Duration.Inf)

    pullRequest.isMergeable = Some(pr.mergeable)
    pullRequest.commits = Some(pr.commits)
    pullRequest.linesAdded = Some(pr.additions)
    pullRequest.linesDeleted = Some(pr.deletions)
    pullRequest.filesChanged = Some(pr.changed_files)
    pullRequest
  }
}
