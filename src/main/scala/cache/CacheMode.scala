package cache

/**
 * An enum type for merge results.
 */
object CacheMode extends Enumeration {
  type CacheMode = Value
  val None, Read, Write = Value
}
