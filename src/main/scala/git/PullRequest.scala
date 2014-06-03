package git

import scala.collection.immutable.SortedSet

/**
 * An object that holds information about the pull request.
 * @param number The number of the pull request.
 * @param branch The source branch name.
 * @param target The target branch name.
 */
case class PullRequest(number: Int,
                  branch: String,
                  target: String) {
  /**
   * The number of added/deleted/changed lines.
   */
  var lineCount: Long = 0L
  /**
   * Indicates whether this PR is mergeable with its target
   */
  var isMergeable: Boolean = false
  /**
   * Contains a list of PRs that conflict with this PR.
   */
  var conflictsWith: List[PullRequest] = List()

  override def toString: String =
    s"#$number: '$branch' into '$target'"
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
