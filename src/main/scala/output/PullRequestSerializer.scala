package output

import org.joda.time.format.ISODateTimeFormat
import org.json4s.CustomSerializer
import org.json4s.JsonAST._
import git.PullRequest
import org.json4s.JsonDSL._
import org.json4s.JsonAST.JString

object PullRequestSerializer extends CustomSerializer[PullRequest]( format => (
  {
    case JObject(
      JField("number", JInt(number)) ::
      JField("author", JString(author)) ::
      JField("sha", JString(sha)) ::
      JField("source", JString(source)) ::
      JField("target", JString(target)) ::
      Nil
    ) =>
      PullRequest(number.toInt, sha, author, source, target)
  },
  {
    case pr: PullRequest =>
      val df = ISODateTimeFormat.dateTime
      ("number" -> pr.number) ~
      ("author" -> pr.author) ~
      ("sha" -> pr.sha) ~
      ("source" -> pr.source) ~
      ("target" -> pr.target) ~
      ("title" -> pr.title) ~
      ("createdAt" -> pr.createdAtUtc.map(d => d.toString(df))) ~
      ("updatedAt" -> pr.updatedAtUtc.map(d => d.toString(df))) ~
      ("linesAdded" -> pr.linesAdded) ~
      ("linesDeleted" -> pr.linesDeleted) ~
      ("filesChanged" -> pr.filesChanged) ~
      ("commits" -> pr.commits) ~
      ("avatar" -> pr.avatar) ~
      ("coreMember" -> pr.coreMember) ~
      ("comments" -> pr.comments) ~
      ("reviewComments" -> pr.reviewComments) ~
      ("labels" -> pr.labels) ~
      ("milestone" -> pr.milestone) ~
      ("type" -> pr.`type`.map(t => t.toString)) ~
      ("isMergeable" -> pr.isMergeable) ~
      ("conflictsWith" -> pr.conflictsWithNumbers) ~
      ("contributedCommits" -> pr.contributedCommits) ~
      ("acceptedPullRequests" -> pr.acceptedPullRequests) ~
      ("totalPullRequests" -> pr.totalPullRequests)
  }
))
