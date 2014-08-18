package output

import org.joda.time.{DateTime, DateTimeZone}
import spray.json._

object JodaJsonFormat extends RootJsonFormat[DateTime] {
  private val df = org.joda.time.format.ISODateTimeFormat.dateTime

  private def toUtc(date: DateTime): DateTime =
    date.toDateTime(DateTimeZone.UTC)

  def write(d: DateTime) = JsString(toUtc(d).toString(df))

  def read(value: JsValue) = value match {
    case JsString(string) =>
      new DateTime(string)
    case JsNumber(number) =>
      new DateTime(number * 1000, DateTimeZone.UTC)
    case _ => deserializationError("DateTime expected")
  }
}
