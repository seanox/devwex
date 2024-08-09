#!/bin/bash

# Seanox Devwex is started from working directory ./devwex/program.
# Here, following variables are used:
# 
#   CLASSPATH      path of java resources
# 
#   JAVAPATH       path of java runtime environment
# 
#   LIBRARIESPATH  colon separate paths, from which the server invites startup
#                  modules
# 
#   OPTIONS        spaces separated arguments for the java virtual machine, in
#                  format -Dname=value;...
# 
#   SYSTEMPATH     based on standard variable PATH of the OS, this is used of
#                  some CGI application to find systems components
#
# With startup all shell scripts are loaded from path ../runtime.
# Please note, that scripts to extend of runtime environment are relative to
# working directory.

clear

CLASSPATH=
JAVAPATH=
LIBRARIESPATH=
OPTIONS=

# Automatic determination of the Java runtime environment:
# - in the runtime sub-directories ../runtime
# - else if JAVA_HOME is set
# - else Java runtime in the PATH variable

RUNTIME==../runtime

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
    if [ "$JAVAPATH" = "" ]
    then
      [ -f "$directory/bin/java" ] \
          && JAVAPATH="$directory/bin"
      [ -f "$directory/jre/bin/java" ] \
          && JAVAPATH="$directory/jre/bin"
      [ -f "$directory/java" ] \
          && JAVAPATH="$directory"
    fi
  done
fi

if [ "$JAVAPATH" = "" ]
then
  IFS=:
  set -o noglob
  for directory in $PATH""; do
    [ -f "$directory\java" ] \
        && JAVAPATH=$directory
  done
  if [ ! "$JAVA_HOME" = "" ]
  then
    [ -f "$JAVA_HOME/bin/java" ] \
        && JAVAPATH="$JAVA_HOME/bin"
    [ -f "$JAVA_HOME/jre/bin/java" ] \
        && JAVAPATH="$JAVA_HOME/jre/bin"
  fi
fi

if [ ! -f "$JAVAPATH/java" ]
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

LIBRARIESPATH=$(echo "$LIBRARIESPATH" | xargs)
[ ! "$LIBRARIESPATH" = "" ] \
    && LIBRARIESPATH=":$LIBRARIESPATH"
OPTIONS="$OPTIONS -Dlibraries=\"../libraries\$LIBRARIESPATH\""
OPTIONS="$OPTIONS -Dpath=\"\$PATH\""
OPTIONS=$(echo "$OPTIONS" | xargs)

eval '"$JAVAPATH/java" -cp devwex.jar $OPTIONS com.seanox.devwex.Service $1'
