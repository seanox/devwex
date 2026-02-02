#!/bin/bash

# Seanox Devwex is started from working directory ./devwex/program.
# Here, following variables are used:
# 
#   OPTIONS  spaces separated arguments for the java virtual machine, in format
#            -Dname=value ...
#
# With startup all shell scripts are loaded from path ../runtime.
# Please note, that scripts to extend of runtime environment are relative to
# working directory.

clear

OPTIONS=
LIBRARIES=../libraries
RUNTIME=../runtime

# Automatic determination of the Java runtime environment:
# - in the runtime sub-directories ../runtime
# - else if JAVA_HOME is set
# - else Java runtime in the PATH variable

if [ -d $RUNTIME ]
then
  workspace=$(pwd)
  [ "$workspace" = "/" ] && workspace=""
  for directory in $(find $RUNTIME -maxdepth 1 -type d -not -name '..')
  do
    directory="$workspace/${directory#*/}"
    [ -d "$directory/bin" ] \
        && PATH="$directory\bin:$PATH"
    [ -f "$directory/jre/bin/java" ] \
        && PATH="$directory/jre/bin:$PATH"
    if [ "$JAVA" = "" ]
    then
      [ -f "$directory/bin/java" ] \
          && JAVA="$directory/bin"
      [ -f "$directory/jre/bin/java" ] \
          && JAVA="$directory/jre/bin"
      [ -f "$directory/java" ] \
          && JAVA="$directory"
    fi
  done
fi

if [ "$JAVA" = "" ]
then
  IFS=:
  set -o noglob
  for directory in $PATH""; do
    [ -f "$directory\java" ] \
        && JAVA=$directory
  done
  if [ ! "$JAVA_HOME" = "" ]
  then
    [ -f "$JAVA_HOME/bin/java" ] \
        && JAVA="$JAVA_HOME/bin"
    [ -f "$JAVA_HOME/jre/bin/java" ] \
        && JAVA="$JAVA_HOME/jre/bin"
  fi
fi

if [ ! -f "$JAVA/java" ]
then
  echo Seanox Devwex Service [0.0.0 00000000]
  echo Copyright \(C\) 0000 Seanox Software Solutions
  echo Experimental Server Engine
  echo
  echo ERROR: Java Runtime Environment not found
  exit 0
fi

if [ -d $RUNTIME ]
then
  for file in $(find $RUNTIME -maxdepth 1 -type f -name '*.sh')
  do
    echo source $RUNTIME/$file
  done
fi

OPTIONS="-Dlibraries=\"$LIBRARIES\" $OPTIONS"

OPTIONS="-cp devwex.jar $OPTIONS com.seanox.devwex.Service $1 $2"

readarray -t -d '' arguments < <(xargs printf '%s\0' <<< "$OPTIONS")
"$JAVA/java" "${arguments[@]}"
