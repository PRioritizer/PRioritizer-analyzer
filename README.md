PRioritizer analyzer
====================

[![Build Status](https://magnum.travis-ci.com/erikvdv1/PRioritizer-analyzer.svg?token=wgtEsFC7Tpxoy7U9Y5p1&branch=master)](https://magnum.travis-ci.com/erikvdv1/PRioritizer-analyzer)

A pull request prioritizer written in Scala.

Follow the steps below to prioritize your pull requests.

1. Clone the project
2. Copy the distributed config file `cp src/main/resources/client.properties.dist src/main/resources/client.properties`
3. Edit the config file to your needs
4. Build the project with `sbt assembly`
5. Run the project with `./run <owner> <repo> <local-git-dir>`
6. A `.json` file is generated which can be [visualized](https://github.com/erikvdv1/PRioritizer-visualizer)

The analyzer is written for the [GHTorrent](http://ghtorrent.org/) project, however the data collection process is abstracted in a decorator pattern. So, it should not be to hard to implement other data sources.

Machine learning (optional)
---------------------------
To predict which pull requests require more attention (high priority) the [predictor](https://github.com/erikvdv1/PRioritizer-predictor) can be used as extra decorator.
