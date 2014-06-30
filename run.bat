@echo off

set OWNER=%1
set REPOSITORY=%2
set GIT_LOCATION=%3

IF "%3"=="" GOTO InvalidArgs
IF NOT "%4"=="" GOTO InvalidArgs
IF NOT %OWNER: =%==%OWNER% GOTO Whitespace
IF NOT %REPOSITORY: =%==%REPOSITORY% GOTO Whitespace
IF NOT %GIT_LOCATION: =%==%GIT_LOCATION% GOTO Whitespace
IF NOT EXIST %GIT_LOCATION% GOTO DirNotFound
GOTO ValidArgs

:InvalidArgs
echo Wrong number of arguments, expected 3 arguments.
echo Usage: %0 owner_name repository_name git_location
goto end

:DirNotFound
echo Git repository does not exist.
goto end

:Whitespace
echo Whitespace is not supported.
goto end

:ValidArgs
set JAVA_OPTS=-Dfile.encoding=UTF8 -Dgithub.Owner="%OWNER%" -Dgithub.Repository="%REPOSITORY%" -Djgit.Directory="%GIT_LOCATION%"
sbt run
:end
