package utils

import java.io.{PrintStream, ByteArrayOutputStream}

object Extensions {
  implicit class EnrichString(str: String) {

    def safeFileName: String =
      str.replaceAll("[\\\\/:*?\"<>|]+", "-").trim(List(' ', '-'))

    def trim(chars: List[Char]): String =
      str.dropWhile(c => chars.contains(c)).reverse.dropWhile(c => chars.contains(c)).reverse
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
