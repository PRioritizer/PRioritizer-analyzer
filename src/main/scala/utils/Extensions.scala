package utils

import java.io.{ByteArrayOutputStream, PrintStream}

import scala.util.{Success, Try}

object Extensions {
  implicit class EnrichString(str: String) {

    def safeFileName: String =
      str.replaceAll("[\\\\/:*?\"<>|]+", "-").trim(List(' ', '-'))

    def trim(chars: List[Char]): String =
      str.dropWhile(c => chars.contains(c)).reverse.dropWhile(c => chars.contains(c)).reverse

    def toOptionInt: Option[Int] = Try(str.toInt).toOption

    def toOptionLong: Option[Long] = Try(str.toLong).toOption
  }

  implicit class EnrichException(ex: Throwable) {
    def stackTraceToString: String = {
      val output = new ByteArrayOutputStream()
      val stream = new PrintStream(output)
      ex.printStackTrace(stream)
      output.toString("UTF-8").trim
    }
  }
}
