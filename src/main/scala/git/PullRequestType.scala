package git

/**
 * An enum type for severity levels.
 */
object PullRequestType extends Enumeration {
  type PullRequestType = Value
  val Fix, Refactor, Feature, Documentation, Unknown = Value

  def parse(value: String): PullRequestType = {
    val words = value.toLowerCase
    if(words.contains("fix") || words.contains("bug") || words.contains("secur"))
      PullRequestType.Fix
    else if(words.contains("refactor") || words.contains("chang"))
      PullRequestType.Refactor
    else if(words.contains("doc") || words.contains("comment"))
      PullRequestType.Documentation
    else
      PullRequestType.Feature
  }
}
