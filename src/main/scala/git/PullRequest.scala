package git

import scala.collection.immutable.SortedSet
import org.joda.time.DateTime

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
   * The ratio of previously accepted pull requests of the same requester.
   */
  var previouslyAcceptedPullRequests: Int = _
  /**
   * The number of previously created pull requests of the same requester.
   */
  var previouslyCreatedPullRequests: Int = _

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
