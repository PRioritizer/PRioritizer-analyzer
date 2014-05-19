package git

trait Provider {
  def pullRequests: PullRequestProvider
  def merger: MergeProvider
  def data: DataProvider
}
