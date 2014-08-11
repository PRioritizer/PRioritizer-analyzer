package output

import spray.json._

object JsonProtocol extends DefaultJsonProtocol {
  implicit val jodaFormat = JodaJsonFormat
  implicit val pullRequestFormat = PullRequestJsonFormat
}
