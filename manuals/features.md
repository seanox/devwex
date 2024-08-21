&#9665; [License Terms](license-terms.md)
&nbsp;&nbsp;&nbsp;&nbsp; &#8801; [Table of Contents](README.md)
&nbsp;&nbsp;&nbsp;&nbsp; [System Requirement](system-requirement.md) &#9655;
- - -

# Features


## Content
- [Modular Architecture](#modular-architecture)
- [Central Configuration](#central-configuration)
- [Web Server Implementation / Hypertext Transfer Protocol](#web-server-implementation--hypertext-transfer-protocol)
- [Remote Control](#remote-control)
- [Extensibility and Customization](#extensibility-and-customization)
 

## Modular Architecture
The experimental server engine supports a modular architecture for (web)servers
and applications that are integrated via the server and module API provided. The
own class loader supports the loading and unloading of servers and modules at
the runtime. In addition, cascaded constructors for servers and modules are
supported. Along with classic inheritance, constructors can be chained, which
enables the insertion of additional class loaders, for example.

## Central Configuration
The configuration is based on a central file in an extended INI format, which is
divided into sections with keys and values, supports multiple inheritance in the
sections, dynamic values and access to system and environment variables.

## Web Server Implementation / Hypertext Transfer Protocol
Already included is a web server implementation with the following features:
Virtual hosting, filters, HTTP modules, TLS/SSL, basic as well as digest access
authentication, XCGI/(Fast)CGI1.1, directory listing, templates with support for
CGI environment variables and more. In accordance with specification HTTP 1.0,
GET, POST and HEAD as well OPTIONS, PUT and DELETE are supported by HTTP 1.1 and
other methods can be provided via HTTP modules (XAPI+), XCGI and (Fast)CGI. 

## Remote Control
The included remote access supports a telnet-based control of the server engine
(restart and stop) and queries about the operating status of running servers and
modules. It includes a server and a client component. 

## Extensibility and Customization
The modular architecture and many interfaces (SAPI, XAPI, XAPI+, (Fast)CGI,
XCGI) allow the modification, extension and addition of functionalities.



- - -
&#9665; [License Terms](license-terms.md)
&nbsp;&nbsp;&nbsp;&nbsp; &#8801; [Table of Contents](README.md)
&nbsp;&nbsp;&nbsp;&nbsp; [System Requirement](system-requirement.md) &#9655;
