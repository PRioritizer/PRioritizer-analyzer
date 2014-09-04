package github

import settings.Settings

object GitHubSettings {
  val owner = Settings.get("github.owner").getOrElse("")
  val repository = Settings.get("github.repository").getOrElse("")
  val token = Settings.get("github.token").getOrElse("")

  def validate = owner != null &&
    owner != "" &&
    repository != null &&
    repository != "" &&
    token != null &&
    token != ""
}
