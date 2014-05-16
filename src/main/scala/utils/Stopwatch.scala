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
  private var stopTime = -1L
  private var running = false

  def start(): Stopwatch = {
    startTime = System.currentTimeMillis()
    running = true
    this
  }

  def stop(): Stopwatch = {
    stopTime = System.currentTimeMillis()
    running = false
    this
  }

  def reset(): Stopwatch = {
    startTime = -1
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

  def print() = println(s"${getElapsedTime}ms")

  def log() = logger info s"${getElapsedTime}ms"
}
