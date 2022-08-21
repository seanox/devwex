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

__The size of the Devwex binary is limited to a maximum of 30kB. There is no
technical reason for this, it is rather the more than 20 years old quirk and
question -- Why are web servers so big? This should make some unconventional
decisions in the project easier to understand :-)__


# Features
- __Architecture__  
  Seanox Devwex is a multithreaded runtime container for (server)modules, which
  are integrated via the provided Server and Module API. The own ClassLoader
  supports the loading and unloading of servers and modules at the runtime. 
- __Hypertext Transfer Protocol__  
  The HTTP server implementation provides virtual hosting, filters, HTTP
  modules, SSL/TLS, XCGI/CGI1.1, directory listing, templates and more. In  
  accordance with specification 1.0, GET, POST and HEAD as well as OPTIONS, PUT
  and DELETE are supported by HTTP 1.1 and other methods can be provided via
  HTTP modules, XCGI and CGI. 
- __Remote Control__  
  The included remote access supports a telnet-based control of the runtime
  container (restart and stop) and queries about the operating status of
  running servers and modules. In addition to the server implementation, a
  client implementation is also included. 
- __Configuration__  
  The configuration uses a central file in an advanced INI format, divided into
  sections with keys and values, which supports multiple inheritance in the
  sections, dynamic values, and access to system and environment variables.
- __Server API (SAPI)__  
  The Server API integrates implementations that provide physical access to the
  network for a protocol at one address and one port, allowing existing server
  and network functionalities to be changed or new ones to be deployed. 
- __Module API (XAPI)__  
  The module API integrates implementations that act in the background and do
  not provide direct external functions. 
- __HTTP Module API (XAPI+)__  
  The HTTP Module API is an extension of the Module API for the HTTP server to
  implement filter and service functions that run in the context of the server. 
- __(Fast)CGI__  
  For data exchange as well as for connecting external runtime environments and
  applications, the specification 1.1 of the Common Gateway Interface and thus
  PHP, Perl, Python and others are supported. FastCGI is also available as an
  option. 
- __XCGI__  
  The XCGI is an interface based on the CGI and has the same basic principle to
  communicate via the standard I/O, but also transmits server-relevant
  information and environment variables in this way, so that applications can
  also be used which do not have an exclusive environment or do not have access
  to the environment variables of the operating system. 
- __Telnet__  
  The included remote access supports a telnet-based control of the container
  (restart and stop) and queries about the operating status of running servers
  and modules. In addition to the server implementation, a client
  implementation is also included. 
- __Security__  
  Transport Layer Security (TLS) and Secure Socket Layer (SSL) with server and
  client certificates are supported for secure data transfer, allowing
  certificates to be assigned to each physical host individually, by
  inheritance in groups or globally.  
  Access to directories and files can be provided with basic as well as digest
  access authentication, which supports groups and can be controlled by filters
  that support freely definable rules, individual error pages, automatic
  redirections and modules. 
- __Virtualization__  
  The HTTP server supports virtual hosting and aliasing for virtual paths. 
- __Individuality__  
  HTTP servers and virtual hosts use customizable templates for the error pages
  and list view of the directories (directory listing). With support for CGI
  environment variables, the content can be designed dynamically. 
- __Expandability__  
  The modular architecture and many interfaces allow the modification, extension
  and addition of functionalities. 


# Licence Agreement
LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
Folgenden Seanox Software Solutions oder kurz Seanox genannt.

Diese Software unterliegt der Version 2 der Apache License.

Copyright (C) 2022 Seanox Software Solutions

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.


# System Requirement
- Java Runtime 8.x or higher


# Downloads
[Seanox Devwex 5.4.0](https://github.com/seanox/devwex/releases/download/5.4.0/seanox-devwex-5.4.0.zip)  
[Seanox Devwex 5.4.0 Windows](https://github.com/seanox/devwex/releases/download/5.4.0/seanox-devwex-5.4.0-win.zip)  


# Installation
Unpack the zip file to any location in the file system.  
Go to the program directory and start the program directly or by script.


# Changes (Change Log)
## 5.4.0 20210411 (summary of the current version)  
BF: HTTP(S) Server Status: Update of class 4xx  
BF: HTTP(S) CGI: Correction of REQUEST_URI  
BF: HTTP(S) CGI: Correction of SCRIPT_URI / SCRIPT_URL  
BF: Service: Optimization / correction of Service::Print  
BF: Documentation: Update / corrections  
CR: HTTP(S) CGI: Output of the error pipe of the CGI process  
CR: HTTP(S) CGI: PATH_CONTEXT replaces PATH_ABSOLUTE (omitted)  
CR: Build: Target release replaced archive (omitted)  
CR: Windows: Integration and merge of Seanox Devwex Service project  

[Read more](https://raw.githubusercontent.com/seanox/devwex/master/CHANGES)


# Contact
[Issues](https://github.com/seanox/devwex/issues)  
[Requests](https://github.com/seanox/devwex/pulls)  
[Mail](http://seanox.de/contact)
