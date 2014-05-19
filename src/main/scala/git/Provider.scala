package git

trait Provider {
  def pullRequests: Option[PullRequestProvider]
  def merger: Option[MergeProvider]
  def data: Option[DataProvider]

  def dispose(): Unit = {}
}
