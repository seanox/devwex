  @echo off

  rem Seanox Devwex is started from working directory ./devwex/program.
  rem Following variables are used:
  rem
  rem     name  Service name (spaces/special characters are not allowed)
  rem
  rem     text  Service display name (maximum 1024 characters)
  rem
  rem     note  Service description (maximum 1024 characters)
  rem
  rem     jvms  Initial memory pool size in MB
  rem
  rem     jvmx  Maximum memory pool size in MB
  rem
  rem     home  Working path (optional)
  rem
  rem     java  Java environment path (optional)
  rem
  rem     jdwp  Options for remote debugging (optional)
  rem
  rem Link(s) to used prunsrv.exe (alias service-32.exe/service-64.exe):
  rem     https://commons.apache.org/daemon/procrun.html
  rem     https://commons.apache.org/proper/commons-daemon/procrun.html
  rem
  rem NOTE - If environment variables "home" and "java" empty or not defined,
  rem this will be resolved automatically. The declaration of both values is
  rem optional.

  echo Seanox Devwex Service 0.0.0 00000000
  echo Copyright (C) 0000 Seanox Software Solutions
  echo Experimental Server Engine
  echo.

  cd /D "%~dp0"

  SetLocal EnableDelayedExpansion

  rem -- CONFIGURATION ---------------------------------------------------------

  set name=Devwex
  set text=Seanox Devwex
  set note=Seanox Experimental Server Engine

  set home=%cd%
  set java=

  rem set home=C:\Program Files\Devwex\program
  rem set java=C:\Program Files\Java
  rem set jdwp=dt_socket,server=y,suspend=n,address=8000
  rem set jvms=256
  rem set jvmx=512

  set ServiceHome=%home%
  set ServiceName=%name%
  set DisplayName=%text%
  set Description=%note%
  set Startup=auto
  set ServiceAccount=NetworkService

  set Jvm=%jvm%
  set Classpath=devwex.jar

  set LogPrefix=service
  set LogPath=%home%/../storage
  set StdOutput=%LogPath%/output.log
  set StdError=%LogPath%/error.log

  set StartPath=%ServiceHome%
  set StartMode=jvm
  set StartClass=com.seanox.devwex.Service
  set StartMethod=main
  set StartParams=start

  set StopPath=%ServiceHome%
  set StopMode=jvm
  set StopClass=com.seanox.devwex.Service
  set StopMethod=main
  set StopParams=stop

  rem --------------------------------------------------------------------------
 
  rem Automatic determination of the Java runtime environment:
  rem - in the work directory ..\runtime\java
  rem - else if JAVA_HOME is set
  rem - else Java runtime in the PATH variable

  if "%java%" == "" (
    if exist "%home%\..\runtime\java\bin\java.exe" set java=%home%\..\runtime\java\bin
  )
  if "%java%" == "" (
    if not "%JAVA_HOME%" == "" (
      if exist "%JAVA_HOME%\bin\java.exe" set java=%JAVA_HOME%\bin
    )
  )
  if "%java%" == "" (
    for %%i in ("%PATH:;=";"%") do (
      if exist "%%i\java.exe" set java=%%i
    )
  )

  if not exist "%JAVAPATH%\java.exe" (
    echo ERROR: Java Runtime Environment not found
    goto :EOF
  )

  if "%1" == "install"   goto install
  if "%1" == "update"    goto install
  if "%1" == "uninstall" goto uninstall
 
  if "%1" == "start"   goto start
  if "%1" == "restart" goto restart
  if "%1" == "stop"    goto stop
  if "%1" == "status"  goto status

  echo usage: %~nx0 [command]
  echo.
  echo    install
  echo    update
  echo    uninstall
  echo.
  echo    start
  echo    restart
  echo    stop
  
  net session >nul 2>&1
  if not %errorLevel% == 0 (
    echo.
    echo This script must run as Administrator.
  )
  goto exit



:install

  set label=INSTALL
  if "%1" == "update" set label=UPDATE

  echo %label%: Detection of Java runtime environment
  set jvm=
  if exist "%java%\bin\client\jvm.dll" set jvm=%java%\bin\client\jvm.dll
  if exist "%java%\bin\server\jvm.dll" set jvm=%java%\bin\server\jvm.dll
  if exist "%java%\jre\bin\client\jvm.dll" set jvm=%java%\jre\bin\client\jvm.dll
  if exist "%java%\jre\bin\server\jvm.dll" set jvm=%java%\jre\bin\server\jvm.dll
  if not exist "%jvm%" (
    echo.
    echo ERROR: Java Runtime Environment not found
    goto exit
  )
  for %%i in ("%jvm%") do echo    %%~fi

  echo %label%: Detection of service runner
  set service=service-32.exe
  if exist "%PROCESSOR_ARCHITECTURE:~-2,2%" == "64" (
      set service=service-64.exe
  )
  if not exist "%home%\%service%" (
    echo.
    echo ERROR: Service runner ^(%service%^) not found
    goto exit
  )
  for %%i in ("%home%\%service%") do echo    %%~fi

