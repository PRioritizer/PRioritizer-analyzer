package settings

import java.io.{BufferedReader, FileNotFoundException, InputStreamReader}
import java.util.Properties

import scala.collection.JavaConverters._

object GeneralSettings {
  lazy val updateInterval = Settings.get("update.interval").map(p => p.toInt).getOrElse(120)
  lazy val pairTargetsEqual = Settings.get("pairs.targets.equal").map(c => c.toBoolean).getOrElse(true)
  lazy val outputDirectory = Settings.get("output.directory").getOrElse("")
  lazy val outputIndex = Settings.get("output.index").map(c => c.toBoolean).getOrElse(false)
  lazy val monitorIntervalType = Settings.get("monitor.interval.type").getOrElse("percentage")
  lazy val monitorIntervalValue = Settings.get("monitor.interval.value").map(p => p.toInt).getOrElse(10)
}

object ProviderSettings {
  lazy val repository = Settings.get("provider.repository")
  lazy val pullRequests = Settings.get("provider.requests")

  lazy val single = Settings.get("decorators.single") match {
    case Some(list) => list.split(',').toList; case _ => List()
  }

  lazy val pairwise = Settings.get("decorators.pairwise") match {
    case Some(list) => list.split(',').toList; case _ => List()
  }

  lazy val all = List(
    repository match { case Some(p) => List(p); case _ => List() },
    pullRequests match { case Some(p) => List(p); case _ => List() },
    single, pairwise).flatten
}

object CacheSettings {
  val directory = Settings.get("cache.directory").getOrElse("")
}

object GHTorrentSettings {
  val host = Settings.get("ghtorrent.host").getOrElse("")
  val port = Settings.get("ghtorrent.port").map(p => p.toInt).getOrElse(3306)
  val username = Settings.get("ghtorrent.username").getOrElse("")
  val password = Settings.get("ghtorrent.password").getOrElse("")
  val database = Settings.get("ghtorrent.database").getOrElse("")

}

object GitHubSettings {
  val owner = Settings.get("github.owner").getOrElse("")
  val repository = Settings.get("github.repository").getOrElse("")
  val token = Settings.get("github.token").getOrElse("")

}

object JGitSettings {
  val directory = Settings.get("jgit.directory").getOrElse("")
  val clean = Settings.get("jgit.clean").map(c => c.toBoolean).getOrElse(false)
}

/**
 * Settings object that holds the client properties.
 */
object Settings {
  val fileName = "settings.properties"
  val resource = getClass.getResourceAsStream("/" + fileName)
  val data = read

  /**
   * @param property The name of the property.
   * @return True iff there exists a property with the give name.
   */
  def has(property: String): Boolean =
    data.get(property).isDefined

  /**
   * @param property The name of the property.
   * @return The value of the property.
   */
  def get(property: String): Option[String] =
    data.get(property)

  /**
   * Read the properties from the config file.
   * @return A map with the properties.
   */
  private def read: Map[String, String] = {
    if (resource == null)
      throw new FileNotFoundException(
        s"The configuration file was not found. Please make sure you copied $fileName.dist to $fileName.")

    // Read properties file
    val reader = new BufferedReader(new InputStreamReader(resource, java.nio.charset.StandardCharsets.UTF_8))
    val props = new Properties
    props.load(reader)
    props.readSystemOverride()
    props.asScala.toMap
  }

  implicit class RichProperties(properties: Properties) {
    def readSystemOverride(): Unit = {
      val keys = properties.keySet().asScala.collect({ case str: String => str })

      keys.foreach(key => {
        val propOverride = System.getProperty(key)
        if (propOverride != null)
          properties.put(key, propOverride)
      })
    }
  }
}
