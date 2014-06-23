package git

trait Provider {
  def repositoryProvider: Option[RepositoryProvider]
  def pullRequestProvider: Option[PullRequestProvider]
  def mergeProvider: Option[MergeProvider]
  def getDecorator(list: PullRequestList): Option[PullRequestList]

  def dispose(): Unit = {}
}
