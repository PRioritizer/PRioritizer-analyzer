import dispatch.github.GitHub
import git.{PullRequestProvider, Provider, MergeProvider, PullRequest}
import jgit.JGitProvider
import github.GitHubProvider
import org.slf4j.LoggerFactory
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import utils.Stopwatch

object TestJGit extends App {
  val timer = new Stopwatch
  val logger = LoggerFactory.getLogger("Application")
  val inMemoryMerge = true

  // Read settings
  val owner = Settings.get("github.Owner").orNull
  val repository = Settings.get("github.Repository").orNull
  val token = Settings.get("github.PersonalAccessToken").orNull
  val workingDir = Settings.get("jgit.Directory").orNull

  // Setup providers
  logger info s"Reading repository..."
  timer.start()
  val gitHub: Provider = new GitHubProvider(owner, repository, token)
  val jGit: Provider = new JGitProvider(workingDir)
  val git: MergeProvider = jGit.merger
  val prProvider: PullRequestProvider = gitHub.pullRequests
  timer.log()

  // Get pull requests
  logger info s"Fetching pull request meta data..."
  val pullRequests = Await.result(prProvider.get, Duration.Inf)
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
