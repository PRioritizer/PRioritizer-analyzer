package predictor

import settings.Settings

object PredictorSettings {
  val command = Settings.get("predictor.command").getOrElse("")
  val directory = Settings.get("model.directory").getOrElse("")

  def validate = command != null &&
    command != "" &&
    directory != null &&
    directory != ""
}
