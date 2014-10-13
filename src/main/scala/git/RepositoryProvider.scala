package git

/**
 * Offers the functionality to get data about the repository.
 */
trait RepositoryProvider {
  def defaultBranch: String
}
