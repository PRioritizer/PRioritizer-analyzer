package git

//case class PullRequest(branch: String, base:String, account:String, repo:String)
case class PullRequest(number: Int, branch: String, base:String) {
  override def toString(): String = {
    s"#$number: '$branch'"
  }
}

object PullRequest {
  def get = List(
    PullRequest(1, "pr", "master"),
    PullRequest(4, "merge-conflict", "master"),
    PullRequest(3, "merge-via-cmd", "master"),
    PullRequest(5, "cmd-merge", "master"),
    PullRequest(6, "merge-conflict-2", "master"),
    PullRequest(7, "test-jgit", "master"),
    PullRequest(8, "test-conflict", "master")
  )

  def getPairs(pulls: List[PullRequest]): Traversable[(PullRequest, PullRequest)] = {
    val pairs = for {
      // Pairwise
      x <- pulls
      y <- pulls
      // Normalize
      if x.number != y.number
      pr1 = if (x.number < y.number) x else y
      pr2 = if (x.number < y.number) y else x
    } yield (pr1, pr2)
    // Distinct
    pairs.toSet
  }
}
