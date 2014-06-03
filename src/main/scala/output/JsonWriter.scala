package output

import git.PullRequest
import org.json4s.{Formats, DefaultFormats}
import org.json4s.native.Serialization

object JsonWriter {
  def writePullRequests(file: String, pullRequests: List[PullRequest]): Unit = {
    implicit var formats: Formats = DefaultFormats + PullRequestSerializer
    val json = Serialization.writePrettyOld(pullRequests)
    writeToFile(file, json)
  }

  private def writeToFile(filePath: String, contents: String): Unit = {
    val file = new java.io.File(filePath)
    val writer = new java.io.PrintWriter(file)
    try writer.write(contents) finally writer.close()
  }
}
