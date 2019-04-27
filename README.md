# Description
Seanox Devwex is a minimalist runtime container with a modular architecture for
(web) servers and applications. The included server instances support HTTP with
virtual hosting, filters, modules, SSL/TLS, XCGI/CGI1.1, directory index,
templates and a telnet-based remote access to container control. Additional
servers and modules/applications can be integrated via the available APIs. The
runtime container with the servers is a pure Java implementation and can be used
on many operating systems due to the corresponding runtime environments. 


# Features

## Architecture
Seanox Devwex is a multithreaded runtime container for (server)modules, which
are integrated via the provided Server and Module API. The own ClassLoader
supports the loading and unloading of servers and modules at the runtime. 

## Hypertext Transfer Protocol
The HTTP server implementation provides virtual hosting, filters, HTTP modules,
SSL/TLS, XCGI/CGI1.1, directory index, templates and more.  
In accordance with specification 1.0, GET, POST and HEAD as well as OPTIONS PUT
and DELETE are supported by HTTP 1.1 and other methods can be provided via HTTP
modules, XCGI and CGI. 

## Remote Control
The included remote access supports a telnet-based control of the runtime
container (restart and stop) and queries about the operating status of running
servers and modules.  
In addition to the server implementation, a client implementation is also
included. 

## Configuration
The configuration uses a central file in an advanced INI format, divided into
sections with keys and values, which supports multiple inheritance in the
sections, dynamic values, and access to system and environment variables. 

## Server API (SAPI)
The Server API integrates implementations that provide physical access to the
network for a protocol at one address and one port, allowing existing server and
network functionalities to be changed or new ones to be deployed. 

## Module API (XAPI)
The module API integrates implementations that act in the background and do not
provide direct external functions. 

## HTTP Module API (XAPI+)
The HTTP Module API is an extension of the Module API for the HTTP server to
implement filter and service functions that run in the context of the server. 

## (Fast)CGI
For data exchange as well as for connecting external runtime environments and
applications, the specification 1.1 of the Common Gateway Interface and thus
PHP, Perl, Python and others are supported. FastCGI is also available as an
option. 

## XCGI
The XCGI is an interface based on the CGI and has the same basic principle to
communicate via the standard I/O, but also transmits server-relevant information
and environment variables in this way, so that applications can also be used
which do not have an exclusive environment or do not have access to the
environment variables of the operating system. 

## Telnet
The included remote access supports a telnet-based control of the container
(restart and stop) and queries about the operating status of running servers and
modules.  
In addition to the server implementation, a client implementation is also
included. 

## Security
Transport Layer Security (TLS) and Secure Socket Layer (SSL) with server and
client certificates are supported for secure data transfer, allowing
certificates to be assigned to each physical host individually, by inheritance
in groups or globally.

Access to directories and files can be provided with basic as well as digest
access authentication, which supports groups and can be controlled by filters
that support freely definable rules, individual error pages, automatic
redirections and modules. 

## Virtualization
The HTTP server supports virtual hosting and aliasing for virtual paths. 

## Individuality
HTTP servers and virtual hosts use customizable templates for the error pages
and list view of the directories (directory index). With support for CGI
environment variables, the content can be designed dynamically. 

## Expandability
The modular architecture and many interfaces allow the modification, extension
and addition of functionalities. 



# Licence Agreement
Seanox Software Solutions ist ein Open-Source-Projekt, im Folgenden
Seanox Software Solutions oder kurz Seanox genannt.

Diese Software unterliegt der Version 2 der GNU General Public License.

Copyright (C) 2019 Seanox Software Solutions

This program is free software; you can redistribute it and/or modify it under
the terms of version 2 of the GNU General Public License as published by the
Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
Street, Fifth Floor, Boston, MA 02110-1301, USA.


# System Requirement
- Java Runtime 8.x or higher


# Downloads
[Seanox Devwex 5.1.0](https://github.com/seanox/devwex/raw/master/releases/seanox-devwex-5.1.0.zip)  
[Seanox Devwex Sources 5.1.0](https://github.com/seanox/devwex/raw/master/releases/seanox-devwex-5.1.0-src.zip)  
[Seanox Devwex Test 5.1.0](https://github.com/seanox/devwex-test/raw/master/releases/seanox-devwex-test-5.1.0.zip) 


# Extensions
[Seanox Devwex Service 1.1.0.0](https://github.com/seanox/devwex-service/raw/master/releases/seanox-devwex-service-1.1.0.0.zip)
Runs Seanox Devwex as a service in Windows.


# Installation
Unpack the zip file to any location in the file system.  
Go to the program directory and start the program directly or by script.


# Changes (Change Log)
## 5.2.0 201905xx (summary of the next version)  
BF: HTTP(S) Correction  
BF: Generator: Correction when rendering successive placeholders #[...]#[...]  
BF: HTTP(S) - Request: Optimization/correction for invalid requests/connections without data streams  
CR: Generator: Optimization / Review  
CR: HTTP(S) - AccessLog: Optimization  
CR: Project: Uniform use of ./LICENSE and ./CHANGES  
CR: KeyStore: Change to use PKCS12  

[Read more](https://raw.githubusercontent.com/seanox/devwex/master/CHANGES)


# Contact
[Support](http://seanox.de/contact?support)  
[Development](http://seanox.de/contact?development)  
[Project](http://seanox.de/contact?service)  
[Page](http://seanox.de/contact)  


# Thanks!
<img src="https://raw.githubusercontent.com/seanox/seanox/master/sources/resources/images/thanks.png">

[JetBrains](https://www.jetbrains.com/?from=seanox)  
Sven Lorenz  
Andreas Mitterhofer  
[novaObjects GmbH](https://www.novaobjects.de)  
Leo Pelillo  
Gunter Pfannm&uuml;ller  
Annette und Steffen Pokel  
Edgar R&ouml;stle  
Michael S&auml;mann  
Markus Schlosneck  
[T-Systems International GmbH](https://www.t-systems.com)
