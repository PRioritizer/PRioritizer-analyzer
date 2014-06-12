package output

import git.{PullRequestProvider, PullRequest}
import org.joda.time.{DateTimeZone, DateTime}
import org.json4s.JsonAST.{JArray, JString, JObject}
import org.json4s.{Extraction, JField, Formats, DefaultFormats}
import org.json4s.native.Serialization
import org.json4s.ext.JodaTimeSerializers

object JsonWriter {
  def writePullRequests(file: String, provider: PullRequestProvider, pullRequests: List[PullRequest]): Unit = {
    implicit var formats: Formats = DefaultFormats ++ JodaTimeSerializers.all + PullRequestSerializer
    val jsonObject = JObject(List(
      JField("source", JString(provider.source)),
      JField("owner", JString(provider.owner)),
      JField("repository", JString(provider.repository)),
      JField("date", JString(DateTime.now.toDateTime(DateTimeZone.UTC).toString())),
      JField("pullRequests", Extraction.decompose(pullRequests))
    ))
    val json = Serialization.writePrettyOld(jsonObject)
    writeToFile(file, json)
  }

  private def writeToFile(filePath: String, contents: String): Unit = {
    val file = new java.io.File(filePath)
    val writer = new java.io.PrintWriter(file)
    try writer.write(contents) finally writer.close()
  }
}
