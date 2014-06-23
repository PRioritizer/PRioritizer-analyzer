package git

import scala.concurrent.Future

/**
 * Offers the functionality to get data about the repository.
 */
trait RepositoryProvider {
  def commits: Long
}
