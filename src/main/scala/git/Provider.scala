package git

trait Provider {
  def repositoryProvider: Option[RepositoryProvider]
  def pullRequestProvider: Option[PullRequestProvider]
  def mergeProvider: Option[MergeProvider]
  def enrichmentProvider: Option[EnrichmentProvider]

  def dispose(): Unit = {}
}
