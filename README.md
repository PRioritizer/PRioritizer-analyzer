PRioritizer analyzer
====================

[![Build Status](https://travis-ci.org/PRioritizer/PRioritizer-analyzer.svg)](https://travis-ci.org/PRioritizer/PRioritizer-analyzer)

A pull request prioritizer written in Scala.

The analyzer is written for the [GHTorrent](http://ghtorrent.org/) project, however the data collection process is abstracted in a decorator pattern. So, it should not be to hard to implement other data sources.

Prerequisites
-------------

* The [predictor](https://github.com/PRioritizer/PRioritizer-predictor)
* [Scala](http://www.scala-lang.org/) compiler
* [JVM 8](https://java.com/download/)

Building
--------

1. Clone the project into `~/analyzer`
2. Install dependencies and build the project with `sbt compile`
3. Copy `src/main/resources/settings.properties.dist` to `src/main/resources/settings.properties`
4. Configure the application by editing `src/main/resources/settings.properties`
  * e.g. repository provider: `github`
  * e.g. commits provider: `ghtorrent`
  * e.g. requests provider: `github`
  * e.g. output directory: `~/json/`
  * e.g. cache directory: `~/tmp/`
  * e.g. github access token: get your [access token](https://help.github.com/articles/creating-an-access-token-for-command-line-use/).
  * e.g. predictor command: `~/predictor/run $action $owner $repository`
  * e.g. model directory: `~/tmp/`
  * Ignore the other Github and JGit settings
5. Package the project into a `.jar` file with `sbt assembly`

Running
-------

1. Analyze a single repository with `./run [owner] [repo] ~/repos/[owner]/[repo]`
2. A `.json` file is generated which can be [visualized](https://github.com/PRioritizer/PRioritizer-visualizer)

Use the [watcher](https://github.com/PRioritizer/PRioritizer-watcher) to continuously run the analyzer for selected projects.
