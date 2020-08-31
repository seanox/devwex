<p>
  <a href="https://github.com/seanox/devwex/pulls"
      title="Development is waiting for new issues / requests / ideas">
    <img src="https://img.shields.io/badge/development-passive-blue?style=for-the-badge">
  </a>
  <a href="https://github.com/seanox/devwex/issues">
    <img src="https://img.shields.io/badge/maintenance-active-green?style=for-the-badge">
  </a>
  <a href="http://seanox.de/contact">
    <img src="https://img.shields.io/badge/support-active-green?style=for-the-badge">
  </a>
</p>


# Description
Seanox Devwex is a minimalist runtime container with a modular architecture for
(web) servers and applications. The included server instances support HTTP with
virtual hosting, filters, modules, SSL/TLS, XCGI/CGI1.1, directory listing,
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
SSL/TLS, XCGI/CGI1.1, directory listing, templates and more.  
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
and list view of the directories (directory listing). With support for CGI
environment variables, the content can be designed dynamically. 

## Expandability
The modular architecture and many interfaces allow the modification, extension
and addition of functionalities. 



# Licence Agreement
Seanox Software Solutions ist ein Open-Source-Projekt, im Folgenden
Seanox Software Solutions oder kurz Seanox genannt.

Diese Software unterliegt der Version 2 der GNU General Public License.

Copyright (C) 2020 Seanox Software Solutions

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
[Seanox Devwex 5.3.0](https://github.com/seanox/devwex/raw/master/releases/seanox-devwex-5.3.0.zip)  
[Seanox Devwex 5.3.0 Sources](https://github.com/seanox/devwex/raw/master/releases/seanox-devwex-5.3.0-src.zip)  
[Seanox Devwex 5.3.0 Test](https://github.com/seanox/devwex-test/raw/master/releases/seanox-devwex-test-5.3.0.zip) 


# Extensions
[Seanox Devwex Service 1.1.0.1](https://github.com/seanox/devwex-service/raw/master/releases/seanox-devwex-service-1.1.0.1.zip)
Runs Seanox Devwex as a service in Windows.


# Installation
Unpack the zip file to any location in the file system.  
Go to the program directory and start the program directly or by script.


# Changes (Change Log)
## 5.3.0 20200831 (summary of the current version)  
BF: HTTP(S) AccessLog: Correction in the file name of the log file (use http_host instead of remote_host)  
BF: HTTP(S) Content Types: Correction of xml/xsl/xslt to use application/xslt+xml  
BF: HTTP(S) Request: Correction of the request validation  
BF: Generator: Correction of expensive/hungry RegExp  
BF: Manual: Correction for chrome-based browsers  
CR: Manual: Improvement of the navigation  
CR: Project: Automatic update of the version in README.md  
CR: Build: Harmonization when updating the version  
CR: HTTP(S) Status: Added option [H] (Header Only)  
CR: HTTP(S) CGI: Not allowed/configured methods are now answered with status 405  
CR: HTTP(S) CGI: Update of the PHP preparation  
CR: HTTP(S) CGI: Change of the option [P] to [D]  
CR: HTTP(S) Directory Index: Optimization / reduction of code  
CR: HTTP(S) Directory Index: Change from placeholder 'files' to 'file'  
CR: HTTP(S) Directory Index: Omission of the formatting of the file size  
CR: HTTP(S) Directory Index: Realignment of the column 'type'  
CR: Worker: Optimization / reduction of code  
CR: Remote: Optimization / reduction of code  
CR: Sources: Update of the comment format  
CR: XAPI: Integration of cascaded components  

[Read more](https://raw.githubusercontent.com/seanox/devwex/master/CHANGES)


# Contact
[Issues](https://github.com/seanox/devwex/issues)  
[Requests](https://github.com/seanox/devwex/pulls)  
[Mail](http://seanox.de/contact)


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
