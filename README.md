PRioritizer
==========

A pull request prioritizer written in Scala.

Follow the steps below to prioritize your pull requests.

1. Clone the project
2. Copy the distributed config file `cp src/main/resources/client.properties.dist src/main/resources/client.properties`
3. Edit the config file to your needs
4. Build the project with `sbt assembly`
5. Run the project with `./run <owner> <repo> <local-git-dir>`
6. A `.json` file is generated which can be [visualized](https://github.com/erikvdv1/PRioritizer-visualizer)
