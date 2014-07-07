package output

import java.io.File
import git.{Provider, PullRequestProvider, PullRequest}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTimeZone, DateTime}
import org.json4s.JsonAST.{JInt, JString, JObject}
import org.json4s.{Extraction, JField, Formats, DefaultFormats}
import org.json4s.native.Serialization
import org.json4s.ext.JodaTimeSerializers
import utils.Extensions._

object JsonWriter {
  val indexFile = "index.json"
  implicit val formats: Formats = DefaultFormats ++ JodaTimeSerializers.all + PullRequestSerializer

  def writePullRequests(dir: String, provider: Provider, pullRequests: List[PullRequest]): Unit = {
    val prProvider = provider.pullRequestProvider.orNull
    val repoProvider = provider.repositoryProvider.orNull

    val df = ISODateTimeFormat.dateTime
    val jsonObject = JObject(List(
      JField("source", JString(prProvider.source)),
      JField("owner", JString(prProvider.owner)),
      JField("repository", JString(prProvider.repository)),
      JField("commits", JInt(repoProvider.commits)),
      JField("date", JString(DateTime.now.toDateTime(DateTimeZone.UTC).toString(df))),
      JField("pullRequests", Extraction.decompose(pullRequests))
    ))

    val json = Serialization.writePrettyOld(jsonObject)
    val file = getFile(dir, prProvider)
    writeToFile(file, json)
  }

  def writeIndex(dir: String): Unit = {
    val files = getFileMap(new File(dir))
    val json = Serialization.writePrettyOld(files)

    val outputFile = new File(dir, indexFile)
    writeToFile(outputFile, json)
  }

  private def getFileMap(dir: File): Array[Map[String, String]] = {
    val jsonFilter = (file: File) => file.getName.endsWith(".json")
    val dirs = dir.listFiles.filter(_.isDirectory)
    val owners = dirs.map(o => new {
      var name = o.getName
      var repos = o.listFiles.filter(jsonFilter).map(f => f.getName)
    })
    owners.flatMap(owner => owner.repos.map(r => Map("owner" -> owner.name, "repo" -> r)))
  }

  private def getFile(dir: String, provider: PullRequestProvider): File = {
    val ownerDir = provider.owner.safeFileName
    val repoDir: File = new File(dir, ownerDir)
    val repoFile = provider.repository.safeFileName + ".json"
    repoDir.mkdirs()

    new File(repoDir, repoFile)
  }

  private def writeToFile(file: File, contents: String): Unit = {
    val writer = new java.io.PrintWriter(file)
    try writer.write(contents) finally writer.close()
  }
}
