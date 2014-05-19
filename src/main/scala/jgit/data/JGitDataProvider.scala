package jgit.data

import git.DataProvider
import org.eclipse.jgit.api.Git

/**
 * An info getter implementation for the JGit library.
 * @param git The git repository.
 * @param remote The name of the GitHub remote.
 */
class JGitDataProvider(val git: Git, val remote: String) extends DataProvider {
}
