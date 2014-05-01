import git.PullRequest
import merge.MergeTester
import utils.Stopwatch

object TestJGit extends App {
  val timer = new Stopwatch

  // Setup Git
  println(s"Reading repository...")
  timer.start()
  val workingDir = "C:\\Users\\Erik\\git\\potential-octo-dubstep"
  val git: MergeTester = new merge.jgit.JGitMerger(workingDir)
  val pullRequests = PullRequest.get
  timer.print()

  // Fetch pull requests
  println(s"Fetching pull requests...")
  timer.start()
  git.fetch()
  timer.print()

  // Simulate merge to check for conflicts in PRs
  println(s"Check for conflicts in PRs")
  timer.start()
  for {
    pr <- pullRequests // for each PR
    m = git merge pr   // merge the PR into base
    if !m              // keep only conflicted PRs
  } println(s"CONFLICT: cannot merge $pr")
  timer.print()

  // Simulate merge to check for conflicts between two PRs
  println(s"Check for conflicts among PRs")
  timer.start()
  for {
    (pr1, pr2) <- PullRequest.getPairs(pullRequests)
    m = git merge (pr1, pr2) // merge the two PRs into each other
    if !m                    // keep only conflicted PRs
  } println(s"CONFLICT: cannot merge $pr1 into $pr2")
  timer.print()

  // Clean pull request refs
  println(s"Clean up...")
  timer.start()
  git.clean()
  timer.print()
}
