package utils

//
// Stopwatch for benchmarking
// http://thelastdegree.wordpress.com/2012/07/11/a-scala-stopmatch-for-benchmarking/
//
class Stopwatch {

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

  def reset() {
    startTime = -1
    stopTime = -1
    running = false
  }

  def isRunning: Boolean = running

  def getElapsedTime = {
    if (startTime == -1) {
      0
    }
    if (running) {
      System.currentTimeMillis() - startTime
    }
    else {
      stopTime - startTime
    }
  }

  def print() = {
    println(s"${getElapsedTime}ms")
  }
}
