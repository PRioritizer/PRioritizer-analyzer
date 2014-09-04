package cache

import settings.Settings

object CacheSettings {
  val directory = Settings.get("cache.directory").getOrElse("")

  def validate = directory != null &&
    directory != ""
}
