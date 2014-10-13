package ghtorrent

import git.{CommitProvider, RepositoryProvider}

import scala.slick.jdbc.{StaticQuery => Q}

/**
 * An info getter implementation for the GHTorrent database.
 * @param provider The GHTorrent provider.
 */
class GHTorrentCommitProvider(val provider: GHTorrentProvider) extends CommitProvider {
  lazy val repoId = provider.repositoryProvider match {
    case Some(p: GHTorrentRepositoryProvider) => p.repoId
    case _ => -1
  }
  lazy val commits = getCommitCount
  implicit lazy val session = provider.Db

  private def getCommitCount: Long =
    getCommitCountQuery(repoId).firstOption.getOrElse(0).toLong

  private lazy val getCommitCountQuery: Q[Int, Int] =
    Q[Int, Int] +
      """SELECT
        |COUNT(project_commits.commit_id)
        |FROM
        |project_commits
        |WHERE
        |project_commits.project_id = ?""".stripMargin
}
