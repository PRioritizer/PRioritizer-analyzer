package output

import org.json4s.CustomSerializer
import org.json4s.JsonAST._
import git.PullRequest
import org.json4s.JsonDSL._
import org.json4s.JsonAST.JString
import org.json4s.JsonAST.JBool
import org.json4s.ext.JodaTimeSerializers
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTimeZone

object PullRequestSerializer extends CustomSerializer[PullRequest]( format => (
  {
    case JObject(
      JField("number", JInt(number)) ::
      JField("author", JString(author)) ::
      JField("source", JString(source)) ::
      JField("target", JString(target)) ::
      Nil
    ) =>
      PullRequest(number.toInt, author, source, target)
  },
  {
    case pr: PullRequest =>
      ("number" -> pr.number) ~
      ("author" -> pr.author) ~
      ("source" -> pr.source) ~
      ("target" -> pr.target) ~
      ("createdAt" -> pr.createdAt.toDateTime(DateTimeZone.UTC).toString()) ~
      ("updatedAt" -> pr.updatedAt.toDateTime(DateTimeZone.UTC).toString()) ~
      ("linesAdded" -> pr.linesAdded) ~
      ("linesDeleted" -> pr.linesDeleted) ~
      ("isMergeable" -> pr.isMergeable) ~
      ("conflictsWith" -> pr.conflictsWith.map(_.number))
  }
))
