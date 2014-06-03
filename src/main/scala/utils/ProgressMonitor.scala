package utils

import org.slf4j.LoggerFactory

class ProgressMonitor {
  var total = 0L
  var current = 0L

  private val logger = LoggerFactory.getLogger(this.getClass)

  def reportProgress(value: Long): Unit = {
    current = value
    report()
  }

  def report(): Unit = {
    if (total > 0)
      logger info s"$current/$total (${current*100/total}%)"
    else
      logger info s"$current"
  }

  def increment(): Unit = {
    current = current + 1
    report()
  }
}
