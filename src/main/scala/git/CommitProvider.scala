package git

/**
 * Offers the functionality to get data about the commits.
 */
trait CommitProvider {
  def commits: Long
}
