@echo off

rem Seanox Devwex is started from working directory .\devwex\program.
rem Here, following variables are used:
rem
rem   OPTIONS  spaces separated arguments for the java virtual machine, in
rem            format -Dname=value ...
rem
rem With startup all batch scripts are loaded from path ..\runtime.
rem Please note, that scripts to extend of runtime environment are relative to
rem working directory.

cls

SETLOCAL

set OPTIONS=
set LIBRARIES=..\libraries
set RUNTIME=..\runtime

rem Automatic determination of the Java runtime environment:
rem - in the runtime sub-directories ..\runtime
rem - else if JAVA_HOME is set
rem - else Java runtime in the PATH variable

SetLocal EnableDelayedExpansion

set JAVA=
if exist "%RUNTIME%" (
  for /f "delims=: " %%d in ('dir /AD /B %RUNTIME%') do (
    set DIRECTORY=%cd%\%RUNTIME%\%%d
    set DIRECTORY=!DIRECTORY:\\=\!
    set PATH=!DIRECTORY!;!PATH!
    if exist "!DIRECTORY!\bin"^
        set PATH=!DIRECTORY!\bin;!PATH!
    if exist "!DIRECTORY!\jre\bin\java.exe"^
        set PATH=!DIRECTORY!\jre\bin;!PATH!
    if "!JAVA!" == "" (
      if exist "!DIRECTORY!\bin\java.exe"^
          set JAVA=!DIRECTORY!\bin
      if exist "!DIRECTORY!\jre\bin\java.exe"^
          set JAVA=!DIRECTORY!\jre\bin
      if exist "!DIRECTORY!\java.exe"^
          set JAVA=!DIRECTORY!
    )
  )
)

for %%d in ("%PATH:;=";"%") do (
  if exist "%%d\java.exe"^
      set JAVA=%%d
)
if not "%JAVA_HOME%" == "" (
  if exist "%JAVA_HOME%\bin\java.exe"^
      set JAVA=%JAVA_HOME%\bin
  if exist "%JAVA_HOME%\jre\bin\java.exe"^
      set JAVA=%JAVA_HOME%\jre\bin
)

if not exist "%JAVA%\java.exe" (
  echo Seanox Devwex Service [0.0.0 00000000]
  echo Copyright ^(C^) 0000 Seanox Software Solutions
  echo Experimental Server Engine
  echo.
  echo ERROR: Java Runtime Environment not found
  goto :EOF
)

for %%f in (%RUNTIME%\*.bat %RUNTIME%\*.cmd) do call %RUNTIME%\%%f

set OPTIONS=%OPTIONS% -Dlibraries=%LIBRARIES%

"%JAVA%\java.exe" -cp devwex.jar %OPTIONS% com.seanox.devwex.Service %1

ENDLOCAL
