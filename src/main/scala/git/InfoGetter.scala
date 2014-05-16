package git

/**
 * Offers the functionality to get info about the repository.
 */
trait InfoGetter {
  /**
   * @return True iff the repository contains info about GitHub.
   */
  def hasGitHubInfo: Boolean

  /**
   * @return The owner and name of the GitHub repository.
   */
  def gitHubInfo: GitHubInfo
}

/**
 * An object that holds GitHub information about the repository.
 * @param owner The name of the owner.
 * @param repository The name of the repository.
 */
case class GitHubInfo(owner: String, repository: String)
