package git

import scala.collection.immutable.SortedSet

/**
 * An object that holds information about the pull request.
 */
trait PullRequest {
  /**
   * @return The number of the pull request.
   */
  def number: Int
  /**
   * @return The source branch name.
   */
  def branch: String
  /**
   * @return The target branch name.
   */
  def target: String
  /**
   * @return The base commit name.
   */
  def base: String

  override def toString: String = {
    s"#$number: '$branch' into '$target'"
  }
}

/**
 * An object that holds information about the pull request.
 * @param number The number of the pull request.
 * @param branch The source branch name.
 * @param target The target branch name.
 * @param base The base commit name.
 */
case class SimplePullRequest(number: Int, branch: String, target: String, base: String) extends PullRequest

/**
 * An object that holds information about the pull request.
 * @param number The number of the pull request.
 * @param branch The source branch name.
 * @param target The target branch name.
 * @param base The base commit name.
 * @param lineCount The number of added/deleted/changed lines.
 */
case class RichPullRequest(number: Int,
                           branch: String,
                           target: String,
                           base: String,
                           lineCount: Long) extends PullRequest {
}

/**
 * Helper functions for pull requests.
 */
object RichPullRequest {
  implicit val ord = Ordering.by[RichPullRequest, Int](_.number)

  def apply(pr: PullRequest): RichPullRequest =
    apply(pr, -1)

  def apply(pr: PullRequest, lineCount: Long): RichPullRequest =
    RichPullRequest(pr.number, pr.branch, pr.target, pr.base, lineCount)
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
  def getPairs(pulls: List[PullRequest]): SortedSet[(PullRequest, PullRequest)] = {
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
    SortedSet(pairs: _*)
  }
}
