package git

import scala.collection.immutable.SortedSet

//case class PullRequest(branch: String, base:String, account:String, repo:String)
case class PullRequest(number: Int, branch: String, base:String) {
  override def toString(): String = {
    s"#$number: '$branch' into '$base'"
  }
}

object PullRequest {
  implicit val ord = Ordering.by[PullRequest, Int](_.number)

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

    // Distinct and sort on base branch
    SortedSet(pairs: _*)
  }
}
