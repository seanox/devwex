[Configuration](configuration.md) | [TOC](README.md) | [Controlling and Monitoring](control.md)
- - -

# Start
  
Seanox Devwex is a Java console application. After opening the console
(shell/prompt) it changes to the working directory `./devwex/program` and calls
Seanox Devwex with the command to be executed as Java binary or via the
available startup scripts.

Overview of available commands:  
Upper and lower case letters are ignored in the commands.

| Command   | Description |
| :---      | :--- |
| `start`   | Starts Seanox Devwex according to the current configuration. |
| `status`  | Shows information about the version, start and system time as well as the loaded modules and servers. |
| `restart` | Stops Seanox Devwex including all active modules and servers and restarts with reloaded configuration. If the new configuration contains any malfunctioning errors, the last used configuration will be reused. |
| `stop`    | Terminates Seanox Devwex including all active modules and servers. |

> Examples of program calls:
>
> ```java -cp devwex.jar com.seanox.devwex.Service start```
>
> Program call as Java binary
>
> ```devwex.cmd start```
>
> Program call via batch script in Windows
>
> ```devwex.sh status```
>
> Program call for output of operating status via shell script in
> Unix/Linux/MacOS

In addition to the command to be executed, other VM arguments can be passed to
the program as properties or environment variables.

Properties are set in format `-Dproperty=value` directly on Java call.

Environment variables are set in the operating system before the program is
called.

In both cases, the VM arguments and the environment variables can then be used
as dynamic keys in the configuration file `devwex.ini`. In this case,
properties (VM arguments) have a higher priority than environment variables.

> Example for passing VM arguments with the program call:
> 
> ```java -cp devwex.jar -Dargument1=value1 -Dargument2=value2 com.seanox.devwex.Service start```
> 
> The VM arguments `argument1` and `argument2` are passed with the
> program call at startup, which can then be used in the Java
> implementation and in the configuration. The VM arguments are
> available as dynamic keys in the configuration (see also
> [Configuration - Configuration File](
>    configuration.md#configuration-file)).

The argument `libraries` is a fixed part of the argument, which can be used to
pass additional resources, libraries and classes whose paths are separated by
the path separator defined for the operating system. At startup, Seanox Devwex
automatically adds all files specified in this way to its class path (ClassPath)
as well as the files of the specified directories.

> Example for passing VM arguments with the program call:
>
> ```
> java -cp devwex.jar -Dlibraries="../libraries" com.seanox.devwex.Service start
> ```
>
> The VM argument `libraries` is passed with the program call at startup. This
> can then be used in the Java implementation and in the configuration. 
      
> [!TIP]
> Seanox Control is a desktop-based administration tool for Seanox Devwex that
> integrates as a tray icon and and allows the display of the operating status
> as well as access to the server control and server configuration in the local
> and remote network.
      
      
- - -

[Configuration](configuration.md) | [TOC](README.md) | [Controlling and Monitoring](control.md)
