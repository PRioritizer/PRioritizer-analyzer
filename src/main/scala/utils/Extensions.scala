package utils

object Extensions {
  implicit class EnrichString(str: String) {

    def safeFileName: String =
      str.replaceAll("[\\\\/:*?\"<>|]+", "-").trim(List(' ', '-'))

    def trim(chars: List[Char]): String =
      str.dropWhile(c => chars.contains(c)).reverse.dropWhile(c => chars.contains(c)).reverse
  }
}