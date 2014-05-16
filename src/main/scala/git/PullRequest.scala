package git

import scala.collection.immutable.SortedSet

/**
 * An object that holds information about the pull request.
 * @param number The number of the pull request.
 * @param branch The source branch name.
 * @param base The target branch name.
 */
case class PullRequest(number: Int, branch: String, base:String) {
  override def toString: String = {
    s"#$number: '$branch' into '$base'"
  }
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
