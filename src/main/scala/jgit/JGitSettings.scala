package jgit

import settings.Settings

object JGitSettings {
  val directory = Settings.get("jgit.directory").getOrElse("")
  val clean = Settings.get("jgit.clean").map(c => c.toBoolean).getOrElse(false)

  def validate = directory != null &&
    directory != ""
}
