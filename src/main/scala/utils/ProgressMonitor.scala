package utils

import org.slf4j.LoggerFactory

class ProgressMonitor {
  var total = 0L
  private var current = 0L

  val interval = 1000 // milliseconds

  private val logger = LoggerFactory.getLogger(this.getClass)
  private var lastTime = 0L

  def reportProgress(value: Long): Unit = {
    this.synchronized {
      current = value
      report(force = total == current)
    }
  }

  def increment(): Unit = {
    this.synchronized {
      current = current + 1
      report(force = total == current)
    }
  }

  private def report(force: Boolean = false): Unit = {
    // Restrict frequency
    if (!force && !mayUpdate)
      return

    if (total > 0)
      logger info f"${current*100/total}%3d%% ($current/$total)"
    else
      logger info s"$current"

    lastTime = System.currentTimeMillis
  }

  private def mayUpdate: Boolean = {
    lastTime + interval < System.currentTimeMillis
  }
}
