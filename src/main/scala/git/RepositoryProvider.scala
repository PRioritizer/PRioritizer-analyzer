package git

/**
 * Offers the functionality to get data about the repository.
 */
trait RepositoryProvider {
  def commits: Long
  def defaultBranch: String
}
