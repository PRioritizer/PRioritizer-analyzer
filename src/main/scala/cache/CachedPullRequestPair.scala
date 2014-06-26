package cache

import java.sql.Date

import git.PullRequestPair

case class CachedPullRequestPair(date: Date, shaOne: String, shaTwo: String, mergeable: Boolean)

object CachedPullRequestPair extends ((Date, String, String, Boolean) => CachedPullRequestPair) {
  def apply(pair: PullRequestPair): CachedPullRequestPair = {
    val key = getKey(pair)
    CachedPullRequestPair(now, key._1, key._2, pair.isMergeable.getOrElse(false))
  }

  // Smallest sha first
  def getKey(pair: PullRequestPair): (String, String) = {
    if (pair.pr1.sha < pair.pr2.sha)
      (pair.pr1.sha, pair.pr2.sha)
    else
      (pair.pr2.sha, pair.pr1.sha)
  }

  private def now: Date = new Date(new java.util.Date().getTime)
}
