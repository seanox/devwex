&#9665; [Control and Monitoring](control-and-monitoring.md)
&nbsp;&nbsp;&nbsp;&nbsp; &#8801; [Table of Contents](README.md)
- - -

# Development
The extensibility of Seanox Devwex is a key feature. Implementing the server
engine and its components is a challenge to achieve maximum functionality with
minimum resources and maintain a clean architecture.
        

## Content
- [Architecture](#architecture)
- [Sequences](#sequences)
  - [Start](#start)
  - [Module Call](#module-call)
  - [Restart](#restart)
  - [Stop](#stop)
- [Extensibility](#extensibility)
- [Server](#server)
  - [API (SAPI)](#api-sapi)
  - [Implementation](#implementation)
- [Modules](#modules)
  - [API (XAPI)](#api-xapi)
  - [Implementation](#implementation-1)
- [HTTP Modules](#http-modules)
  - [Filters](#filters)
    - [API (XAPI+)](#api-xapi-1)
    - [Implementation](#implementation-2)
  - [Service](#service)
    - [API (XAPI+)](#api-xapi-2)
    - [Implementation](#implementation-3)
- [Cascaded Components](#cascaded-components)
- [Development Environment (SDK)](#development-environment-sdk)


## Architecture
![Architecture](https://github.com/seanox/devwex/raw/refs/heads/master/manual/architecture.svg)


## Sequences
Start, restart and stop of the servers as well as the loading, requesting and
unloading of modules are fixed sequences of the server engine.
      
  
### Start
- The class path is extended to include all files of the directories specified
  with the VM argument `-Dlibraries`.
- All modules from section `INITIALIZE` are loaded and initialized and
  registered via constructor `Module(String options)`.
- All servers are determined by searching for sections that end in `INI` and to
  which an implementation can be found in the class path, which are loaded,
  registered, and initialized via constructor `Server(String name, Settings
      settings)` or `Server(String name, Object settings)`. To do this, each
  server is given the name of the detected section and a complete copy of the
  central configuration as a Settings object. After successful initialization,
  the server is started as a thread and can start its work in method
  `Server.run()`.


### Module Call
- If the module is not yet loaded, this is determined from the current class
  path, initialized and registered via `Module(String options)`. A configuration
  is not passed, since only one central configuration is provided for modules in
  section `INITIALIZE`.
- If the module is already loaded, the current instance is used.


### Restart
This sequence is the combination of [Stop](#stop) and [Start](#start).

- All registered server instances are prompted to quit using the method
  `Server.destroy()`.
- All registered modules are prompted to quit by method `Module.destroy()`.
- The server engine waits for the end of all registered servers.
- All modules and servers are unloaded by discarding the currently used class
  loader.
- The class path is extended to include all files of the directories specified
  with the VM argument `-Dlibraries`.
- All modules from section `INITIALIZE` are loaded and initialized and
  registered via constructor `Module(String options)`.
- All servers are determined by searching for sections that end in `INI` and to
  which an implementation can be found in the class path, which are loaded,
  registered, and initialized via constructor `Server(String name, Settings
      settings)` or `Server(String name, Object settings)`. To do this, each
  server is given the name of the detected section and a complete copy of the
  central configuration as a Settings object. After successful initialization,
  the server is started as a thread and can start its work in method
  `Server.run()`.


### Stop
- All registered server instances are prompted to quit using the method
  `Server.destroy()`.
- All registered modules are prompted to quit by method `Module.destroy()`.
- The server engine waits for the end of all registered servers.
- All modules and servers are unloaded by discarding the currently used class
  loader.


## Extensibility
The extension of Seanox Devwex is possible in several ways. The server engine
provides a Server API, Module API, and HTTP Module API. Special interfaces and
abstractions are not included in the service itself, which uses them and
controls all servers and modules via reflections and silent
standards/interfaces, which may require detailed knowledge of the components and
operation of the server engine. The implementation with Seanox Commons as part
of the Seanox Devwex SDK (for Seanox Devwex 5.x in development) is easier and
more convenient. It contains the necessary interfaces and abstractions and
provides additional class loader layers.
        
__The following describes only the Server API, Module API, and HTTP Module API
contained in the server engine.__

        
## Server
Servers provide physical access to the network for a protocol at one address and
one port. Seanox Devwex integrates servers via the Server API (SAPI), which can
also be used to modify or deploy existing server and network functionalities.
    
    
### API (SAPI)
The server API includes the initialization and termination of server and is
based on the implementation of the __Runnable interface__. The order of
initialization is determined by the order of the server configurations in the
configuration file `devwex.ini`. Server instances are created via their
__constructor__. If this does not exist, this will cause an error. Created
server instances are started by the server engine (Service) by the
__run method__ of the Runnable interface and are requested to terminate by
__destroy method__. Both methods are called asynchronously. If the termination
is delayed, the server engine waits for all registered server instances to
finish. Optionally, the implementation of an __expose method__ is recommended,
which returns general information such as protocol and network connection. 
      
  
### Implementation
```java
public class Server implements Runnable {

    public Server(String name, Setting settings) {

        // A server is initiated by server engine via the constructor. If the
        // constructor is not available, the server is considered incompatible
        // and is not loaded. The configuration is passed next to the name as a
        // Settings object.
    }

    public String expose() {

        // The optional method returns information about the server in the
        // format: <PROTOCOL HOST-NAME:PORT> or <PROTOCOL HOST-ADDRESS:PORT>.
        // This information should be static or set during initialization.
    }              

    public void run() {

        // The server will be started as a thread.
    }

    public void destroy() {

        // The server is requested to close all resources, data streams and
        // processes initiated or used by it.
    }
}
```


## Modules
Modules are extensions intended for background activities. They are identified
by the class and are loaded, initialized and configured by the server engine
(Service) at startup or restart or at runtime in servers and modules via the
application class loader. Modules are global extensions that are available to
all components once instantiated. Optionally, it is possible to use the context
class loader from the Seanox Devwex SDK, so that modules can also be used in
several independent instances with their own class loader.

        
### API (XAPI)
The module API includes the initialization and termination of modules.
Initialization is based on section `INITIALIZE` of the configuration file
`devwex.ini` when the server engine (Service) is started or with the first
programmatic request of a module at runtime. In all cases, modules are initiated
via the __constructor__ to which the configuration is passed. If this
constructor does not exist, this will cause an error. Runtime behavior and
health are not actively monitored by the server engine, only whether a module
has been loaded is relevant here. Modules are requested for termination via the
__destroy method__. Termination is also not actively monitored by the server
engine; it discards the modules by unloading them from the class loader.
Optionally, the implementation of an __expose method__ is recommended, which
returns general information such as manufacturer and version. 
      

### Implementation
```java
public class Module {

    public Module(String options) {

        // A module is initiated via this constructor. If this is not available,
        // the module is considered incompatible with the API and is not loaded.
        // The configuration is optional and is only taken from the section 
        // [INITIALIZE] if it is initiated centrally. 
    }

    public String expose() {

        // The optional method returns information about the module in the
        // format: <PRODUCER-MODULE/VERSION>. This information should be static
        // or set during initialization.
    }

    public void destroy() {

        // The module is requested to close all resources, data streams and
        // processes initiated or used by it.
    }
}
```


## HTTP Modules
HTTP Modules are an extension of the server engine Module API specifically
designed for filtering, accepting and processing HTTP requests that run in the
context of the server. HTTP modules allow you to implement filter and service
functionalities, which is described in detail below, based on individual
implementations that can also be combined in an HTTP module.


### Filters
Access to the physical and virtual hosts can be controlled by specially defined
rules. In addition to forwarding and responding to requests with a defined
server status, processing can also be passed to a filter. HTTP filters are
intended for checking specific requests and, if necessary, manipulating incoming
requests; they should only have access to the request header if the request is
not completely processed by the filter.

        
#### API (XAPI+)
The API corresponds to the Module API, which is extended by `Module.filter(Worker
    worker, String options)`. The passed worker is the request processing
instance and has access to the server, socket, request, response, configuration
and environment. With the configuration you can set further parameters and
options for a filter, which are then optionally passed as string.
       
 
#### Implementation
```java
public class Module {

    public Module(String options) {

        // A module is initiated via this constructor. If this is not available, 
        // the module is considered incompatible with the API and is not loaded.
        // The configuration is optional and is only taken from the section
        // [INITIALIZE] if it is initiated centrally.
    }

    public String expose() {

        // The optional method returns information about the module in the
        // format: <PRODUCER-MODULE/VERSION>. This information should be static
        // or set during initialization.
    }

    public void filter(Worker worker, String options) {

        // This method takes over the check and, if necessary, the manipulation
        // of the request or processes and responds it completely. If the
        // request is only to be responded with a server status, the service
        // method can set this status and does not have to respond to the
        // request itself. Instead, the worker implementation responds.
    }

    public void destroy() {

        // The module is requested to close all resources, data streams and
        // processes initiated or used by it.
    }
}
```


### Service
The target of an HTTP request these are directories, files and dynamic content
from external CGI and XCGI applications that are requested via virtual paths.
HTTP services provide another form of dynamic content. These take over the
complete processing of incoming requests and have unrestricted access.


#### API (XAPI+)
The API corresponds to the Module API, which is extended by
`Module.service(Worker worker, String options)`. The passed worker is the
request processing instance and has access to the server, socket, request,
response, configuration and environment. With the configuration, further
parameters and options can be set for a module, which are then optionally passed
as string.
        

#### Implementation
```java
public class Module {

    public Module(String options) {

        // A module is initiated via this constructor. If this is not available,
        // the module is considered incompatible with the API and is not loaded.
        // The configuration is optional and is only taken from the section
        // [INITIALIZE] if it is initiated centrally.
    }

    public String expose() {

        // The optional method returns information about the module in the
        // format: <PRODUCER-MODULE/VERSION>. This information should be static 
        // or set during initialization.
    }

    public void service(Worker worker, String options) {

        // This method takes over the complete processing of the request and
        // responds it. If the request is only to be responded with a server
        // status, the service method can set this status and does not have to
        // respond to the request itself. Instead, the worker implementation
        // responds.
    }

    public void destroy() {

        // The module is requested to close all resources, data streams and
        // processes initiated or used by it.
    }
}
```


## Cascaded Components
The binding of components is based on the concatenation of calls instead of
inheritance. This allows e.g. the use of different constructors and the
insertion of additional class loader. Inheritance can still be used, even in
combination. The function is available for modules (XAPI) and servers (SAPI) and
is defined directly with the configuration of the extension.

> __The cascade is described separately by the arrow symbol.__
> ```
> package.Delegate > package.another.Delegate > ... > package.Target
> ```


## Development Environment (SDK)
For development and experimentation with Seanox Devwex, a special development
environment (SDK) is available (for Seanox Devwex 5.x in development). The
completely preconfigured project for the Eclipse IDE includes Seanox Devwex,
Seanox Commons and Seanox Devwex WebSocket. In addition to an extensive API with
detailed documentation, the necessary interfaces as well as abstractions,
additional class loader layers for development and many functional examples are
included.



- - -
&#9665; [Control and Monitoring](control-and-monitoring.md)
&nbsp;&nbsp;&nbsp;&nbsp; &#8801; [Table of Contents](README.md)
