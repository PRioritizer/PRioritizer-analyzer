package git

import scala.collection.SortedSet
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Pairwise(pullRequests: List[PullRequest], skipDifferentTargets: Boolean = true) extends PairwiseList {
  /**
   * An ordering for pull requests based on their number.
   */
  implicit val prOrd = Ordering.by[PullRequest, Int](_.number)
  implicit val pairOrd = Ordering.by[PullRequestPair, (PullRequest,PullRequest)](_.toPair)

  val pairs = filterPairs
  val length = pairs.length

  override def get: Future[List[PullRequestPair]] = Future { pairs }

  def unpair = Pairwise.unpair(this)

  private def filterPairs: List[PullRequestPair] = {
    if (skipDifferentTargets)
      getPairs(pullRequests) filter { case PullRequestPair(pr1, pr2, _) => pr1.target == pr2.target }
    else
      getPairs(pullRequests)
  }

  /**
   * Returns a list of distinct paired pull requests.
   * @param pulls A list of pull requests.
   * @return The list of pairs.
   */
  private def getPairs(pulls: List[PullRequest]): List[PullRequestPair] = {
    val pairs = for {
    // Pairwise
      x <- pulls
      y <- pulls
      // Normalize
      if x.number != y.number
      pr1 = if (x.number < y.number) x else y
      pr2 = if (x.number < y.number) y else x
    } yield PullRequestPair(pr1, pr2)

    // Distinct and sort
    SortedSet(pairs: _*).toList
  }
}

object Pairwise {
  implicit val ord = Ordering.by[PullRequest, Int](_.number)

  def pair(pullRequests: List[PullRequest], skipDifferentTargets: Boolean = true) = new Pairwise(pullRequests, skipDifferentTargets)

  def unpair(pairs: Pairwise): List[PullRequest] = unpair(pairs.pairs)

  def unpair(pairs: List[PullRequestPair]): List[PullRequest] = {
    val pulls = distinct(pairs)
    pulls.foreach { pr =>
      pr.conflictsWith = pairs filter {
        case PullRequestPair(pr1, pr2, mergeable) =>
           !mergeable && (pr1 == pr || pr2 == pr)
      } map {
        case PullRequestPair(pr1, pr2, res) =>
          if (pr1 == pr) pr2 else pr1
      }
    }
    pulls
  }

  private def distinct(pairs: List[PullRequestPair]): List[PullRequest] = {
    val list = scala.collection.mutable.ListBuffer[PullRequest]()

    pairs.foreach(pair => {
      if (!list.contains(pair.pr1))
        list += pair.pr1
      if (!list.contains(pair.pr2))
        list += pair.pr2
    })
    list.sortBy(p => p).toList
  }
}
