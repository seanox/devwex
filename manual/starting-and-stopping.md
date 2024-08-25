&#9665; [Configuration](configuration.md)
&nbsp;&nbsp;&nbsp;&nbsp; &#8801; [Table of Contents](README.md)
&nbsp;&nbsp;&nbsp;&nbsp; [Control and Monitoring](control-and-monitoring.md) &#9655;
- - -

# Starting and Stopping
Seanox Devwex is a Java console application. After opening the console
(shell/prompt) it changes to the working directory `./devwex/program` and calls
Seanox Devwex with the command to be executed as Java binary or via the
available startup scripts.

> __Java binary__
> ```
> java -cp devwex.jar com.seanox.devwex.Service [command]
> java -cp devwex.jar com.seanox.devwex.Service [command] [file]
> java -cp devwex.jar com.seanox.devwex.Service [command] [address:port] 
> ```

> __Batch script in Windows__
> ```
> devwex.cmd [command]
> devwex.cmd [command] [file]
> devwex.cmd [command] [address:port]
> ```

> __Shell script in Unix/Linux/MacOS__
> ```
> devwex.sh [command]
> devwex.sh [command] [file]
> devwex.sh [command] [address:port] 
> ```

> __Telnet client__
> When the application is running, the server control can also be called via
> Telnet. 
> ```
> telnet [address] [port]
> [command]
> ```

_Overview of commands_
<table>
  <tr>
    <th>Command</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>start</td>
    <td>
      Starts the server engine according to the current configuration.
    </td>
  </tr>
  <tr>
    <td>status</td>
    <td>
      Shows information about the version, start and system time as well as the
      loaded modules and servers.
    </td>
  </tr>
  <tr>
    <td>restart</td>
    <td>
      Stops the server engine including all active modules and servers and
      restarts with reloaded configuration. If the new configuration contains
      any malfunctioning errors, the last running configuration will be reused.
    </td>
  </tr>
  <tr>
    <td>stop</td>
    <td>
      Terminates the server engine including all active modules and servers.
    </td>
  </tr>
</table>


## Passing parameters with the start
With the start, further VM arguments can be passed as properties or environment
variables. Properties are set in format `-Dproperty=value` directly on Java
call. Environment variables are set in the operating system before the program
starts. In both cases, the VM arguments and the environment variables can then
be used as dynamic keys in the configuration file `devwex.ini`. In this case,
properties (VM arguments) have a higher priority than environment variables.
        
> __Passing VM arguments with the program start__
> ```
> java -cp devwex.jar -Dargument1=value1 -Dargument2=value2 com.seanox.devwex.Service start
> ```
> With the program start, the VM arguments `argument1` and `argument2` are
> passed, which can then be used in the Java implementation and in the
> configuration. The VM arguments are available as dynamic keys in the
> configuration (_see also [Configuration - Configuration File](
>     configuration.md#configuration-file)_)
          
The class path can also be extended with the start via the additional argument
`-Dlibraries`. In comparison to `-cp` and `-jar`, directories are also supported
and the resources are loaded in the application class loader of the server
engine and not by the Bootstrap class loader of the VM. The path separator of
the operating system is used as the separator.

> __Extension of class path with the program start__
> ```
> java -cp devwex.jar -Dlibraries="../libraries" com.seanox.devwex.Service start
> ```
> With the argument `-Dlibraries`, the class path is extended to include all
> files in the `../libraries` directory. Resources are loaded in the application
> class loader of the server engine and not by the Bootstrap class loader of the
> VM.



- - -
&#9665; [Configuration](configuration.md)
&nbsp;&nbsp;&nbsp;&nbsp; &#8801; [Table of Contents](README.md)
&nbsp;&nbsp;&nbsp;&nbsp; [Control and Monitoring](control-and-monitoring.md) &#9655;
