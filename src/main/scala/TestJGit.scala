import dispatch.github.{GitHub, GhPullRequest}
import dispatch.github.GitHubExtensions._
import git.PullRequest
import merge.MergeTester
import org.slf4j.LoggerFactory
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import utils.Stopwatch

object TestJGit extends App {
  val timer = new Stopwatch
  val logger = LoggerFactory.getLogger("Application")

  // Read access token
  val token = Settings.token
  val remote = "origin"
  val workingDir = "C:\\Users\\Erik\\git\\potential-octo-dubstep"

  // Setup Git
  logger info s"Reading repository..."
  timer.start()
  val git: MergeTester = new merge.jgit.JGitMerger(workingDir, remote)
  timer.print()

  // Get pull requests
  logger info s"Fetching pull request meta data..."
  var info = git.gitHubInfo
  if (!info.isDefined) {
    logger error s"Remote '$remote' is not a GitHub remote"
    sys.exit()
  }
  GitHub.accessToken = token
  val req = GhPullRequest.get_pull_requests(info.get._1, info.get._2)
  val pullRequests = Await.result(req, Duration.Inf) map (_.asPullRequest)
  logger info s"Got ${pullRequests.length} open pull requests"

  // Fetch pull requests
  logger info s"Fetching pull requests..."
  timer.start()
  git.fetch()
  timer.print()

  // Simulate merge to check for conflicts in PRs
  logger info s"Check for conflicts in PRs"
  timer.start()
  for {
    pr <- pullRequests // for each PR
    m = git merge pr   // merge the PR into base
    if !m              // keep only conflicted PRs
  } logger error s"CONFLICT: cannot merge $pr"
  timer.print()

  // Simulate merge to check for conflicts between two PRs
  logger info s"Check for conflicts among PRs"
  timer.start()
  for {
    (pr1, pr2) <- PullRequest.getPairs(pullRequests) // get pairs
    m = git merge (pr1, pr2) // merge the two PRs into each other
    if !m                    // keep only conflicted PRs
  } logger error s"CONFLICT: cannot merge $pr1 into $pr2"
  timer.print()

  // Clean pull request refs
  logger info s"Clean up..."
  timer.start()
  git.clean(force = true)
  GitHub.shutdown()
  timer.print()
}
