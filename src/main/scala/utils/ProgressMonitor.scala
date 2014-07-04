package utils

import scala.concurrent.ExecutionContext.Implicits.global
import org.slf4j.LoggerFactory
import scala.concurrent.Future

class ProgressMonitor(intervalType: String = "percentage", intervalValue: Int = 10) {
  private val logger = LoggerFactory.getLogger("Progress")

  private var total = 0L
  private var current = 0L
  private var lastInterval = 0L

  def reset(): Unit = this.synchronized {
    total = 0L
    current = 0L
    lastInterval = 0L
  }

  def setTotal(value: Long): Unit = this.synchronized {
    total = value
  }

  def reportProgress(value: Long): Unit = this.synchronized {
    current = value
    report(isDone)
  }

  def increment(): Unit = this.synchronized {
    current += 1
    report(isDone)
  }

  def incrementWhen(futures: List[Future[AnyRef]]): Unit = this.synchronized {
    futures.foreach { f => incrementWhen(f) }
  }

  def incrementWhen(future: Future[AnyRef]): Unit = this.synchronized {
    future.onComplete { case _ => increment() }
  }

  def isDone: Boolean = current >= total

  private def report(force: Boolean = false): Unit = {
    // Restrict frequency
    if (!force && !mayUpdate)
      return

    if (total > 0)
      logger info f"$percentage%% ($current/$total)"
    else
      logger info s"$current"

    lastInterval = currentInterval
  }

  private def percentage = (current*100/total).toInt

  private def currentInterval: Long = intervalType match {
    case "time" => System.currentTimeMillis
    case "percentage" => percentage
    case _ => 0L
  }

  private def mayUpdate: Boolean = intervalType match {
    case "time" => System.currentTimeMillis - lastInterval >= intervalValue
    case "percentage" => percentage - lastInterval >= intervalValue
    case "absolute" => current % intervalValue == 0
    case _ => false
  }
}
