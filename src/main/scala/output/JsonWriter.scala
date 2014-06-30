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
  def writePullRequests(dir: String, provider: Provider, pullRequests: List[PullRequest]): Unit = {
    implicit var formats: Formats = DefaultFormats ++ JodaTimeSerializers.all + PullRequestSerializer
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
