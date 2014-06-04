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
case class PullRequest(number: Int,
                  author: String,
                  source: String,
                  target: String) {
  /**
   * The title.
   */
  var title: String = _
  /**
   * The creation date.
   */
  var createdAt: DateTime = _
  /**
   * The last modification date.
   */
  var updatedAt: DateTime = _
  /**
   * The number of added lines.
   */
  var linesAdded: Long = _
  /**
   * The number of deleted lines.
   */
  var linesDeleted: Long = _
  /**
   * The number of changed files.
   */
  var filesChanged: Long = _
  /**
   * The number of commits.
   */
  var commits: Long = _
  /**
   * The number of comments.
   */
  var comments: Long = _
  /**
   * The number of comments.
   */
  var `type`: PullRequestType = PullRequestType.Unknown
  /**
   * Indicates whether this PR is mergeable with its target
   */
  var isMergeable: Boolean = _
  /**
   * Contains a list of PRs that conflict with this PR.
   */
  var conflictsWith: List[PullRequest] = List()
  /**
   * Indicates the rate of involvement of the requester.
   */
  var contributorIndex: Double = _
  /**
   * The number of accepted pull requests by the same requester.
   */
  var acceptedPullRequests: Int = _
  /**
   * The number of other created pull requests by the same requester.
   */
  var totalPullRequests: Int = _

  /**
   * @return The total number of added/edited/deleted lines.
   */
  def linesTotal: Long = linesAdded + linesDeleted

  override def toString: String =
    s"#$number: '$author:$source' into '$target'"
}

/**
 * Helper functions for pull requests.
 */
object PullRequest {
  /**
   * An ordering for pull requests based on their number.
   */
  implicit val ord = Ordering.by[PullRequest, Int](_.number)

  /**
   * Returns a list of distinct paired pull requests.
   * @param pulls A list of pull requests.
   * @return The list of pairs.
   */
  def getPairs(pulls: List[PullRequest]): List[(PullRequest, PullRequest)] = {
    val pairs = for {
      // Pairwise
      x <- pulls
      y <- pulls
      // Normalize
      if x.number != y.number
      pr1 = if (x.number < y.number) x else y
      pr2 = if (x.number < y.number) y else x
    } yield (pr1, pr2)

    // Distinct and sort
    SortedSet(pairs: _*).toList
  }
}

/**
 * An enum type for severity levels.
 */
object PullRequestType extends Enumeration {
  type PullRequestType = Value
  val Fix, Refactor, Feature, Documentation, Unknown = Value
}
