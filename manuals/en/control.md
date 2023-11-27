[Starting and Stopping](starting.md) | [TOC](README.md) | [Development](development.md)
- - -

# Control and Monitoring

Restart, stop and request of operating status are accessible via Telnet-based
remote access. Most operating systems contain a corresponding client. Seanox
Devwex itself also contains a suitable client component.

The Telnet request only expects the command to be executed, whereby upper and
lower case letters can be ignored.


## Contents Overview
- [Client](#client)
- [Status](#status)
- [Restart](#restart)
- [Stop](#stop)


## Client

When using Seanox Devwex as a Telnet client, the connection data is determined
from the server configuration `devwex.ini`, which must be in the working
directory.

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

> Telnet
> 
> ```
> telnet 127.0.0.1 25000
> <command>
> ```


## Status

Shows information about the version, start and system time as well as the loaded
modules and servers.

> ```
> telnet 127.0.0.1 25000
> status
> ```
>
> Output 
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
>
> The output of the operating status is multi-line and each line begins with a
> prefix followed by the value.
> 
> `VERS` Version information
> `TIME` Current system time
> `TIUP` Initial start time of the service
> `XAPI` Extension API (module identification: manufacturer name/version)
> `SAPI` Server API (server identification: protocol address:port)


## Restart

Stops Seanox Devwex including all active modules and servers and restarts with
reloaded configuration. If the new configuration contains any malfunctioning
errors, the last used configuration will be reused.

> ```
> telnet 127.0.0.1 25000
> restart
> ```
>
> Output
> 
> ```
> SERVICE RESTARTED
> ```


## Stop

Terminates Seanox Devwex including all active modules and servers.

> ```
> telnet 127.0.0.1 25000
> stop
> ```
>
> Output
> 
> ```
> SERVICE STOPPED
> ```
      
      
- - -

[Starting and Stopping](starting.md) | [TOC](README.md) | [Development](development.md)
