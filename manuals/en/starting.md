[Configuration](configuration.md) | [TOC](README.md) | [Controlling and Monitoring](control.md)
- - -

# Starting and Stopping
  
Seanox Devwex is a Java console application. After opening the console
(shell/prompt) it changes to the working directory `./devwex/program` and calls
Seanox Devwex with the command to be executed as Java binary or via the
available startup scripts. Upper and lower case can be ignored for the commands. 

> Java binary
>
> ```
> java -cp devwex.jar com.seanox.devwex.Service <command>

> Batch-Skript in Windows
>
> ```
> devwex.cmd <command>
> ```

> Shell-Skript in Unix/Linux/MacOS
> 
> ```
> devwex.sh <command>
> ```

When the application is running, the server control can also be called up via
Telnet. 

> Telnet
> 
> ```
> telnet 127.0.0.1 25000
> <command>
> ```


## Contents Overview
- [Start](#start)
- [Status](#status)
- [Restart](#restart)
- [Stop](#stop)


## Start

Starts Seanox Devwex according to the current configuration.

With the start, further VM arguments can be passed as properties or environment
variables. Properties are set in format `-Dproperty=value` directly on Java
call. Environment variables are set in the operating system before the program
is called. In both cases, the VM arguments and the environment variables can
then be used as dynamic keys in the configuration file `devwex.ini`. In this
case, properties (VM arguments) have a higher priority than environment
variables.

> Passing VM arguments with the program start
>
> ```
> java -cp devwex.jar -Dargument1=value1 -Dargument2=value2 com.seanox.devwex.Service start
> ```
> 
> With the program start, the VM arguments `argument1` and `argument2` are
> passed, which can then be used in the Java implementation and in the
> configuration. The VM arguments are available as dynamic keys in the
> configuration (see also [Configuration - Configuration File](
>     configuration.md#configuration-file)).

The argument `libraries` is a fixed part of the argument, which can be used to
pass additional resources, libraries and classes whose paths are separated by
the path separator defined for the operating system. At startup, Seanox Devwex
automatically adds all files specified in this way to its class path (ClassPath)
as well as the files of the specified directories.

> Passing VM arguments with the program start
>
> ```
> java -cp devwex.jar -Dlibraries="../libraries" com.seanox.devwex.Service start
> ```
>
> The VM argument `libraries` is passed with the program call at startup. This
> can then be used in the Java implementation and in the configuration. 


## Status

Shows information about the version, start and system time as well as the loaded
modules and servers.


## Restart

Stops Seanox Devwex including all active modules and servers and restarts with
reloaded configuration. If the new configuration contains any malfunctioning
errors, the last used configuration will be reused.


## Stop

Terminates Seanox Devwex including all active modules and servers.
      
      
- - -

[Configuration](configuration.md) | [TOC](README.md) | [Controlling and Monitoring](control.md)
