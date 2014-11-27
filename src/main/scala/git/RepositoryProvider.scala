package git

/**
 * Offers the functionality to get data about the repository.
 */
trait RepositoryProvider {
  def defaultBranch: String
  def branchTips: Map[String, String] // name -> sha
}
