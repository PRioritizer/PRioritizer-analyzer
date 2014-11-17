package output

import git.PullRequest
import org.joda.time.format.ISODateTimeFormat
import output.JsonProtocol._
import spray.json._

object PullRequestJsonFormat extends RootJsonFormat[PullRequest] {
  def write(pr: PullRequest) = {
    val df = ISODateTimeFormat.dateTime

    val jsonAst = JsObject(
      "number" -> pr.number.toJson,
      "author" -> pr.author.toJson,
      "sha" -> pr.sha.toJson,
      "source" -> pr.source.toJson,
      "target" -> pr.target.toJson,
      "title" -> pr.title.toJson,
      "createdAt" -> pr.createdAtUtc.map(d => d.toString(df)).toJson,
      "updatedAt" -> pr.updatedAtUtc.map(d => d.toString(df)).toJson,
      "linesAdded" -> pr.linesAdded.toJson,
      "linesDeleted" -> pr.linesDeleted.toJson,
      "filesChanged" -> pr.filesChanged.toJson,
      "commits" -> pr.commits.toJson,
      "avatar" -> pr.avatar.toJson,
      "coreMember" -> pr.coreMember.toJson,
      "comments" -> pr.comments.toJson,
      "reviewComments" -> pr.reviewComments.toJson,
      "labels" -> pr.labels.toJson,
      "milestone" -> pr.milestone.toJson,
      "type" -> pr.`type`.map(t => t.toString).toJson,
      "isMergeable" -> pr.isMergeable.toJson,
      "conflictsWith" -> pr.conflictsWithNumbers.toJson,
      "contributedCommits" -> pr.contributedCommits.toJson,
      "acceptedPullRequests" -> pr.acceptedPullRequests.toJson,
      "totalPullRequests" -> pr.totalPullRequests.toJson,
      "hasTestCode" -> pr.hasTestCode.toJson,
      "important" -> pr.important.toJson
    )
    jsonAst
  }

  def read(value: JsValue) = null
}
