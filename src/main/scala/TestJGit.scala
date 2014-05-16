import dispatch.github.{GitHub, GhPullRequest}
import github.GitHubExtensions
import GitHubExtensions._
import git.{InfoGetter, Provider, MergeTester, PullRequest}
import jgit.JGitProvider
import jgit.merge.JGitMergerTester
import org.slf4j.LoggerFactory
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import utils.Stopwatch

object TestJGit extends App {
  val timer = new Stopwatch
  val logger = LoggerFactory.getLogger("Application")
  val inMemoryMerge = true

  // Read settings
  val token = Settings.token
  val remote = Settings.remote
  val workingDir = Settings.dir

  // Setup Git
  logger info s"Reading repository..."
  timer.start()
  val provider: Provider = new JGitProvider(workingDir, remote, inMemoryMerge)
  val git: MergeTester = provider.merger
  val info: InfoGetter = provider.info
  timer.log()

  // Get pull requests
  logger info s"Fetching pull request meta data..."
  if (!info.hasGitHubInfo) {
    logger error s"Remote '$remote' is not a GitHub remote"
    sys.exit()
  }
  GitHub.accessToken = token
  val req = GhPullRequest.get_pull_requests(info.gitHubInfo.owner, info.gitHubInfo.repository)
  val pullRequests = Await.result(req, Duration.Inf) map (_.asPullRequest)
  logger info s"Got ${pullRequests.length} open pull requests"

  // Fetch pull requests
  logger info s"Fetching pull requests..."
  timer.start()
  git.fetch()
  timer.log()

  // Simulate merge to check for conflicts in PRs
  logger info s"Check for conflicts in PRs (${pullRequests.length})"
  timer.start()
  for {
    pr <- pullRequests // for each PR
    m = git merge pr   // merge the PR into base
    if !m              // keep only conflicted PRs
  } logger error s"CONFLICT: cannot merge $pr"
  timer.log()

  // Simulate merge to check for conflicts between two PRs
  val pairs = PullRequest.getPairs(pullRequests) // get pairs
  logger info s"Check for conflicts among PRs (${pairs.size})"
  timer.start()
  for {
    (pr1, pr2) <- pairs
    m = git merge (pr1, pr2) // merge the two PRs into each other
    if !m                    // keep only conflicted PRs
  } logger error s"CONFLICT: cannot merge $pr1 into $pr2"
  timer.log()

  // Clean pull request refs
  logger info s"Clean up..."
  timer.start()
  git.clean(force = true)
  GitHub.shutdown()
  timer.log()
}
