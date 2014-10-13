package output

import java.io.File

import git.{Provider, PullRequest, PullRequestProvider}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import output.JsonProtocol._
import spray.json._
import utils.Extensions._

object JsonWriter {
  val indexFile = "index.json"

  def writePullRequests(dir: String, provider: Provider, pullRequests: List[PullRequest]): Unit = {
    val prProvider = provider.pullRequestProvider.orNull
    val repoProvider = provider.repositoryProvider.orNull
    val commitProvider = provider.commitProvider.orNull

    val df = ISODateTimeFormat.dateTime

    val jsonAst = JsObject(
      "source" -> prProvider.source.toJson,
      "owner" -> prProvider.owner.toJson,
      "repository" -> prProvider.repository.toJson,
      "commits" -> commitProvider.commits.toJson,
      "defaultBranch" -> repoProvider.defaultBranch.toJson,
      "date" -> DateTime.now.toDateTime(DateTimeZone.UTC).toString(df).toJson,
      "pullRequests" -> pullRequests.toJson
    )

    val json = jsonAst.prettyPrint // or .compactPrint
    val file = getFile(dir, prProvider)
    writeToFile(file, json)
  }

  def writeIndex(dir: String): Unit = {
    val files = getFileMap(new File(dir))
    val json = files.toJson.prettyPrint // or .compactPrint

    val outputFile = new File(dir, indexFile)
    writeToFile(outputFile, json)
  }

  def getFile(dir: String, provider: PullRequestProvider): File = {
    val ownerDir = provider.owner.safeFileName
    val repoDir: File = new File(dir, ownerDir)
    val repoFile = provider.repository.safeFileName + ".json"
    repoDir.mkdirs()

    new File(repoDir, repoFile)
  }

  private def getFileMap(dir: File): Array[Map[String, String]] = {
    val ext = ".json"
    val jsonFilter = (file: File) => file.getName.endsWith(ext)
    val dirs = dir.listFiles.filter(_.isDirectory)
    val owners = dirs.map(o => new {
      var name = o.getName
      var repos = o.listFiles.filter(jsonFilter).map(f => f.getName.dropRight(ext.length))
    })
    owners.flatMap(owner => owner.repos.map(r => Map("owner" -> owner.name, "repo" -> r, "file" -> s"${owner.name}/$r$ext")))
  }

  private def writeToFile(file: File, contents: String): Unit = {
    val writer = new java.io.PrintWriter(file)
    try writer.write(contents) finally writer.close()
  }
}
