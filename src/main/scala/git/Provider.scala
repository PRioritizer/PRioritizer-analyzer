package git

trait Provider {
  def pullRequestProvider: Option[PullRequestProvider]
  def mergeProvider: Option[MergeProvider]
  def enrichmentProvider: Option[EnrichmentProvider]

  def dispose(): Unit = {}
}