rem ----------------------------------------------------------------------------

  set lastError=
  set lastError=%errorLevel%

  rem Full access for the NetworkService another user to the AppDirectory
  rem Here no directory of a user should be used, because it is not clear
  rem whether the directory is accessible without the login.
  echo %label%: Grant all privileges for %ServiceAccount% to the app AppDirectory
  for %%i in ("%home%\..") do echo    %%~fi
  icacls.exe "%home%\.." /grant %ServiceAccount%:(OI)(CI)F /T /Q >%~n0.log 2>&1
  if not "%lastError%" == "%errorLevel%" goto error

  sc query %ServiceName% >nul 2>&1
  if "%errorLevel%" == "%lastError%" (
    echo %label%: Service is still present and will be stopped and removed
    %service% //DS//%ServiceName%
  )

  set ServiceLibrariesPath=
  set SystemDrive=%SystemDrive%
  set SystemRoot=%SystemRoot%
  set SystemPath=%path%

  set extensions=false
  echo %label%: Search for runtime extensions
  for %%i in (../runtime/*.bat ../runtime/*.cmd) do (
    set extensions=true
    echo    %%~fi
    call ../runtime/%%i
  )
  if "%extensions%" == "false" echo    nothing found

rem ----------------------------------------------------------------------------

  set init=--DisplayName        "%DisplayName%"
  set init=%init% --Description "%Description%"
  set init=%init% --Startup     "%Startup%"
  set init=%init% --Install     "%home%\%service%"

  set init=%init% --Jvm         "%Jvm%"
  set init=%init% --Classpath   "%Classpath%"

  set init=%init% --LogPath     "%LogPath%"
  set init=%init% --LogPrefix   "%LogPrefix%"
  set init=%init% --StdOutput   "%StdOutput%"
  set init=%init% --StdError    "%StdError%"

  set init=%init% --StartPath   "%StartPath%"
  set init=%init% --StartMode   "%StartMode%"
  set init=%init% --StartClass  "%StartClass%"
  set init=%init% --StartMethod "%StartMethod%"
  set init=%init% --StartParams "%StartParams%"

  set init=%init% --StopPath    "%StopPath%"
  set init=%init% --StopMode    "%StopMode%"
  set init=%init% --StopClass   "%StopClass%"
  set init=%init% --StopMethod  "%StopMethod%"
  set init=%init% --StopParams  "%StopParams%"

  if not "%jvms%" == "" set init=%init% --JvmMs=%jvms%
  if not "%jvmx%" == "" set init=%init% --JvmMx=%jvmx%
  
  echo %label%: Service will be created
  %service% //IS//%ServiceName% %init%

  sc config %ServiceName% obj= "NT Authority\%ServiceAccount%" >%~n0.log 2>&1
  if not "%errorLevel%" == "%lastError%" goto error

  echo %label%: Service will be final configured
  %service% //US/%ServiceName% ++JvmOptions='-Dpath="%SystemPath%;"'
  %service% //US/%ServiceName% ++JvmOptions='-Dsystemdrive=%SystemDrive%'
  %service% //US/%ServiceName% ++JvmOptions='-Dsystemroot=%SystemRoot%'
  %service% //US/%ServiceName% ++JvmOptions='-Dlibraries="..\libraries;%ServiceLibrariesPath%;"'
  %service% //US/%ServiceName% ++JvmOptions='-Dlibraries=true

  if not "%jdwp%" == "" (
    %service% //US/%ServiceName% ++JvmOptions='-Xdebug'
    %service% //US/%ServiceName% ++JvmOptions='-Xnoagent'
    %service% //US/%ServiceName% ++JvmOptions='-Djava.compiler=NONE'
    %service% //US/%ServiceName% ++JvmOptions='-Xrunjdwp:transport="%jdwp%"'
  )

  echo %label%: Successfully completed
  goto exit



:uninstall

  set label=UNINSTALL

  echo %label%: Detection of service runner
  set service=service-32.exe
  if exist "%PROCESSOR_ARCHITECTURE:~-2,2%" == "64" (
      set service=service-64.exe
  )
  if not exist "%home%\%service%" (
    echo.
    echo ERROR: Service runner ^(%service%^) not found
    goto exit
  )
  for %%i in ("%home%\%service%") do echo    %%~fi

  sc query %ServiceName% >nul 2>&1
  if "%errorLevel%" == "0" (
    echo %label%: Service is still present and will be stopped and removed
    %service% //DS//%ServiceName%
  ) else echo %label%: Service has already been removed

  echo %label%: Successfully completed
  goto exit
 
 
 
:start

  sc query %ServiceName% >nul 2>&1
  if not "%errorLevel%" == "0" (
    echo ERROR: Service is not present
    goto exit
  )
  net start %ServiceName%
  goto exit



:restart

  sc query %ServiceName% >nul 2>&1
  if not "%errorLevel%" == "0" (
    echo ERROR: Service is not present
    goto exit
  )
  net stop %ServiceName%
  net start %ServiceName%
  goto exit



:stop
 
  sc query %ServiceName% >nul 2>&1
  if not "%errorLevel%" == "0" (
    echo ERROR: Service is not present
    goto exit
  )
  net stop %ServiceName%
  goto exit
 


:status

  sc query %ServiceName% >nul 2>&1
  if not "%errorLevel%" == "0" (
    echo ERROR: Service is not present
    goto exit
  )
  if not exist "%java%\bin\java.exe" (
    echo ERROR: Java Runtime Environment not found
  ) else (
    "%java%\bin\java.exe" -cp "%Classpath%" com.seanox.devwex.Service status
  )
  goto exit

  
  
:error

  echo.
  echo ERROR: An unexpected error occurred.
  echo ERROR: The script was canceled.

  if not exist %~n0.log goto exit

  echo.
  type %~n0.log
  goto exit



:exit

  if exist %~n0.log del %~n0.log
  exit /B 0
