&#9665; [Starting and Stopping](starting-and-stopping.md)
&nbsp;&nbsp;&nbsp;&nbsp; &#8801; [Table of Contents](README.md)
&nbsp;&nbsp;&nbsp;&nbsp; [Development](development.md) &#9655;
- - -

# Control and Monitoring
Restart, stop and request of operating status are accessible via Telnet-based
remote access. Most operating systems contain a corresponding client. Seanox
Devwex itself also contains a suitable client component.
        
> __Telnet client__
> ```
> telnet 127.0.0.1 25000
> [command]
> ```

Seanox Devwex itself also contains a suitable client component.

> __Java binary__ 
> ```
> java -cp devwex.jar com.seanox.devwex.Service [command]
> java -cp devwex.jar com.seanox.devwex.Service [command] [address:port]
> ```

> __Batch script in Windows__
> ```
> devwex.cmd [command]
> devwex.cmd [command] [address:port]
> ```

> __Shell script in Unix/Linux/MacOS__
> ```
> devwex.sh [command]
> devwex.sh [command] [address:port]
> ```
          
_Overview of commands_
<table>
  <tr>
    <th>Command</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>status</code></td>
    <td>
      Shows information about the version, start and system time as well as the
      loaded modules and servers.
    </td>
  </tr>
  <tr>
    <td><code>restart</code></td>
    <td>
      Stops the server engine including all active modules and servers and
      restarts with reloaded configuration. If the new configuration contains
      any malfunctioning errors, the last running configuration will be reused.
    </td>
  </tr>
  <tr>
    <td><code>stop</code></td>
    <td>
      Terminates the server engine including all active modules and servers.
    </td>
  </tr>
</table>

> __Example of an output for the operating status__
>
> ```
> VERS: 0.0.0 00000000
> TIME: 2017-07-01 06:00:00
> TIUP: 2017-07-01 06:00:00
> XAPI: Seanox-SSX/5.0 20170101
> XAPI: Seanox-SSI/5.0 20170101
> XAPI: Seanox-WebDAV/5.0 20170101
> SAPI: TCP 127.0.0.1:25000
> SAPI: TCP 127.0.0.1:443
> SAPI: TCP 127.0.0.1:80
> ```

The output of the operating status is multiline.  
Each line begins with a prefix followed by the value.

_Overview of prefixes_
<table>
  <tr>
    <th>Prefix</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>VERS</code></td>
    <td>
      Version information
    </td>
  </tr>
  <tr>
    <td><code>TIME</code></td>
    <td>
      Current system time
    </td>
  </tr>
  <tr>
    <td><code>TIUP</code></td>
    <td>
      Initial start time
    </td>
  </tr>
  <tr>
    <td><code>XAPI</code></td>
    <td>
      Extension API<br>
      Modules identify themselves with manufacturer, name and
      version.<br>
      Modules without ID are not displayed.
    </td>
  </tr>
  <tr>
    <td><code>SAPI</code></td>
    <td>
      Server API<br>
      Server identifies itself with protocol, address and port.
    </td>
  </tr>
</table>

> __Telnet client__
> ```
> telnet <address> <port>
> status
> ```

> __Java binary__
> ```
> java -cp devwex.jar com.seanox.devwex.Service status
> java -cp devwex.jar com.seanox.devwex.Service status <address:port>
> ```


> __Batch script in Windows__
> ```
> devwex.cmd status
> devwex.cmd status <address:port>
> ```

> __Shell script in Unix/Linux/MacOS__
> ```
> devwex.sh status
> devwex.sh status <address:port>
> ```



- - -
&#9665; [Starting and Stopping](starting-and-stopping.md)
&nbsp;&nbsp;&nbsp;&nbsp; &#8801; [Table of Contents](README.md)
&nbsp;&nbsp;&nbsp;&nbsp; [Development](development.md) &#9655;
