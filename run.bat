@echo off
SETLOCAL

SET JAR=target\scala-2.11\analyzer-assembly-1.0.jar
FOR %%i in (%*) DO SET /A _argcActual+=1

SET OWNER=%1
SET REPOSITORY=%2
SET GIT_LOCATION=%3

IF NOT EXIST %JAR% GOTO JarNotFound
IF NOT %_argcActual%==3 GOTO InvalidArgs
IF NOT %OWNER: =%==%OWNER% GOTO Whitespace
IF NOT %REPOSITORY: =%==%REPOSITORY% GOTO Whitespace
IF NOT %GIT_LOCATION: =%==%GIT_LOCATION% GOTO Whitespace
IF NOT EXIST %GIT_LOCATION% GOTO DirNotFound
GOTO ValidArgs

:JarNotFound
ECHO JAR file does not exist.
ECHO Make sure you run `sbt assembly` to build the JAR executable.
GOTO end

:InvalidArgs
ECHO Wrong number of arguments, expected 3 arguments.
ECHO Usage: %0 owner_name repository_name git_location
GOTO end

:DirNotFound
ECHO Git repository does not exist.
GOTO end

:Whitespace
ECHO Whitespace is not supported.
GOTO end

:ValidArgs
SET PROPS=-Dfile.encoding=UTF8 -Dgithub.owner=%OWNER% -Dgithub.repository=%REPOSITORY% -Djgit.directory=%GIT_LOCATION%
java %PROPS% -jar %JAR%
:end

ENDLOCAL
