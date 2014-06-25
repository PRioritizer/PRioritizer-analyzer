package jgit

import git._
import jgit.JGitExtensions._
import jgit.JGitProvider._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * An info getter implementation for the JGit library.
 * @param provider The JGit provider.
 */
class JGitPairwiseMerger(base: PairwiseList, val provider: JGitProvider) extends PairwiseDecorator(base) {
  val repo = provider.repository

  override def decorate(pair: PullRequestPair): PullRequestPair = {
    if (pair.isMergeable.isDefined)
      return pair

    val result = repo.isMergeable(pullRef(pair.pr1), pullRef(pair.pr2))
    pair.isMergeable = Some(MergeResult.isSuccess(result))
    pair.dirty = true
    pair
  }
}
