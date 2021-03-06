package ghtorrent

import settings.Settings

object GHTorrentSettings {
  val host = Settings.get("ghtorrent.host").getOrElse("localhost")
  val port = Settings.get("ghtorrent.port").map(p => p.toInt).getOrElse(3306)
  val username = Settings.get("ghtorrent.username").getOrElse("")
  val password = Settings.get("ghtorrent.password").getOrElse("")
  val database = Settings.get("ghtorrent.database").getOrElse("")

  def validate = host != null &&
    host != "" &&
    port != 0 &&
    username != null &&
    username != "" &&
    password != null &&
    password != "" &&
    database != null &&
    database != ""
}

object GHTorrentMongoSettings {
  val host = Settings.get("ghtorrent.mongodb.host").getOrElse("localhost")
  val port = Settings.get("ghtorrent.mongodb.port").map(p => p.toInt).getOrElse(27017)
  val username = Settings.get("ghtorrent.mongodb.username").getOrElse("")
  val password = Settings.get("ghtorrent.mongodb.password").getOrElse("")
  val database = Settings.get("ghtorrent.mongodb.database").getOrElse("")
  val repositoriesCollection = Settings.get("ghtorrent.mongodb.collection.repositories").getOrElse("")
  val issueCommentsCollection = Settings.get("ghtorrent.mongodb.collection.issue.comments").getOrElse("")
  val reviewCommentCollection = Settings.get("ghtorrent.mongodb.collection.pullrequest.comments").getOrElse("")

  def validate = host != null &&
    host != "" &&
    port != 0 &&
    username != null &&
    username != "" &&
    password != null &&
    password != "" &&
    database != null &&
    database != "" &&
    repositoriesCollection != null &&
    repositoriesCollection != "" &&
    issueCommentsCollection != null &&
    issueCommentsCollection != "" &&
    reviewCommentCollection != null &&
    reviewCommentCollection != ""
}
