package ghtorrent

import git.RepositoryProvider

import scala.slick.jdbc.{StaticQuery => Q}

/**
 * An info getter implementation for the GHTorrent database.
 * @param provider The GHTorrent provider.
 */
class GHTorrentRepositoryProvider(val provider: GHTorrentProvider) extends RepositoryProvider {
  lazy val repoId = getRepoId
  lazy val commits = getCommitCount
  implicit lazy val session = provider.Db

  private def getRepoId: Int = {
    val owner = provider.owner
    val repo = provider.repository

    // Execute query
    val id = getRepoIdQuery(owner, repo).firstOption
    if (!id.isDefined)
      throw new GHTorrentException(s"Could not find the $owner/$repo repository in the GHTorrent database")

    id.get
  }

  private def getCommitCount: Long =
    getCommitCountQuery(repoId).firstOption.getOrElse(0).toLong

  private lazy val getRepoIdQuery: Q[(String, String), Int] =
    Q[(String, String), Int] +
      """SELECT
        |projects.id
        |FROM
        |projects
        |INNER JOIN users AS owners ON owners.id = projects.owner_id
        |WHERE
        |owners.login = ? AND
        |projects.`name` = ?""".stripMargin

  private lazy val getCommitCountQuery: Q[Int, Int] =
    Q[Int, Int] +
      """SELECT
        |COUNT(project_commits.commit_id)
        |FROM
        |project_commits
        |WHERE
        |project_commits.project_id = ?""".stripMargin
}
