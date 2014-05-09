import java.util.Properties
import scala.collection.JavaConverters._
import java.nio.file._

object Settings {
  val fileName = "client.properties"
  val resource = getClass.getResource("/" + fileName)
  val data = read

  def has(property: String): Boolean =
    data.get(property).isDefined

  def get(property: String): Option[String] =
    data.get(property)

  def token: String =
    get("github.api.PersonalAccessToken").orNull

  private def read: Map[String, String] = {
    if (resource == null) {
      throw new FileSystemNotFoundException(
        s"The configuration file was not found. Please make sure you copied $fileName.dist to $fileName.")
    }

    // Read properties file
    val path = Paths get resource.toURI
    val reader = Files.newBufferedReader(path)
    val props = new Properties
    props.load(reader)
    props.asScala.toMap
  }
}
