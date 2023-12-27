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

Seanox Devwex is a experimental server engine with a modular architecture for
(web)servers and applications, which can be used with a appropriate Java runtime
environment on many operating systems. The included server instances support
HTTP with virtual hosting, filters, modules, SSL/TLS, XCGI/CGI1.1, director
listing, templates and a Telnet-base remote control the server engine.
Additional servers, modules and applications can be integrated via the available
APIs. 

__The size of the Devwex binary is limited to a maximum of 30kB. There is no
technical reason for this, it is rather the more than 20 years old quirk and fun
question -- Why are web servers so big? This should make some unconventional
decisions in the project easier to understand :-)__


# Features
- __Modular Architecture__  
  The experimental server engine supports a modular architecture for
  (web)servers and applications that are integrated via the server and module
  API provided. The own class loader supports the loading and unloading of
  servers and modules at the runtime. In addition, cascaded constructors for
  servers and modules are supported. Along with classic inheritance,
  constructors can be chained, which enables the insertion of additional class
  loaders, for example.
- __Central Configuration__  
  The configuration is based on a central file in an extended INI format, which
  is divided into sections with keys and values, supports multiple inheritance
  in the sections, dynamic values and access to system and environment
  variables.
- __Web Server Implementation / Hypertext Transfer Protocol__  
  Already included is a web server implementation with the following features: 
  Virtual hosting, filters, HTTP modules, TLS/SSL, basic as well as digest 
  access authentication, XCGI/(Fast)CGI1.1, directory listing, templates with
  support for CGI environment variables and more. In accordance with
  specification HTTP 1.0, GET, POST and HEAD as well OPTIONS, PUT and DELETE are
  supported by HTTP 1.1 and other methods can be provided via HTTP modules
  (XAPI+), XCGI and (Fast)CGI. 
- __Remote Control__  
  The included remote access supports a telnet-based control of the server
  engine (restart and stop) and queries about the operating status of running
  servers and modules. It includes a server and a client component. 
- __Extensibility and Customization__  
  The modular architecture and many interfaces (SAPI, XAPI, XAPI+, (Fast)CGI,
  XCGI) allow the modification, extension and addition of functionalities.
 

# Licence Agreement
LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
Folgenden Seanox Software Solutions oder kurz Seanox genannt.

Diese Software unterliegt der Version 2 der Apache License.

Copyright (C) 2023 Seanox Software Solutions

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
[Seanox Devwex 5.6.0](https://github.com/seanox/devwex/releases/download/5.4.0/seanox-devwex-5.6.0.zip)  
[Seanox Devwex 5.6.0 Windows](https://github.com/seanox/devwex/releases/download/5.4.0/seanox-devwex-5.6.0-win.zip)  


# Installation
Unpack the zip file to any location in the file system.  
Go to the program directory and start the program directly or by script.


# Changes
## 5.6.0 20231227  
BF: Review: Optimization and corrections  
BF: Worker: Correction of the unwanted SocketException on stop and restart  
BF: Worker: Correction of logging if the client closes the connection unexpectedly  
BF: HTML: Cleanup of the obsolete type attribute in the style tag  
BF: Project: Update of slogan (Experimental Server Engine)  
BF: (Class)Loader: Optimization and corrections of error handling  
BR: Windows: Optimization and corrections for case insensitive commands  
CR: XAPI: Change method String Module.explain() to String Module.expose()  
CR: SAPI: Change method String Server.explain() to String Server.expose()  
CR: Documentation: Reduction to English  
CR: Test: Integration of the test environment  
CR: Windows: Update of service-32.exe / service-64.exe (prunsrv.exe 1.3.4.0)  
CR: Windows: Optimization of JRE determination  

[Read more](https://raw.githubusercontent.com/seanox/devwex/master/CHANGES)


# Contact
[Issues](https://github.com/seanox/devwex/issues)  
[Requests](https://github.com/seanox/devwex/pulls)  
[Mail](http://seanox.de/contact)
