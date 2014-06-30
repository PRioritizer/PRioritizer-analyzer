#!/bin/sh

JAR="target/scala-2.11/analyzer-assembly-1.0.jar"

OWNER=$1
REPOSITORY=$2
GIT_LOCATION=$3

if ! [ -f "$JAR" ]; then
  echo "JAR file does not exist." >&2
  echo "Make sure you run \`sbt assembly\` to build the JAR executable." >&2
  exit 1
fi

if [ "$#" -ne 3 ]; then
  echo "Wrong number of arguments, expected 3 arguments." >&2
  echo "Usage: $0 owner_name repository_name git_location" >&2
  exit 1
fi

case "$OWNER$REPOSITORY$GIT_LOCATION" in
  *\ * )
  echo "Whitespace is not supported." >&2
  exit 1
  ;;
esac

if ! [ -d "$GIT_LOCATION" ]; then
  echo "Git repository does not exist." >&2
  exit 1
fi

JAVA_OPTS="-Dfile.encoding=UTF8 -Dgithub.Owner=$OWNER -Dgithub.Repository=$REPOSITORY -Djgit.Directory=$GIT_LOCATION"
java $JAVA_OPTS -jar $JAR
