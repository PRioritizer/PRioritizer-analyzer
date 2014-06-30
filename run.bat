@echo off

IF "%3"=="" GOTO InvalidArgs
IF NOT "%4"=="" GOTO InvalidArgs
IF NOT EXIST "%3" GOTO DirNotFound
GOTO ValidArgs

:InvalidArgs
echo Wrong number of arguments, expected 3 arguments.
echo Usage: %0 owner_name repository_name git_location
goto end

:DirNotFound
echo Git repository does not exist.
echo Usage: %0 owner_name repository_name git_location
goto end

:ValidArgs
set OWNER=%1
set REPOSITORY=%2
set GIT_LOCATION=%3

set JAVA_OPTS=-Dfile.encoding=UTF8 -Dgithub.Owner="%OWNER%" -Dgithub.Repository="%REPOSITORY%" -Djgit.Directory="%GIT_LOCATION%"
sbt run
:end
