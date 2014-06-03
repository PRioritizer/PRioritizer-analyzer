package output

import org.json4s.CustomSerializer
import org.json4s.JsonAST._
import git.PullRequest
import org.json4s.JsonDSL._
import org.json4s.JsonAST.JString
import org.json4s.JsonAST.JBool

object PullRequestSerializer extends CustomSerializer[PullRequest]( format => (
  {
    case JObject(
      JField("number", JInt(number)) ::
      JField("branch", JString(branch)) ::
      JField("target", JString(target)) ::
      JField("lineCount", JInt(lineCount)) ::
      JField("isMergeable", JBool(isMergeable)) ::
      Nil
    ) =>
      val pr = PullRequest(number.toInt, branch, target)
      pr.lineCount = lineCount.toInt
      pr.isMergeable = isMergeable
      pr
  },
  {
    case pr: PullRequest =>
      ("number" -> pr.number) ~
      ("branch" -> pr.branch) ~
      ("target" -> pr.target) ~
      ("lineCount" -> pr.lineCount) ~
      ("isMergeable" -> pr.isMergeable) ~
      ("conflictsWith" -> pr.conflictsWith.map(_.number))
  }
))
