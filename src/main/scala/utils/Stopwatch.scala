package utils

import org.slf4j.LoggerFactory

/**
 * A very simple Stopwatch in Scala for Benchmarking
 * Source: http://thelastdegree.wordpress.com/2012/07/11/a-scala-stopmatch-for-benchmarking/
 * @author the/last/degree
 * @see http://thelastdegree.wordpress.com/2012/07/11/a-scala-stopmatch-for-benchmarking/
 */
class Stopwatch {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private var startTime = -1L
  private var lapTime = -1L
  private var stopTime = -1L
  private var running = false

  def start(): Stopwatch = {
    startTime = System.currentTimeMillis()
    lapTime = startTime
    running = true
    this
  }

  def lap(): Stopwatch = {
    lapTime = System.currentTimeMillis()
    this
  }

  def stop(): Stopwatch = {
    stopTime = System.currentTimeMillis()
    running = false
    this
  }

  def reset(): Stopwatch = {
    startTime = -1
    lapTime = -1
    stopTime = -1
    running = false
    this
  }

  def isRunning: Boolean = running

  def getElapsedTime = {
    if (startTime == -1)
      0L
    else if (running)
      System.currentTimeMillis() - startTime
    else
      stopTime - startTime
  }

  def getLapTime = {
    if (lapTime == -1)
      0L
    else if (running)
      System.currentTimeMillis() - lapTime
    else
      stopTime - lapTime
  }

  def print() = println(toString)

  def log() = logger info toString

  def printLap() = {
    print()
    lap()
  }

  def logLap() = {
    log()
    lap()
  }

  override def toString: String = f"Lap: $getLapTime%5dms, Total: $getElapsedTime%5dms"
}
