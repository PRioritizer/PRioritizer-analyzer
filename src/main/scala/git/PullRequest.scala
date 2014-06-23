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
                        source: String,
                        target: String,
                        var title: String = "",
                        var createdAt: DateTime = null,
                        var updatedAt: DateTime = null,
                        var linesAdded: Long = 0,
                        var linesDeleted: Long = 0,
                        var filesChanged: Long = 0,
                        var commits: Long = 0,
                        var coreMember: Boolean = false,
                        var comments: Long = 0,
                        var milestone: Long = 0,
                        var `type`: PullRequestType = PullRequestType.Unknown,
                        var isMergeable: Boolean = false,
                        var conflictsWith: List[PullRequest] = List(),
                        var contributedCommits: Int = 0,
                        var acceptedPullRequests: Int = 0,
                        var totalPullRequests: Int = 0
                        ) {
  /**
   * @return The total number of added/edited/deleted lines.
   */
  def linesTotal: Long = linesAdded + linesDeleted

  override def toString: String =
    s"#$number: '$source' into '$target'"
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
