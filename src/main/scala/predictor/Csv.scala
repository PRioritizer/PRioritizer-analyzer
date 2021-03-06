package predictor

import java.io.File

import git.PullRequest
import utils.Extensions._

object Csv {

  private val escapeChars = Map("\"" -> "\"\"", "\r" -> "\\r", "\n" -> "\\n")
  private val nf = java.text.NumberFormat.getInstance(java.util.Locale.ROOT)
  nf.setMaximumFractionDigits(6)
  nf.setGroupingUsed(false)

  def write(file: String, data: List[PullRequest]): Unit = write(new File(file), data)

  def write(file: File, data: List[PullRequest]): Unit = {

    val header = List(
      "age",
      "title",
      "target",
      "author",
      "coreMember",
      "intraBranch",
      "containsFix",
      "commitRatio",
      "pullRequestRatio",
      "comments",
      "reviewComments",
      "lastCommentMention",
      "additions",
      "deletions",
      "commits",
      "files",
      "hasTestCode",
      "important")

    val rows = for {
      pr <- data
    } yield List(
        pr.age,
        pr.title.getOrElse(""),
        pr.target,
        pr.author,
        pr.coreMember.getOrElse(false),
        pr.intraBranch.getOrElse(false),
        pr.containsFix.getOrElse(false),
        pr.contributedCommitRatio.getOrElse(0D),
        pr.pullRequestAcceptRatio.getOrElse(0D),
        pr.comments.getOrElse(0L),
        pr.reviewComments.getOrElse(0L),
        pr.lastCommentMention.getOrElse(false),
        pr.linesAdded.getOrElse(0L),
        pr.linesDeleted.getOrElse(0L),
        pr.commits.getOrElse(0L),
        pr.filesChanged.getOrElse(0L),
        pr.hasTestCode.getOrElse(false),
        false)

    val contents = header :: rows
    writeData(file, contents)
  }

  def readAsBoolean(file: File): List[List[Boolean]] = read(file).map(r => r.map(f => f.toBoolean))

  def readAsDouble(file: File): List[List[Double]] = read(file).map(r => r.map(f => f.toDouble))

  def read(file: String): List[List[String]] = read(new File(file))

  def read(file: File): List[List[String]] = {
    val data = scala.io.Source.fromFile(file).mkString
    val rows = data.split("\n").map(r => r.trim).toList
    rows.map(r => r.split(",").map(f => f.trim.trim(List('"'))).toList)
  }

  def writeData(file: File, data: List[List[Any]]): Unit = {
    val contents = data.map(row => row.map(v => format(v)).mkString(",")).mkString("\n") + "\n"
    writeToFile(file, contents)
  }

  private def writeToFile(file: File, contents: String): Unit = {
    val dir: File = file.getParentFile
    dir.mkdirs()
    val writer = new java.io.PrintWriter(file)
    try writer.write(contents) finally writer.close()
  }

  private def format(value: Any): String = value match {
    case s: String => s""""${escape(s)}""""
    case true => "1"
    case false => "0"
    case u: Unit => ""
    case b: Byte => nf.format(b)
    case c: Char => nf.format(c)
    case s: Short => nf.format(s)
    case i: Int => nf.format(i)
    case l: Long => nf.format(l)
    case f: Float => if (f.isNaN || f.isInfinity) "0" else nf.format(f)
    case d: Double => if (d.isNaN || d.isInfinity) "0" else nf.format(d)
    case _ => s""""$value""""
  }

  private def escape(value: String): String = {
    escapeChars.foldLeft(value)((s,c) => s.replace(c._1, c._2))
  }
}
