package git

case class PullRequestPair(pr1: PullRequest,
                           pr2: PullRequest,
                           var isMergeable: Option[Boolean] = None
                           ) {

  var dirty = false
  def tuple: (PullRequest, PullRequest) = (pr1, pr2)

  override def toString: String =
    s"#${pr1.number}: '${pr1.source}' into #${pr2.number}: '${pr2.source}'"
}
