package ghtorrent

import git.RepositoryProvider

import scala.slick.jdbc.StaticQuery

/**
 * An info getter implementation for the GHTorrent database.
 * @param provider The GHTorrent provider.
 */
class GHTorrentRepositoryProvider(val provider: GHTorrentProvider) extends RepositoryProvider {
  lazy val repoId = getRepoId
  lazy val commits = getCommitCount

  def getRepoId: Int = {
    val owner = provider.owner
    val repo = provider.repository
    implicit val session = provider.Db

    // Execute query
    val query = getRepoIdQuery
    val id = query.apply(owner, repo).list(session).head
    id
  }

  def getCommitCount: Long = {
    implicit val session = provider.Db

    // Execute query
    val query = getCommitCountQuery
    val count = query.apply(repoId).list(session).head
    count
  }

  def getRepoIdQuery: StaticQuery[(String, String), Int] = {
    StaticQuery.query[(String, String), Int](
      """SELECT
        |projects.id
        |FROM
        |projects
        |INNER JOIN users AS owners ON owners.id = projects.owner_id
        |WHERE
        |owners.login = ? AND
        |projects.`name` = ?""".stripMargin)
  }

  def getCommitCountQuery: StaticQuery[Int, Int] = {
    StaticQuery.query[Int, Int](
      """SELECT
        |COUNT(project_commits.commit_id)
        |FROM
        |project_commits
        |WHERE
        |project_commits.project_id = ?""".stripMargin)
  }
}
