import java.util.Properties
import scala.collection.JavaConverters._
import java.nio.file._

/**
 * Settings object that holds the client properties.
 */
object Settings {
  val fileName = "client.properties"
  val resource = getClass.getResource("/" + fileName)
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
    if (resource == null) {
      throw new FileSystemNotFoundException(
        s"The configuration file was not found. Please make sure you copied $fileName.dist to $fileName.")
    }

    // Read properties file
    val path = Paths get resource.toURI
    val reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)
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
