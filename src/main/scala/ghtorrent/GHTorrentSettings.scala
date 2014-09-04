package ghtorrent

import settings.Settings

object GHTorrentSettings {
  val host = Settings.get("ghtorrent.host").getOrElse("")
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
