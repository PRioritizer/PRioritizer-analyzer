import sbt._

object AnalyzerBuild extends Build {
  lazy val root = Project(id = "analyzer", base = file(".")) dependsOn dispatchGitHubProject
  lazy val dispatchGitHubProject = GitHubDependency("erikvdv1", "dispatch-github", "0.1-SNAPSHOT").toRootProject
}

case class GitHubDependency(owner: String, repository: String, ref: String = "master") {
  private val host = "github.com"
  private def location = s"$host/$owner/$repository.git#$ref"
  def toHttpsUri = uri(s"https://$location")
  def toGitUri = uri(s"git://$location")
  def toRootProject = RootProject(toGitUri)
}
