@echo off

rem Seanox Devwex is started from working directory .\devwex\program.
rem Here, following variables are used:
rem
rem   CLASSPATH     path of java resources
rem
rem   JAVAPATH      path of java runtime environment
rem
rem   LIBRARIESPATH semicolon separate paths, from which the server invites
rem                 startup modules
rem
rem   OPTIONS       spaces separated arguments for the java virtual machin,
rem                 in format -Dname=value;...
rem
rem   SYSTEMDRIVE   standard variable SYSTEMDRIVE of Windows, this is used of
rem                 some CGI application to find systems components
rem
rem   SYSTEMPATH    based on standard variable PATH of Windows, this is used of
rem                 some CGI application to find systems components
rem
rem   SYSTEMROOT    standard variable SYSTEMROOT of Windows, this is used of
rem                 some CGI application to find systems components
rem
rem With startup all batch scripts are loaded from path ..\runtime.
rem Please note, that scripts to extend of runtime environment are relative to
rem working directory.

cls

set CLASSPATH=
set JAVAPATH=
set LIBRARIESPATH=
set OPTIONS=
set SYSTEMPATH=%PATH%

rem Automatic determination of the Java runtime environment:
rem - in the work directory ..\runtime\java
rem - else if JAVA_HOME is set
rem - else Java runtime in the PATH variable

SET RUNTIME=..\runtime

SetLocal EnableDelayedExpansion

if "%JAVAPATH%" == "" (
  if not "%JAVA_HOME%" == "" (
    if exist "%JAVA_HOME%\bin\java.exe"^
        set JAVAPATH=%JAVA_HOME%\bin
  )
)
if "%JAVAPATH%" == "" (
  for %%i in ("%PATH:;=";"%") do (
    if exist "%%i\java.exe"^
        set JAVAPATH=%%i
  )
)

for /f "delims=: " %%d in ('dir /AD /B %RUNTIME%') do (
  set DIRECTORY=%cd%\%RUNTIME%\%%d
  set DIRECTORY=!DIRECTORY:\\=\!
  set PATH=!DIRECTORY!;!PATH!
  if exist "!DIRECTORY!\bin"^
      set PATH=!DIRECTORY!\bin;!PATH!
  if "!JAVAPATH!" == "" (
  if exist "!DIRECTORY!\bin\java.exe"^
     if exist "!DIRECTORY!\bin\java.exe"^
         set JAVAPATH=!DIRECTORY!\bin
  )
)

for %%f in (%RUNTIME%\*.bat %RUNTIME%\*.cmd) do call %RUNTIME%\%%f

if not exist "%JAVAPATH%\java.exe" (
  echo Seanox Devwex Service 0.0.0 00000000
  echo Copyright ^(C^) 0000 Seanox Software Solutions
  echo Experimental Server Engine
  echo.
  echo ERROR: Java Runtime Environment not found
  goto :EOF
)

set OPTIONS=%OPTIONS% -Dpath="%SYSTEMPATH%;"
set OPTIONS=%OPTIONS% -Dsystemdrive=%SYSTEMDRIVE%
set OPTIONS=%OPTIONS% -Dsystemroot="%SYSTEMROOT%"
set OPTIONS=%OPTIONS% -Dlibraries="..\libraries;%LIBRARIESPATH%;"

"%JAVAPATH%\java.exe" -cp "devwex.jar;%CLASSPATH%" %OPTIONS% com.seanox.devwex.Service %1
