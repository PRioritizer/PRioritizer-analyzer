package output

import java.io.File

import git.{Provider, PullRequest, PullRequestProvider}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import output.JsonProtocol._
import spray.json._
import utils.Extensions._
import com.roundeights.hasher.Implicits._

import scala.io.Source

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
    val file = getHashedFile(dir, prProvider) // or getFile(dir, prProvider)
    writeToFile(file, json)
  }

  def writeIndex(dir: String): Unit = {
    val files = getFileMap(new File(dir))
    val json = files.toJson.prettyPrint // or .compactPrint

    val outputFile = new File(dir, indexFile)
    writeToFile(outputFile, json)
  }

  def getFile(dir: String, provider: PullRequestProvider): File = {
    val ownerDir = provider.owner.toLowerCase.safeFileName
    val repoDir: File = new File(dir, ownerDir)
    val repoFile = provider.repository.toLowerCase.safeFileName + ".json"
    repoDir.mkdirs()

    new File(repoDir, repoFile)
  }

  def getHashedFile(dir: String, provider: PullRequestProvider): File = {
    val ownerDir = provider.owner.toLowerCase.safeFileName
    val repoDir: File = new File(dir, ownerDir)
    val repoFile = getHash(provider) + ".json"
    repoDir.mkdirs()

    new File(repoDir, repoFile)
  }

  private def getFileMap(dir: File): Array[Map[String, String]] = {
    val ext = ".json"
    val jsonFilter = (file: File) => file.getName.endsWith(ext)
    val dirs = dir.listFiles.filter(_.isDirectory)
    val owners = dirs.map(o => new {
      var name = o.getName
      var files = o.listFiles.filter(jsonFilter)
    })

    val list = owners.flatMap(owner => owner.files.map(f => Map("owner" -> owner.name, "repo" -> readRepoFromFile(f), "file" -> s"${owner.name}/${f.getName}")))
    list
  }

  private def readRepoFromFile(file: File): String = {
    for (line <- Source.fromFile(file).getLines()) {
      if (line.trim().substring(1).startsWith("repository")) {
        val col = line.indexOf(':')
        val com = line.lastIndexOf(',')
        val repoJson = line.substring(col + 1, com)
        return repoJson.parseJson.convertTo[String]
      }
    }
    ""
  }

  private def writeToFile(file: File, contents: String): Unit = {
    val writer = new java.io.PrintWriter(file)
    try writer.write(contents) finally writer.close()
  }

  private def getHash(provider: PullRequestProvider) : String = {
    val owner = provider.owner.toLowerCase.safeFileName
    val repo = provider.repository.toLowerCase.safeFileName
    val salt = "Analyz3r"
    val value = salt + owner + '/' + repo
    val hash = value.sha256.hex
    hash.substring(0, 10)
  }
}
