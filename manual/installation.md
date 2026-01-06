&#9665; [System Requirement](system-requirement.md)
&nbsp;&nbsp;&nbsp;&nbsp; &#8801; [Table of Contents](README.md)
&nbsp;&nbsp;&nbsp;&nbsp; [Configuration](configuration.md) &#9655;
- - -

# Installation


## Content
- [Java Binary](#java-binary)
- [Java Runtime Environment](#java-runtime-environment)
- [Windows Service](#windows-service)
- [Linux Service](#linux-service)
- [MacOS Service](#macos-service)


## Java Binary
Seanox Devwex is delivered as a ZIP archive that is unpacked at any location in
the file system.

_Overview of directory structure_
```
+ devwex                                Home directory of Seanox Devwex
  + documents                           Document root for web content
    - index.html
  + manuals                             Directory for manuals, modularly organized in subdirectories
    + seanox-devwex                     Manual for Seanox Devwex  
      - configuration.md
      - control-and-monitoring.md
      - description.md
      - development.md
      - features.md
      - installation.md
      - license-terms.md
      - README.md                       Main page with table of contents
      - starting-and-stopping.md        
      - system-requirement.md
  + program                             Program and working directory of Seanox Devwex
    - devwex.cmd                        Batch script for Windows
    - devwex.sh                         Shell script for Unix/Linux
    - devwex.ini                        Seanox Devwex configuration   
    - devwex.jar                        Seanox Devwex binary
    - keystore                          Key and truststore
    - service.cmd                       Service batch script for Windows
    - service.exe                       Service runner for Windows 64 Bit
    - service.license
  + runtime                             Directory for runtime environments and extensions
  + storage                             Directory for data storage incl. log files
  + system                              Directory for system files incl. templates
    - index.html                        Template for the directory listing
    - status-2xx.html                   Template for error pages of HTTP status class 2xx
    - status-3xx.html                   Template for error pages of HTTP status class 3xx
    - status-4xx.html                   Template for error pages of HTTP status class 4xx
    - status-5xx.html                   Template for error pages of HTTP status class 5xx
  - CHNAGES                             Change log
  - LICENSE                             License      
```

> [!NOTE]
> The package does not contain a JRE. If one is already installed. This must be
> found via the environment variables `PATH` and/or `JAVA_HOME`. Alternatively,
> the JRE can also be placed in `./runtime/java` so that the batch script finds
> the JRE automatically.


## Java Runtime Environment
The Java runtime environment can be downloaded here:

- https://oracle.com/java/technologies/downloads
- https://adoptium.net/
- https://developers.redhat.com/products/openjdk/download
- https://microsoft.com/openjdk
- https://azul.com/downloads/#zulu
- https://bell-sw.com/libericajdk/
- https://aws.amazon.com/corretto
- https://jdk.java.net/

After downloading, the JRE can be installed or or without installation unpacked
to `../runtime/java`, where it is automatically found by the start script.


## Windows Service
To install as a Windows service, a package with [Apache Procrun](
    https://commons.apache.org/daemon/procrun.html) is available based on the
Java binary, which also contains service runners for 32 and 64 bit, as well as
the batch file `service.cmd` for easy use of the service.

Also the Windows package is unpacked at an arbitrary place in the file system.
Because the service should run with a Windows service account and appropriate
access rights, a place outside the user profiles is recommended for unpacking
and installation. By default, the Windows service account `NetworkService` is
used, which can be changed in `service.cmd`. The required access rights for the
program directory are set automatically during installation.

The parameters for configuring the service are centralized in the batch file and
easily accessible.

To install the service, the batch file `service.cmd`is used. To do this, open
the console (shell/command prompt) as administrator and change to the program
directory `./devwex/program` and call the batch file with the desired function.

```
usage: service.cmd [command]
```

_Overview of commands_
<table>
  <tr>
    <th>Command</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>install</code></td>
    <td>
      Installs the service
    </td>
  </tr>
  <tr>
    <td><code>update</code></td>
    <td>
      Removes the service and reinstalls it with updated configuration
    </td>
  </tr>
  <tr>
    <td><code>uninstall</code></td>
    <td>
      Removes the service
    </td>
  </tr>
  <tr>
    <td><code>start</code></td>
    <td>
      Starts the service
    </td>
  </tr>
  <tr>
    <td><code>status</code></td>
    <td>
      Output of the status of the running service
    </td>
  </tr>
  <tr>
    <td><code>restart</code></td>
    <td>
      Stops the service and restarts it
    </td>
  </tr>
  <tr>
    <td><code>stop</code></td>
    <td>
      Stops the service
    </td>
  </tr>
</table>


## Linux Service
TODO:


## MacOS Service
TODO:



- - -
&#9665; [System Requirement](system-requirement.md)
&nbsp;&nbsp;&nbsp;&nbsp; &#8801; [Table of Contents](README.md)
&nbsp;&nbsp;&nbsp;&nbsp; [Configuration](configuration.md) &#9655;
