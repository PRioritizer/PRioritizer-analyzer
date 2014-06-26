package git

import scala.collection.immutable.SortedSet
import org.joda.time.DateTime
import git.PullRequestType.PullRequestType

/**
 * An object that holds information about the pull request.
 * @param number The number of the pull request.
 * @param author The author name.
 * @param source The source branch name.
 * @param target The target branch name.
 */
case class PullRequest( number: Int,
                        author: String,
                        sha: String,
                        source: String,
                        target: String,
                        var title: Option[String] = None,
                        var createdAt: Option[DateTime] = None,
                        var updatedAt: Option[DateTime] = None,
                        var linesAdded: Option[Long] = None,
                        var linesDeleted: Option[Long] = None,
                        var filesChanged: Option[Long] = None,
                        var commits: Option[Long] = None,
                        var coreMember: Option[Boolean] = None,
                        var comments: Option[Long] = None,
                        var milestone: Option[Long] = None,
                        var `type`: Option[PullRequestType] = None,
                        var isMergeable: Option[Boolean] = None,
                        var conflictsWith: Option[List[PullRequest]] = None,
                        var contributedCommits: Option[Int] = None,
                        var acceptedPullRequests: Option[Int] = None,
                        var totalPullRequests: Option[Int] = None
                        ) {
  /**
   * @return The total number of added/edited/deleted lines.
   */
  def linesTotal: Long = linesAdded.getOrElse(0L) + linesDeleted.getOrElse(0L)

  def createdAtUtc: Option[DateTime] = createdAt.map(date => date.toDateTime(DateTimeZone.UTC))

  def updatedAtUtc: Option[DateTime] = updatedAt.map(date => date.toDateTime(DateTimeZone.UTC))

  def conflictsWithNumbers: Option[List[Int]] = conflictsWith.map(list => list.map(pr => pr.number))

  override def toString: String =
    s"#$number: '$source' into '$target'"
}
