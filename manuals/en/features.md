[License Terms](license.md) | [TOC](README.md) | [System Requirements](requirements.md)
- - -

# Features

## Contents Overview
- [Architecture](#architecture)
- [Hypertext Transfer Protocol](#hypertext-transfer-protocol)
- [Remote Control](#remote-control)
- [Configuration](#configuration)
- [Interfaces](#interfaces)
  - [Server-API (SAPI)](#server-api-sapi)
  - [Module-API (XAPI)](#module-api-xapi)
  - [HTTP](#http)
  - [HTTP-Module-API (XAPI+)](#http-module-api-xapi)
  - [(Fast)CGI](#fast-cgi)
  - [XCGI](#xcgi)
  - [Telnet](#telnet)
- [Security](#security)
- [Virtualization](#virtualization)
- [Customization](#customization)
- [Extensibility](#extensibility)


## Architecture

Seanox Devwex is a multithreaded runtime container for (server) modules. for
(server) modules, which are integrated via the provided server and module and
modules API provided. In addition, the ClassLoader supports the loading and
unloading of servers and modules at runtime. at runtime.

## Hypertext Transfer Protocol

The HTTP server implementation supports virtual hosting, filters, HTTP modules,
SSL/TLS, XCGI/CGI1.1, directory listing, templates, among others.

## Remote Control

The included remote access supports a telnet-based control of the runtime
container (restart and stop) and queries about the operating status of running
servers and modules. In addition to the server implementation, a client
implementation is also included.

## Configuration

The configuration uses a central file in an advanced INI format, divided into
sections with keys and values, which supports multiple inheritance in the
sections, dynamic values, and access to system and environment variables.

## Interfaces

### Server API (SAPI)

The Server API integrates implementations that provide physical access to the
network for a protocol at one address and one port, allowing existing server and
network functionalities to be changed or new ones to be deployed.

### Module API (XAPI)

The Module API integrates implementations that act in the background and do not
provide direct external functions.

### HTTP

In accordance with specification 1.0, GET, POST and HEAD as well as OPTIONS PUT
and DELETE are supported by HTTP 1.1 and other methods can be provided via HTTP
modules, XCGI and CGI.

### HTTP Module API (XAPI+)

The HTTP Module API is an extension of the Module API for the HTTP server to
implement filter and service functions that run in the context of the server.

### (Fast)CGI

For data exchange as well as for connecting external runtime environments and
applications, the specification 1.1 of the Common Gateway Interface and thus
PHP, Perl, Python and others are supported. FastCGI is also available as an
option.

### XCGI

The XCGI is an interface based on the CGI and has the same basic principle to
communicate via the standard I/O, but also transmits server-relevant information
and environment variables in this way, so that applications can also be used
which do not have an exclusive environment or do not have access to the
environment variables of the operating system. 

### Telnet

The runtime container can be controlled via Telnet (restart and stop) and the
operating status to the running servers and modules can be queried. In addition
to the implementation of the server, a client is also included.

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

## Customization

HTTP servers and virtual hosts use customizable templates for the error pages
and list view of the directories (directory listing). With support for CGI
environment variables, the content can be designed dynamically.

## Extensibility

The modular architecture and many interfaces allow the modification, extension
and addition of functionalities.


- - -

[License Terms](license.md) | [TOC](README.md) | [System Requirements](requirements.md)
