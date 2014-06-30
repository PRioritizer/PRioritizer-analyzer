package cache.models

import git.PullRequestPair

case class CachedPullRequestPair(shaOne: String, shaTwo: String, isMergeable: Boolean) {
  def fill(pair: PullRequestPair): PullRequestPair = {
    pair.isMergeable = Some(isMergeable)
    pair
  }

  def represents(pair: PullRequestPair): Boolean = {
    this == CachedPullRequestPair(pair)
  }
}

object CachedPullRequestPair extends ((String, String, Boolean) => CachedPullRequestPair) {
  def apply(pair: PullRequestPair): CachedPullRequestPair = {
    val key = getKey(pair)
    CachedPullRequestPair(key._1, key._2,
      pair.isMergeable.getOrElse(false))
  }

  // Smallest sha first
  def getKey(pair: PullRequestPair): (String, String) = {
    if (pair.pr1.sha < pair.pr2.sha)
      (pair.pr1.sha, pair.pr2.sha)
    else
      (pair.pr2.sha, pair.pr1.sha)
  }
}
