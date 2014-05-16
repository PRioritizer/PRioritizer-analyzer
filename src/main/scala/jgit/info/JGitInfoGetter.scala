package jgit.info

import git.{GitHubInfo, InfoGetter}
import org.eclipse.jgit.api.Git

/**
 * An info getter implementation for the JGit library.
 * @param git The git repository.
 * @param remote The name of the GitHub remote.
 */
class JGitInfoGetter(val git: Git, val remote: String) extends InfoGetter {
  /**
   * @return True iff the repository contains info about GitHub.
   */
  override def hasGitHubInfo: Boolean = getGitHubInfo.isDefined

  /**
   * @return The owner and name of the GitHub repository.
   */
  override def gitHubInfo: GitHubInfo = getGitHubInfo.get

  /**
   * @return The owner and name of the GitHub repository.
   */
  def getGitHubInfo: Option[GitHubInfo] = {
    val config = git.getRepository.getConfig
    val url = config.getString("remote", remote, "url")

    // Match     git@github.com:<owner>/<repo>.git
    // -OR-  https://github.com/<owner>/<repo>.git
    val gitHub = "^(?:git@|https?://)github\\.com(?::|/)(.*?)/(.*?)\\.git$".r

    url match {
      case gitHub(owner, repo) => Some(GitHubInfo(owner,repo))
      case _ => None
    }
  }
}
