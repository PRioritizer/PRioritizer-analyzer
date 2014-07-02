package utils

import scala.concurrent.ExecutionContext.Implicits.global
import org.slf4j.LoggerFactory
import scala.concurrent.Future

class ProgressMonitor {
  val interval = 1000 // milliseconds

  private val logger = LoggerFactory.getLogger(this.getClass)

  private var total = 0L
  private var current = 0L
  private var lastTime = 0L

  def reset(): Unit = this.synchronized {
    total = 0L
    current = 0L
    lastTime = 0L
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
      logger info f"${current*100/total}%3d%% ($current/$total)"
    else
      logger info s"$current"

    lastTime = System.currentTimeMillis
  }

  private def mayUpdate: Boolean = {
    lastTime + interval < System.currentTimeMillis
  }
}
