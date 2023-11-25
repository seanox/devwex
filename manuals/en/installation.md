[System Requirements](requirements.md) | [TOC](README.md) | [Configuration](configuration.md)
- - -

# Installation

Seanox Devwex is delivered as a ZIP archive that is unpacked at any location in
the file system.


## Contents Overview
- [Windows Service](#windows-service)
- [Linux Service](#linux-service)
- [MacOS Service](#macos-service)


## Windows Service
For Windows, a distribution of Seanox Devwex with [Procrun](
    https://commons.apache.org/daemon/procrun.html) is available, which installs
Seanox Devwex in Windows as a service. The distribution is based on the Java
version and contains in additional additionally the service runners for 32 and
64 bit as well the batch file `service.cmd` for easy usage of the service.
Also the Windows distribution is unpacked at an arbitrary place in the file
system. Because the service should run with a Windows service account and
appropriate access rights, a place outside the user profiles is recommended for
unpacking and installation. By default, the service is installed with the
Windows service account `NetworkService`, but other service accounts can also
be used. The access rights required for the program directory are set during
installation. The parameters to configure the service have been bundled in the
batch file and are easily accessible.

To install the service, the batch file `service.cmd` is used. To do this, open
the console (shell/command prompt) as administrator and change to the program
directory `./devwex/program` and call the batch file with the desired function.

```
usage: service.cmd [command]
```

Overview of available commands:  
The letter case (upper and lower case) is to be respected for the commands.

| Command     | Description                                                       |
| :---        | :---                                                              |
| `install`   | Installs the service.                                             |
| `update`    | Removes the service and reinstalls it with updated configuration. |
| `uninstall` | Removes the service.                                              |
| `start`     | Starts the service.                                               |
| `status`    | Output of the status of the running service.                      |
| `restart`   | Stops the service and restarts it.                                |
| `stop`      | Stops the service.                                                |

> [!NOTE]
> The distribution does not contain a JRE.
>
> If an already installed or in the system available one should be used, the
> Java home can be specified in `service.cmd` with the variable `java`.
> Alternatively, the JRE can also be placed in `./runtime/java`. In this case
> the batch script will find the JRE automatically.


## Linux Service

TODO:


## MacOS Service

TODO:


- - -

[System Requirements](requirements.md) | [TOC](README.md) | [Configuration](configuration.md)
