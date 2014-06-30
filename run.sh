#!/bin/sh

if [ "$#" -ne 3 ]; then
  echo "Wrong number of arguments, expected 3 arguments." >&2
  echo "Usage: $0 owner_name repository_name git_location" >&2
  exit 1
fi

if ! [ -d "$3" ]; then
  echo "Git repository does not exist." >&2
  echo "Usage: $0 owner_name repository_name git_location" >&2
  exit 1
fi

OWNER=$1
REPOSITORY=$2
GIT_LOCATION=$3

JAVA_OPTS="-Dfile.encoding=UTF8 -Dgithub.Owner=\"$OWNER\" -Dgithub.Repository=\"$REPOSITORY\" -Djgit.Directory=\"$GIT_LOCATION\""
sbt run $JAVA_OPTS
