package jgit.data

import git.DataProvider
import org.eclipse.jgit.api.Git

/**
 * An info getter implementation for the JGit library.
 * @param git The git repository.
 */
class JGitDataProvider(val git: Git) extends DataProvider {
}
