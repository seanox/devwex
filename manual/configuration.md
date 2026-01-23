&#9665; [Installation](installation.md)
&nbsp;&nbsp;&nbsp;&nbsp; &#8801; [Table of Contents](README.md)
&nbsp;&nbsp;&nbsp;&nbsp; [Starting and Stopping](starting-and-stopping.md) &#9655;
- - -

# Configuration
Seanox Devwex uses a central preconfigured configuration `devwex.ini` which is
located in the current working directory `./devwex/program`. The configuration
is divided into sections with keys and values, which also supports dynamic
values and access to system and environment variables.


## Content
- [Configuration File](#configuration-file)
- [Configuration Structure](#configuration-structure)
- [General Application Configuration](#general-application-configuration)
- [Initialisation](#initialisation)
- [Server](#server)
- [Remote Control Server](#remote-control-server)
- [HTTP / HTTPS Server](#http--https-server)
  - [Server](#server-1)
  - [Transport Layer Security / Secure Socket Layer](#transport-layer-security--secure-socket-layer)
  - [Client Authentication / Mutual Authentication](#client-authentication--mutual-authentication)
  - [Virtual Hosts / Virtual Hosting](#virtual-hosts--virtual-hosting)
  - [Filters](#filters)
  - [Virtual Paths (Aliasing and Redirection)](#virtual-paths-aliasing-and-redirection)
  - [Basic Access Authentication / Digest Access Authentication](#basic-access-authentication--digest-access-authentication)
  - [Directory Listing](#directory-listing)
  - [Environment Variables](#environmental-variables)
  - [Common Gateway Interface](#common-gateway-interface)
- [Status Codes](#status-codes)
- [Media Types](#media-types)


## Configuration File
The configuration uses an extended INI format which, like the original format,
is line-based and uses sections with keys with values, whereby sections and keys
are case-insensitive and declarations with the same name cause sections and keys
to be overwritten. One extension is that sections can use multiple inheritance
by using the keyword `EXTENDS` followed by the names of the referenced sections.
In this way, all keys and values of the referenced sections are adopted as the
basis and can then be overwritten in the section.

The values are assigned to the keys with an equals sign. This assignment can be
continued in the next line if this starts with a plus sign. Values can also be
fixed, variable and optionally assigned. With the option `[?]` at the end of a
key, the system searches for a key with the same name in the system properties
of the Java runtime environment. If no suitable value is found, an optionally
specified value is used. Without a value, these keys are interpreted as
non-existent and ignored.


Comments begin with a semicolon or, more commonly, a hash. They are optional and
can appear anywhere in a line; all characters following them are then no longer
part of the section, key, or value. The `[+]` option at the end of a key can be
used to disable the comment function for that key, allowing a semicolon or hash
in the value.

> [!NOTE]
> __The semicolon (`;`) is the original comment character for this format. The
> hash (`#`) is also supported, which is common in many modern configuration
> formats. Both characters have the same function. For simplicity, the following
> documentation uses the semicolon throughout; however, the hash can be used
> interchangeably.__

Comments begin with a semicolon, are optional and can be used anywhere in a
line, so the following characters are not part of section, key or value. With
the option `[+]` at the end of a key, the use of comments for that key can be
disabled and the semicolon used in the value.

Sections, keys and values also support a hexadecimal notation, starting with
`0x...` followed by the hexadecimal string, which can only be used for the
complete element.

For better readability, spaces can be inserted before the keys, which will be
ignored by the configuration.

> __Example__
> ```
> [SECTION] EXTENDS SECTION-A SECTION-B      ;comment
>   PARAM-A                 = VALUE-1        ;comment
>   PARAM-B             [+] = VALUE-2; VALUE-3
>                           + VALUE-4; VALUE-5
>   PARAM-C          [?][+] = VALUE-6; VALUE-7
>   PARAM-E          [?]                     ;comment
>
> [0x53454354494F4E2D41]
>   PARAM-A                 = 0x574552542D31 ;comment
>   0x504152414D2D42        = VALUE-2        ;comment
>   0x504152414D2D43    [+] = 0x574552542D33
>   PARAM-D                 = 0x574552542D34
>                           + 0x574552542D35
>   PARAM-E          [?][+] = 0x574552542D363B20574552542D37
>   0x504152414D2D45 [?]                     ;comment
> ```

__The indentation has no relevance, it is only intended to improve
readability.__

> __Line 1__
> ```
> [SECTION] EXTENDS SECTION-A SECTION-B ;comment
> ```
> The section with the name `SECTION` is defined, the keyword `EXTENDS` refers
> to the derivation from the sections `SECTION-A` and `SECTION-B`. Thus,
> `SECTION` is based on the keys and values of the sections `SECTION-A` and
> `SECTION-B`. From the semicolon onwards, the following characters are
> interpreted as comments.

> __Line 2__
> ```
> PARAM-A = VALUE-1 ;comment
> ```
> The value `VALUE-1` is assigned to the key `PARAM-A`. The following characters
> are interpreted as comments from the semicolon onwards.

> __Line 3__
> ```
> PARAM-B [+] = VALUE-2; VALUE-3
> ```
> The key `PARAM-B` is assigned `VALUE-2; VALUE-3` as a value, with the option
> `[+]` at the end of the key disables the line comment and uses all characters
> for the value assignment, but it is not possible to specify a comment in this
> line.

> __Line 4__
> ```
> + VALUE-4; VALUE-5
> ``` 
> The value assignment of line 3 is continued and the value `VALUE-4;  VALUE-5`
> is added to the existing value of the key `PARAM-B`. The option `[+]` from
> line 3 is also taken from line 4, which also disables the line comment and
> uses all characters as value assignment. It is not possible to enter a comment
> in this line. Further options are not possible. 

> __Line 5__
> ```
> PARAM-C [?][+] = VALUE-6; VALUE-7
> ``` 
> The value assignment for the key `PARAM-C` is dynamic, and the system
> properties (VM arguments) of the Java runtime environment are searched for the
> key `PARAM-C` of the same name, ignoring upper and lower case. The key must be
> part of the runtime environment or can be set as VM argument (property) at the
> program start in the form `-Dkey=value`. If the system properties of the Java
> runtime environment do not contain a corresponding key, `VALUE-6; VALUE-7` is
> used as the value. By combining it with the `[+]` option at the end of the 
> key, the line comment is disabled and all characters are used for the value 
> assignment. It is not possible to enter a comment in this line. 

> __Line 6__
> ```
> PARAM-E [?] ;comment
> ``` 
> The value assignment for the key `PARAM-E` is dynamic, and the system 
> properties (VM arguments) of the Java runtime environment are searched for the
> key of the same name, ignoring upper and lower case. The key must be part of 
> the runtime environment or can be set as VM argument (property) at the program
> start in the form `-Dkey=value`. If the system properties of the Java runtime
> environment do not contain a corresponding key, this key is ignored because no
> alternative value has been specified. Comments are supported in this line.

> __Line 8 - 15__
> ```
> [0x53454354494F4E2D41]
>   PARAM-A                 = 0x574552542D31 ;comment
>   0x504152414D2D42        = VALUE-2        ;comment
>   0x504152414D2D43    [+] = 0x574552542D33
>   PARAM-D                 = 0x574552542D34
>                           + 0x574552542D35
>   PARAM-E          [?][+] = 0x574552542D363B20574552542D37
>   0x504152414D2D45 [?]                     ;comment
> ``` 
> Like the examples from lines 1 - 6, the hexadecimal notation is used for
> sections, keys and values.


## Configuration Structure

_Sections of the base configuration_
<table>
  <tr>
    <th>Section</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>[COMMON]</code></td>
    <td>
      General application configuration.
    </td>
  </tr>
  <tr>
    <td><code>[INITIALIZE]</code></td>
    <td>
      Central module configuration. Initialization with the (re)start of the
      service.
    </td>
  </tr>
  <tr>
    <td><code>[REMOTE]</code></td>
    <td>
      Configuration area of the remote control of the server engine.
    </td>
  </tr>
  <tr>
    <td><code>[SERVER]</code></td>
    <td>
      Configuration area of the HTTP server.
    </td>
  </tr>
  <tr>
    <td><code>[VIRTUAL]</code></td>
    <td>
      Configuration area of the virtual hosts for HTTP servers.
    </td>
  </tr>
  <tr>
    <td><code>[STATUSCODES]</code></td>
    <td>
      HTTP server status codes.
    </td>
  </tr>
  <tr>
    <td><code>[MEDIATYPES]</code></td>
    <td>
      Assignment of media and data types.
    </td>
  </tr>
</table>

Further sections can be added as required.


## General Application Configuration
General and globally available options for server engine, servers, hosts and
modules are defined here.
        
_Overview of configuration_
<table>
  <tr>
    <th>Key</th>
    <th>Value</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>CLEANUP</code></td>
    <td><code>ON|OFF</code></td>
    <td>
      Option for intelligent resource cleanup. The Java garbage collector is
      triggered if released resources are expected.
    </td>
  </tr>
  <tr>
    <td><code>RELOAD</code></td>
    <td><code>ON|OFF</code></td>
    <td>
      Option to monitor the configuration file and automatically reload and
      restart it when changes are made.
    </td>
  </tr>
</table>


## Initialisation
The section `[INITIALIZE]` is intended for global modules that are initiated
when the server engine is started and restarted.

> __Scheme for initialization__
> ```
> NAME = MODULE [OPTION] [OPTION] ...
> ```

> __Example of initialization__
> ```
> [INITIALIZE]
>   CONTROL = com.seanox.devwex.control.Connector
>   PHP     = com.seanox.fastcgi.Connector [connection:127.0.0.1:8129]
>   CGI     = com.seanox.fastcgi.Connector [connection:127.0.0.1:8139] [*]
> ```

With the option `[*]` a module can be marked as optional. If the module is not
present during initialization, this option does not cause to an error output.


## Server
Servers are an essential part of the server engine, and it requires at least one
successfully set up server instance to run. During initialization, all sections
that end with `INI` and for which a suitable implementation is searched for in
the class path are taken into account. The class name of the implementation is
the first part of the section name before the first colon.

> __Example of the structure__
> ```
> [CLASS:INI]
>   SCOPE = package.of.class
>   ...
>
> [NAME:INI]
>   SCOPE = package.of.class.Implementation
>   ...
>
> [CLASS:IDENTIFIER:INI]
>   SCOPE = package.of.class
>   ...
>
> [NAME:IDENTIFIER:INI]
>   SCOPE = package.of.class.Implementation
>   ...
> ```

Optionally, the key `SCOPE` can be used to specify the package in which the
class is located or the fully qualified class. Without this key, the default
package `com.seanox.devewx` is used and the class name is derived from the
section name. If no implementation is found for a section ending on `INI`, it
will be ignored without an error message.

> __Example of usage__
> ```
> [CLASS:INI]
>   SCOPE = package.of.class
>   ...
>
> [CLASS:IDENTIFIER:INI]
>   SCOPE = package.of.class
>   ...
> ```


## Remote Control Server
The included remote control supports a Telnet-based access of the server engine
(restart and stop) and queries about the operating status of the running servers
and modules. The server component of the remote control is a typical [servers](
    #server) configuration, except that it is an integral part of the
implementation and uses the fixed name `REMOTE` in the configuration.

> __Example of configuration__
> ```
> [REMOTE:INI]
>   ADDRESS = 127.0.0.1
>   PORT    = 25000
> ```

_Overview of configuration_
<table>
  <tr>
    <th>Key</th>
    <th>Value</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>ADDRESS</code></td>
    <td><code>AUTO|IP|NAME</code></td>
    <td>
      Local address or name of the server in the network. <code>AUTO</code>
      corresponds to <code>0.0.0.0</code> and uses all IP addresses available in
      the system.
    </td>
  </tr>
  <tr>
    <td><code>PORT</code></td>
    <td><code>...</code></td>
    <td>
      Local port of the server.
    </td>
  </tr>
</table>


## HTTP / HTTPS Server
The server for hypertext transfer protocol has many configuration options, which
are divided into different sections, whose order is arbitrary. All the sections
have the same name as the server section, but differ in the section identifier
at the end.

_Overview of sections_
<table>
  <tr>
    <th>Section</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>[SERVER:X:INI]</code></td>
    <td>
      Server configuration, network and runtime behavior
    </td>
  </tr>
  <tr>
    <td><code>[SERVER:X:SSL]</code></td>
    <td>TLS/SSL configuration</td>
  </tr>
  <tr>
    <td><code>[SERVER:X:REF]</code></td>
    <td>Virtual paths</td>
  </tr>
  <tr>
    <td><code>[SERVER:X:ACC]</code></td>
    <td>Access control (Access Control List)</td>
  </tr>
  <tr>
    <td><code>[SERVER:X:CGI]</code></td>
    <td>XCGI/CGI assignment and configuration</td>
  </tr>
  <tr>
    <td><code>[SERVER:X:ENV]</code></td>
    <td>Environment variables</td>
  </tr>
  <tr>
    <td><code>[SERVER:X:FLT]</code></td>
    <td>Filter configuration</td>
  </tr>
</table>


### Server
This section contains the configuration for HTTP servers and their associated
virtual hosts. The configurations for HTTP servers and virtual hosts is the
same, except for the network specific keys: `ADDRESS`, `PORT`, `BACKLOG` and
`MAXACCESS` which are omitted for virtual hosts, since they are configured only
server specific.

The server configuration consists of two parts: Network connection and HTTP. For
virtual hosts, the configuration of the network connection is not required.

_Overview of server configuration_
<table>
  <thead>
    <tr>
      <th>Key</th>
      <th>Value</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>ADDRESS</code></td>
      <td><code>AUTO|IP|NAME</code></td>
      <td>
        Local address or name of the server in the network. <code>AUTO</code>
        corresponds to <code>0.0.0.0</code> and uses all IP addresses available
        in the system.
      </td>
    </tr>
    <tr>
      <td><code>BACKLOG</code></td>
      <td><code>500</code></td>
      <td>
        Maximum number of disconnected connections when the number of
        simultaneous connections is exhausted.
      </td>
    </tr>
    <tr>
      <td><code>ISOLATION</code></td>
      <td><code>300000</code></td>
      <td>
        Maximum runtime of XCGI/CGI processes in milliseconds. If exceeded, the
        process is terminated by the server. The value <code>0</code> or less
        ignores the runtime limit.
      </td>
    </tr>
    <tr>
      <td><code>MAXACCESS</code></td>
      <td><code>100</code></td>
      <td>
        Maximum number of simultaneous connections.
      </td>
    </tr>
    <tr>
      <td><code>PORT</code></td>
      <td><code>...</code></td>
      <td>
        Local port of the server.
      </td>
    </tr>
  </tbody>
</table>

_Overview of HTTP configuration_
<table>
  <thead>
    <tr>
      <th>Key</th>
      <th>Value</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>ACCESSLOG</code></td>
      <td><code>... &gt; ../system/access.log</code></td>
      <td>
        Format and path of the log file for logging accesses. Without path the
        Standard I/O is used, <code>OFF</code> disables logging. Format and path
        supports a print formatted syntax, with which CGI environment variables
        in the format <code>%[...]</code>> and symbols of date and time in the
        format <code>%t...</code> can be used. The date/time symbols are based
        on the <a target="_extern"
            href="https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#dt">
        Date/Time Conversions</a> of the formatter class.
      </td>
    </tr>
    <tr>
      <td><code>BLOCKSIZE</code></td>
      <td><code>65535</code></td>
      <td>
        Maximum size of the data blocks during data transfer in bytes.
      </td>
    </tr>
    <tr>
      <td><code>DEFAULT</code></td>
      <td><code>index.htm index.html ...</code></td>
      <td>
        A list of standard documents, separated by spaces, to be displayed when
        directories are called, using the first existing document in the list,
        if none of the listed documents exist, the contents of the directory
        will be displayed if this is allowed.
      </td>
    </tr>
    <tr>
      <td><code>DOCROOT</code></td>
      <td><code>../documents</code></td>
      <td>
        Path from the document directory.
      </td>
    </tr>
    <tr>
      <td><code>IDENTITY</code></td>
      <td><code>ON|OFF</code></td>
      <td>
        Option to transfer the server name with the XCGI/CGI environment
        variable and the HTTP response.
      </td>
    </tr>
    <tr>
      <td><code>INDEX</code></td>
      <td><code>ON|OFF</code></td>
      <td>
        Option for displaying directories as a navigable list view (Directory
        Listing). With the addition <code>[S]</code>> hidden entries of the file
        system can be suppressed for display.
      </td>
    </tr>
    <tr>
      <td><code>INTERRUPT</code></td>
      <td><code>10</code></td>
      <td>
        Interruption for system processes in milliseconds. This setting
        minimizes the CPU utilization of load-intensive processes, in which they
        do without a portion of their execution time and thus leave time
        slots/time slices to other processes. 
      </td>
    </tr>
    <tr>
      <td><code>MEDIATYPE</code></td>
      <td><code>application/octet-stream</code></td>
      <td>
        Standard media type that is used when the requested media type is not
        included in the list of media types (section <code>[MEDIATYPES]</code>).
      </td>
    </tr>
    <tr>
      <td><code>METHODS</code></td>
      <td><code>GET POST HEAD OPTIONS PUT DELETE ...</code></td>
      <td>
        List of methods that the server is allowed to process, separated by
        spaces.
      </td>
    </tr>
    <tr>
      <td><code>SYSROOT</code></td>
      <td><code>../system</code></td>
      <td>
        Path from the directory of the system files.
      </td>
    </tr>
    <tr>
      <td><code>TIMEOUT</code></td>
      <td><code>30000</code></td>
      <td>
        Maximum idle time of inbound sockets and streams and maximum time for
        outbound blocked streams. Exceeding it will terminate the request by
        closing the socket. A value of <code>0</code> or less ignores the
        timeout. 
      </td>
    </tr>
  </tbody>
</table>

### Transport Layer Security / Secure Socket Layer
Transport Layer Security (TLS) and Secure Socket Layer (SSL) are supported for
secure data transmission. Both are part of the connection setup with Java and
use certificates that are assigned to each host individually.

Java uses Keystores and Truststores for the management of key and certificates.
Keystore, for private keys and certificates, and the Truststore, for CA
certificates of trusted certification authorities.

__With Seanox Devwex, this distinction is no longer necessary and so only one
keystore is used for both. The use of aliases in the keystore is possible, but
is not supported, because certificates are always determined based on the host.__

_Overview of configuration_
<table>
  <tr>
    <th>Key</th>
    <th>Value</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>PROTOCOL</code></td>
    <td>
      <code> 
        TLS|TLSv1|TLSv1.1|TLSv1.2|...<br>
        SSL|SSLv2|SSLv3|...
      </code>
    </td>
    <td>
      Protocol: <code>TLS</code> (Transport Layer Security), <code>SSL</code>
      (Secure Socket Layer). Default value: <code>TLS</code> (if not specified)
    </td>
  </tr>
  <tr>
    <td><code>CLIENTAUTH</code></td>
    <td><code>OFF|ON|AUTO</code></td>
    <td>
      (De)activation of the client authorization. <code>ON</code> requires
      mandatory client authorization, without which the connection to the client
      is terminated. <code>AUTO</code> requires an optional client authorization
      and leaves the authorization decision to the client and the server
      establishes the connection even if the client does not provide
      authorization. Default value: <code>OFF</code> (if not specified)
    </td>
  </tr>
  <tr>
    <td><code>KEYSTORE</code></td>
    <td><code>...</code></td>
    <td>
      Path of the keystore file
    </td>
  </tr>
  <tr>
    <td><code>TYPE</code></td>
    <td><code>JCEKS|JKS|DKS|PKCS11|PKCS12|...</code></td>
    <td>
      Type of used keystore. Default value from the JDK: <code>PKCS12</code> (if
      not specified)
    </td>
  </tr>
  <tr>
    <td><code>ALGORITHM</code></td>
    <td><code>SunX509|PKIX|...</code></td>
    <td>
      Algorithm of encryption from keystore. Default value from the JDK:
      <code>SunX509</code> / <code>PKIX</code> (if not specified)
    </td>
  </tr>
  <tr>
    <td><code>PASSWORD</code></td>
    <td><code>...</code></td>
    <td>
      Password for the keystore.
    </td>
  </tr>
</table>

HTTPS uses  by default port 443 and for the keystore the working directory
`./devwex/program` which can be customized.

```
[SERVER:X:INI]
  PORT       = 443
  ...

[SERVER:X:SSL]
  PROTOCOL   = TLS
  CLIENTAUTH = OFF

  KEYSTORE   = keystore
  TYPE       = PKCS12
  ALGORITHM  = SunX509
  PASSWORD   = changeIt
  ...
```

Due to the automatically assigned default values, the following configuration is
usually sufficient:

```
[SERVER:X:INI]
  PORT       = 443
  ...

[SERVER:X:SSL]
  KEYSTORE   = keystore
  PASSWORD   = changeIt
```

To create and manage keys and certificates, the JRE contains the Keytool
utility. In the following, the creation of self-created server certificates is
described. For details on the utility program, please refer to the documentation
of the JRE used.

> __Example of how to create a server certificate in the keystore__
> ```
> ./java/bin/keytool -genkey -keyalg RSA -keysize 2048 -validity 365
>     -alias Server -dname "CN=127.0.0.1"
>     -keystore keystore -keypass changeIt -storepass changeIt
> ```

If no target directory is specified with `-keystore` in the Keytool, the
generated certificate is stored as `.keystore` file in the user directory.
depending on the operating system, different user directories are used, e.g.
with Windows this can be `C:\Users\<user>\.keystore` or with Unix-based
system `/home/<user>/.keystore`. The created keystore file can be renamed
arbitrarily and moved in the file system.


### Client Authentication / Mutual Authentication
This type of authorization, which is activated via `CLIENTAUTH` is based on
certificates. server and client negotiate the exchange of certificates during
connection setup, accepting only certificates known to them. The behavior of the
server and the establishment of the connection can be set up here by `ON`
strictly and with `AUTO` tolerant. In case of strict conduct, the transmission
of a valid certificate is required. Without it, the connection is closed without
a response from the server. In tolerant behavior, the connection is always
established. In this case, it is up to the server configuration how the server
behaves when no or no appropriate certificate is transmitted. Without further
server configuration, the behaviour corresponds to the configuration
`CLIENTAUTH = OFF`. However, the server can react to missing or unsuitable
certificates via filters. If a client authenticates itself with a known
certificate, the environment variable `AUTH_CERT` is provided with the
certificate attributes. If the environment variable `AUTH_CERT` is not set or
empty, a request without an appropriate certificate can be assumed and an error
page can be sent to the client. 

> __Example of a filter for checking for valid certificates__
> ```
> [SERVER:X:SSL]
>   ...
>   CLIENTAUTH  = AUTO
>   ...
> [SERVER:X:FLT]
>   CLIENT-AUTH = ALL IS EMPTY AUTH_CERT
> ```

> __Example to create server and client certificates__
> ```
> ./java/bin/keytool -genkeypair -alias Server -dname 'CN=Server' -validity 365
>     -keystore server.keystore -keyalg RSA -keysize 2048 -keypass changeIt
>     -storetype PKCS12 -storepass changeIt
> ```
> ```
> ./java/bin/keytool -exportcert -alias Server -file server.cer
>     -keystore server.keystore -storepass changeIt
> ```

The keystore for the client is created and the public client certificate is
exported. Both are temporary in this example and will be needed later on for the
creation of the server keystore and the export of the client certificate.

> __Example for the export__
> ```
> ./java/bin/keytool -genkeypair -alias Client -dname 'CN=Client' -validity 365
>     -keystore client.keystore -keyalg RSA -keysize 2048 -keypass changeIt
>     -storetype PKCS12 -storepass changeIt
> ```
> ```
> ./java/bin/keytool -exportcert -alias Client -file client.cer
>     -keystore client.keystore -storepass changeIt
> ```

The keystore of the server and the client are migrated into each other, because
the server only requires one keystore.

> ```
> ./java/bin/keytool -importcert -alias Client -file client.cer
>     -keystore server.keystore -storepass changeIt
>     -noprompt
> ```
> ```
> ./java/bin/keytool -importcert -alias Server -file server.cer
>     -keystore client.keystore -storepass changeIt
>     -noprompt
> ```

Finally, the server and client certificates are exported as `PKCS12`. The file
`client.p12` is needed later for the client to authorize the server and the
server certificate can be ignored.

> ```
> ./java/bin/keytool -importkeystore
>     -srckeystore server.keystore -srcstorepass changeIt
>     -destkeystore server.p12 -deststoretype PKCS12
>     -keypass changeIt -storepass changeIt
>     -noprompt
> ```
> ```
> ./java/bin/keytool -importkeystore -alias ClientA
>     -srckeystore client.keystore -srcstorepass changeIt
>     -destkeystore client.p12 -deststoretype PKCS12
>     -keypass changeIt -storepass changeIt
>     -noprompt
> ```


### Virtual Hosts / Virtual Hosting
With virtual hosting, a server processes requests for different host names,
domains or DNS aliases. For this purpose, the server uses the network
configuration of a physical host and then applies the configuration to the
requested host.

> __Example of a request for a virtual host__
> ```
> GET /directory/file.cgi?value=123 HTTP/1.1
> Accept-Encoding: gzip, deflate
> Accept-Language: de
> Accept: */*
> User-Agent: Browser
> Host: example.local
> ```

> __The sections of the configuration derived from the virtual host for the
> above example.__ 
> ```
> [VIRTUAL:EXAMPLE.LOCAL:INI]
>   ...
> [VIRTUAL:EXAMPLE.LOCAL:FLT]
>   ...
> [VIRTUAL:EXAMPLE.LOCAL:REF]
>   ...
> [VIRTUAL:EXAMPLE.LOCAL:ACC]
>   ...
> [VIRTUAL:EXAMPLE.LOCAL:ENV]
>   ...
> [VIRTUAL:EXAMPLE.LOCAL:CGI]
>   ...
> ```

The configuration of the virtual hosts is the same as for HTTP servers, whereby
the specific keys for network connections: `ADDRESS`, `PORT`, `BACKLOG` and
`MAXACCESS` are omitted. Virtual hosts are independent configurations that can
be used by all servers. Using the key `SERVER` in section `[VIRTUAL:INI]` the
use of the configuration can be restricted to certain servers.

> __Example of restriction to specific servers__
> ```
> [VIRTUAL:X:INI]
>   SERVER = SERVER:A SERVER:B SERVER:C
>   ...
> ```

### Filters
Access to servers and virtual hosts can be controlled by individual rules,
whereby incoming requests are checked against these rules before processing.
Filters support individual error pages, automatic redirects and modules.

> __Scheme for filters__
> ```
> NAME = METHOD CONDITION FUNCTION VARIABLE VALUE [+] ... > REFERENCE [R]
> ```
        
Filters can use all CGI environment variables, which includes the system
environment variables. Variables and values are case-insensitive and the values
are in transmitted raw format. The specification of a reference is optional.

_Overview of elements of the filter syntax_
<table>
  <tr>
    <th>Name</th>
    <th>Value</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>NAME</code></td>
    <td><code>...</code></td>
    <td>freely selectable</td>
  </tr>
  <tr>
    <td><code>METHOD</code></td>
    <td><code>GET|POST|PUT|ALL|...</code></td>
    <td>HTTP method to be reacted to</td>
  </tr>
  <tr>
    <td><code>CONDITION</code></td>
    <td><code>IS|NOT|ALWAYS</code></td>
    <td>stipulation</td>
  </tr>
  <tr>
    <td></td>
    <td><code>IS</code></td>
    <td>The filter responds when the condition is fulfilled.</td>
  </tr>
  <tr>
    <td></td>
    <td><code>NOT</code></td>
    <td>
      The filter responds if the condition is not fulfilled.
    </td>
  </tr>
  <tr>
    <td></td>
    <td><code>ALWAYS</code></td>
    <td>
      The filter always responds. The specification of function and value is not
      required for this condition.
    </td>
  </tr>
  <tr>
    <td><code>FUNCTION</code></td>
    <td><code>STARTS|EQUALS|CONTAINS|ENDS|EMPTY</code></td>
    <td>Type of comparison</td>
  </tr>
  <tr>
    <td></td>
    <td><code>STARTS</code></td>
    <td>The value of the variable starts with the comparison value.</td>
  </tr>
  <tr>
    <td></td>
    <td><code>EQUALS</code></td>
    <td>
      The value of the variable corresponds to the comparison value.
    </td>
  </tr>
  <tr>
    <td></td>
    <td><code>CONTAINS</code></td>
    <td>The value of the variable contains the comparison value.</td>
  </tr>
  <tr>
    <td></td>
    <td><code>ENDS</code></td>
    <td>The value of the variable ends with the comparison value.</td>
  </tr>
  <tr>
    <td></td>
    <td><code>EMPTY</code></td>
    <td>
      The value of the variables is empty or does not exist. The specification
      of function and value is not required for this condition.
    </td>
  </tr>
  <tr>
    <td></td>
    <td><code>MATCH</code></td>
    <td>
      The value of the variable corresponds to the comparison value as a regular
      expression.
    </td>
  </tr>
  <tr>
    <td><code>VARIABLE</code></td>
    <td><code>...</code></td>
    <td>
      All environment variables / CGI variables are available to the filter.
    </td>
  </tr>
  <tr>
    <td><code>VALUE</code></td>
    <td><code>...</code></td>
    <td>comparative value</td>
  </tr>
  <tr>
    <td><code>OPTION</code></td>
    <td><code>[+] [R] [M]</code></td>
    <td>Additional filter control options</td>
  </tr>
  <tr>
    <td></td>
    <td><code>[+]</code></td>
    <td>Logical AND operation of several conditions.</td>
  </tr>
  <tr>
    <td></td>
    <td><code>[R]</code></td>
    <td>Reference for forwarding (redirect).</td>
  </tr>
  <tr>
    <td></td>
    <td><code>[M]</code></td>
    <td>
      Reference to the use of an HTTP module. Addressing the module is not a
      final step, but terminating the filter sequence only occurs if a module
      changes the response status and/or sends data to the client.
    </td>
  </tr>
</table>

> __Example of a request__
> ```
> GET /directory/file.cgi?value=123 HTTP/1.1
> Host: www.xxx.zzz
> Accept-Encoding: gzip, deflate
> Accept-Language: de
> Accept: */*
> User-Agent: Browser
> ```
        
The environment variables / CGI variables and values depend on the request
header and the request body is not read out for filters, which means that data
and parameters such as those passed in the body for the HTTP method `POST` are
not available.

_Overview of variables (selection)_
<table>
  <tr>
    <th>Parameter</th>
    <th>CGI Variable</th>
    <th>Value</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>Request</code></td>
    <td><code>REQUEST_METHOD</code></td>
    <td><code>GET</code></td>
    <td>HTTP request methods</td>
  </tr>
  <tr>
    <td><code>Request</code></td>
    <td><code>REQUEST_URI</code></td>
    <td><code>/directory/file.cgi?value=123</code></td>
    <td>Path of HTTP request including passed parameters</td>
  </tr>
  <tr>
    <td><code>Request</code></td>
    <td><code>QUERY_STRING</code></td>
    <td><code>value=123</code></td>
    <td>List of parameters passed with the request URI</td>
  </tr>
  <tr>
    <td><code>Host</code></td>
    <td><code>HTTP_HOST</code></td>
    <td><code>www.xxx.zzz</code></td>
    <td>
      Domain name or IP address of the target address requested by the client
    </td>
  </tr>
  <tr>
    <td><code>Accept-Encoding</code></td>
    <td><code>HTTP_ACCEPT_ENCODING</code></td>
    <td><code>gzip, deflate</code></td>
    <td>List of encoding methods supported by the client</td>
  </tr>
  <tr>
    <td><code>Accept-Language</code></td>
    <td><code>HTTP_ACCEPT_LANGUAGE</code></td>
    <td><code>en</code></td>
    <td>List of languages supported by the client</td>
  </tr>
  <tr>
    <td><code>Accept</code></td>
    <td><code>HTTP_ACCEPT</code></td>
    <td><code>*/*</code></td>
    <td>List of media types supported by the client</td>
  </tr>
  <tr>
    <td><code>User-Agent</code></td>
    <td><code>HTTP_USER_AGENT</code></td>
    <td><code>Browser</code></td>
    <td>Client product and version information</td>
  </tr>
</table>

_Examples of filters_
> ```
> FILTER-A = GET NOT EQUALS HTTP_ACCEPT_LANGUAGE EN
> ```
> The method `GET` is denied if the request header parameter `Accept-Language`
> does not match `EN`.
          
> ```
> FILTER-B = GET IS EQUALS HTTP_ACCEPT_LANGUAGE EN
> ```
> The method `GET` is denied if parameter `Accept-Language` in the request
> header corresponds to value `EN`.
          
> ```
> FILTER-C = GET IS CONTAINS HTTP_ACCEPT_LANGUAGE EN
> ```
> The method `GET` is denied if parameter `Accept-Language` contains the value
> `EN` in the request header.

> ```
> FILTER-D = GET IS STARTS REMOTE_ADDR 192.168.
> ```
> The method `GET` is denied when the CGI variable `REMOTE_ADDR` starts with
> `192.168.`.

> ```
> FILTER-E = GET IS ENDS SCRIPT_URL .dat
> ```
> The method `GET` is denied when the CGI variable `SCRIPT_URL` ends with
> `.dat`.

> ```
> FILTER-F = GET IS MATCH REQUEST_URI \.do$
> ```
> The method `GET` is denied if the CGI variable `SCRIPT_URL` corresponds to the
> regular expression `(?i).do$` and thus ends in `.do`.

> ```
> FILTER-G = GET IS CONTAINS REQUEST_URI /documents [+] GET IS EMPTY HTTP_REFERER
> ```
> The method `GET` is denied if the CGI variable `REQUEST_URI` contains the
> value `/documents` and the request parameter `REFERER` is empty.

> ```
> FILTER-H = GET IS CONTAINS REQUEST_URI /private > ../system/status-403.html
> ```
> The method `GET` is denied if the CGI variable `REQUEST_URI` contains the
> value `/private`, in which case the content of the file
> `../system/status-403.html` is displayed.

> ```
> FILTER-I = GET IS CONTAINS REQUEST_URI /private > http://www.xxx.zzz/403.php [R]
> ```
> The method `GET` is denied if the CGI variable `REQUEST_URI` contains the
> value `/private`, in which case it is forwarded to the address
> `http://www.xxx.zzz/403.php`.

> ```
> FILTER-J = GET IS ENDS REQUEST_URI .do > example.Connector [A:...] [B:...] [M]
> ```
> The method `GET` is denied when the CGI variable `REQUEST_URI` ends with th
> value `.do`. In this case the module `Connector` is called from package
> `example`, which must be in the class path of the server,  with the additional
> parameters `A` and `B`.

> ```
> FILTER-K = ALL ALWAYS > example.Connector [A:...] [B:...] [M]
> ```
> In all requests, the module `Connector` from package `example`, which must be
> in the class path of the server, is called with the additional parameters `A`
> and `B`.


### Virtual Paths (Aliasing and Redirection)
Virtual paths are an alias for physical elements in a file system that can be
used to build new file and directory structures or modify existing ones,
regardless of the file system; in addition to the file system, modules and
redirections are also supported as targets.

> [!IMPORTANT]
> If the target is a relative path in the local file system, the path always
> refers to the working directory of the server.

> __Scheme for virtual paths__
> ```
> NAME = VIRTUAL PATH > TARGET [OPTION]
> ```
> The specification of a target is mandatory only for modules and redirections.
> Real paths to which only additional options are assigned can be used without
> targets.

> __Example__
> ```
> EXAMPLE-LONG  = /directory/file.txt > ./documents/directory/file.txt [C]
> EXAMPLE-SHORT = /directory/file.txt [C]
> ```

_Overview of options_
<table>
  <tr>
    <th>Option</th>
    <th>Role</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>[A]</code></td>
    <td>Absolute</td>
    <td>
      Defines a virtual sub-path that points to physical directories or files;
      if the path of a request starts with this virtual sub-path, the resource
      defined in this way is used; if a script or module is referenced, it can
      evaluate and apply the additional path information. If other more
      qualified paths exist for an absolute path, these are used. Absolute paths
      cover a wide range of paths, but this can be explicitly suppressed in
      detail.
    </td>
  </tr>
  <tr>
    <td><code>[C]</code></td>
    <td>Forbidden</td>
    <td>
      This option blocks access to virtual and physical paths, and locks
      directories primarily affect the directory itself and all subdirectories.
    </td>
  </tr>
  <tr>
    <td><code>[R]</code></td>
    <td>Redirect</td>
    <td>
      This option sets up automatic forwarding to a specified address for
      virtual and physical directories and files.
    </td>
  </tr>
  <tr>
    <td><code>[M]</code></td>
    <td>Module</td>
    <td>
      If the path of a request starts with this virtual sub-path, the module
      defined in this way is used, which integrates HTTP modules as a web
      application, and modules can evaluate and apply the additional path
      information.
    </td>
  </tr>
  <tr>
    <td><code>[X]</code></td>
    <td>Method Extension</td>
    <td>
      Only in conjunction with option <code>[M]</code>, the additional option
      <code>[X]</code> opens the constraint by <code>[SERVER:INI] METHODS</code>,
      so that all requested HTTP methods are passed to a module via the virtual
      path.
    </td>
  </tr>
  <tr>
    <td><code>[D]</code></td>
    <td>Digest Access Authentication</td>
    <td>
      Only in conjunction with the option <code>[ACC]</code>, the additional
      option <code>[D]</code> activates the use of Digest Access Authentication.
    </td>
  </tr>
  <tr>
    <td><code>[ACC:...]</code></td>
    <td>Authentication</td>
    <td>
      Optionally, Digest Access Authentication can be activated in combination
      with the option <code>[D]</code> Digest Access Authentication. In both
      cases, authorization against the Access Control List is performed in
      section <code>[SERVER:ACC]</code> / <code>[VIRTUAL:ACC]</code>.
    </td>
  </tr>
</table>

_Examples of using virtual paths_
> ```
> DIRECTION-A = /system > ../system
> ```
> The physical directory `../system` is used for queries  with the path
> `/system`.

> ```
> DIRECTION-A = /system/test.xml > ../system/test.php
> ```
> Physical `../system/test.php` is used for requests with the path
> `/system/test.xml`.

> ```
> DIRECTION-B = /doc >; ../documents [A]
> ```
> The path `/doc` has been defined as absolute, so for all requests starting
> with `/doc` like `/documents`, `/documentation` or `/doc/test.cgi?cmd=123`,
> then the physical directory `../documents` is used.

> ```
> DIRECTION-C = /test > http://www.xxx.zzz [R]
> ```
> Requests from path `/test` will be responded with a forward to
> `http://www.xxx.zzz`.

> ```
> DIRECTION-D = /control > example.Connector [M]
> ```
> Path `/control` refers to the module class `Connector` from package `example`
> which must be in the class path of the server.

> ```
> DIRECTION-E = /program [C]
> ```
> Access to the directory `/program` is prohibited.


### Basic Access Authentication / Digest Access Authentication
Basic Access Authentication (BAA) and Digest Access Authentication (DAA) are two
of the different methods by which a user can authenticate himself or herself
against a web server or a web application, whereby the use of Digest Access
Authentication is recommended here, because here checksums are transmitted
instead of credentials, which is intended to protect against the reconstruction
of credentials.          

The configuration of the Basic / Digest Access Authentication consists of a
virtual or physical path in the section `[SERVER:REF]` or `[VIRTUAL:REF]` in
combination with the option `[acc:...]` as reference to the permissions and
optionally with the options `[realm:...]` for the description of the area and
`[D]` for the use of Digest Access Authentication. With `[acc:none]`, the parent
authorization for the specified path and all subordinate paths can be cancelled
and, if necessary, further authorizations in deeper sub-directories can be used
again. The permissions are defined in the independent section `[SERVER:ACC]` or
`[VIRTUAL:ACC]` and consist of one or more groups with individual permissions,
consisting of user and password. 

> __Examples of basic access authentication__
> ```
> [SERVER:X:REF]
>   ACCESS-A = /access                     [acc:group-a] [realm:Section-A]
>   ACCESS-B = /access/section             [acc:group-b] [realm:Section-B]
>   ACCESS-C = /access/section/protected   [acc:group-c] [realm:Section-C]
>   ACCESS-N = /access/public              [acc:none]
> 
>   ACCESS-X = /access/example... > ... [acc:...] [acc:...] ... [realm:...] [...
> ```

> __Examples of Digest-Access-Authentication: The option `[D]` makes the
> difference__
> ```
> [SERVER:X:REF]
>   ACCESS-A = /access                     [acc:group-a] [realm:Section-A] [D]
>   ACCESS-B = /access/section             [acc:group-b] [realm:Section-B] [D]
>   ACCESS-C = /access/section/protected   [acc:group-c] [realm:Section-C] [D]
>   ACCESS-N = /access/public              [acc:none]
>
>   ACCESS-X = /access/example... > ... [acc:...] [acc:...] ... [realm:...] [D] [...
> ```

> __Examples for the definition of access data__
> ```
> [SERVER:X:ACC]
>   GROUP-A = ua:pa ub:pb uc:pc
>   GROUP-B = ua:pa ub:pbb
>   GROUP-C = uc:pcc
>
>   GROUP-X = user:password user:password ...
> ```

The directory `/access` with all subdirectories is only accessible to
authorized users and with password, except for the directory `/access/public`
and its subdirectories.

> __ACCESS-A__  
> The directory `/access` can only be accessed by the users `ua` and the
> password `pa`, `ub` and the password `pb` and `uc` with the password `pc`.
> Also access to all subdirectories, with the exception of `/access/section`
> and `/access/section/protected`, is only possible for these users.

> __ACCESS-B__  
> The directory `/access/section` with all subdirectories can be used by users
> `ua` with the password `pa` and `ub` with the password `pbb`.

> __ACCESS-C__  
> Access to the directory `/access/section/protected` is only possible for the
> user `uc` with the password `pcc`.

> __ACCESS-N__  
> For the directory `/access/public` the basic / digest access authentication is
> removed and can be used without login and password and subdirectories can be
> provided with basic access authentication again.

> [!TIP]
> Users and passwords are case-sensitive, using colons and spaces is not
> possible.

> [!TIP]
> Basic / Digest Access Authentication can be used except for files, but some
> browsers will ask you again at directory level.


### Directory Listing
The content of directory listings is based on the template file
`./devwex/system/index.html`. Through the configuration of `SYSROOT`, the
directory and thus the template can be individually defined for servers and
virtual hosts. The template is pure HTML combined with placeholders for the
template generator. The syntax of the placeholders `#[...]` is case-insensitive,
must begin with a letter and is limited to the following characters: `a-z A-Z
    0-9 _-`. Placeholders can be used for single values and segments. Segments
are partial structures that can be nested. The placeholders of segments are
retained after filling and can be reused iteratively. As values for segments,
collections and maps are expected. Both create deep, complex and possibly
repetitive recursive structures.

_Structure and description of the placeholders_
<table>
  <tr>
    <th>Syntax</th>
    <th>Description</th>
  </tr>        
  <tr>
    <td nowrap="nowrap">
      <code>#[value]</code>
    </td>
    <td>
      Inserts the value for `value` and removes the placeholder.
    </td>
  </tr>
  <tr>
    <td nowrap="nowrap">
      <code>#[segment[[...]]]</code>
    </td>
    <td>
      Defines a segment. The nesting and use of further segments is possible.
      Because the placeholders of segments are retained, they can be used to
      generate lists.
    </td>
  </tr>
  <tr>
    <td nowrap="nowrap">
      <code>#[0x0A]</code><br>
      <code>#[0x4578616D706C6521]</code>
    </td>
    <td>
      Escaping one or more characters. The conversion only takes place when the
      template is generated.
    </td>
  </tr>
</table>

_The following placeholders are available for the directory listing_
<table>
  <tr>
    <th>Placeholder</th>
    <th>Description</th>
  </tr>        
  <tr>
    <td nowrap="nowrap">
      <code>#[location[[</code><br>
      <code>&nbsp;&nbsp;#[path]</code><br>
      <code>&nbsp;&nbsp;#[name]</code><br>
      <code>]]]</code>
    </td>
    <td>
      Iteration over the components path and name of the location (URL).
    </td>
  </tr>
  <tr>
    <td nowrap="nowrap">
      <code>#[sort]</code>
    </td>
    <td>
      Indicator of sorting, which can be used for CSS, among other things.
    </td>
  </tr>
  <tr>
    <td nowrap="nowrap">
      <code>#[file[[</code><br>
      <code>&nbsp;&nbsp;#[case]</code><br>
      <code>&nbsp;&nbsp;#[mime]</code><br>
      <code>&nbsp;&nbsp;#[name]</code><br>
      <code>&nbsp;&nbsp;#[type]</code><br>
      <code>&nbsp;&nbsp;#[size]</code><br>
      <code>&nbsp;&nbsp;#[date]</code><br>
      <code>]]]</code>
    </td>
    <td>
      Iteration over the file list.
    </td>
  </tr>
  <tr>
    <td nowrap="nowrap">
      <code>#[HTTP_...]</code>
    </td>
    <td>
      Placeholder for the environment variables / CGI variables which are
      available with the request
    </td>
  </tr>          
</table>                


### Environmental Variables
With environment variables, the operating system and runtime environment provide
an application with essential and additional system and runtime information. As
an extension of the environment variables from the operating system and the
runtime environment, additional environment variables required for the CGI and
XCGI are defined in this section.

> __Scheme for environment variables__
> ```
> VARIABLE = VALUE
> ```

> __Example for setting up an environment variable__
> ```
> WEBSERVER = DEVWEX
> ```
> The environment variable `WEBSERVER` is defined with the value `DEVWEX`.

> [!NOTE]
> XCGI/CGI applications determine system and runtime information about the
> environment variables, so essential environment variables such as
> `SYSTEMDRIVE`, `SYSTEMROOT` and `PATH` should be set in the configuration
> file `devwex.ini`.

> [!TIP]
> The option `[?]` at the end of a key identifies it as dynamic by specifying
> the key with its value at program startup as a VM argument or in the
> environment variables, system variables such as `PATH` and for Windows
> `SYSTEMDRIVE` and `SYSTEMROOT` can be passed by this way, see section
> [Configuration File](#configuration-file) for more information.


### Common Gateway Interface
For data exchange as well as for connecting external runtime environments and
applications, the specification 1.1 of the Common Gateway Interface and thus
PHP, Perl, Python and others are supported. FastCGI is also available as an
option.

With the XCGI, another interface similar to the CGI is available. The basic
principle and method of operation are the same as for CGI. The XCGI also
communicates via the standard I/O and the server-relevant information is
transmitted as environment variables. However, the form of transmission differs.
The environment variables for the CGI are provided via the runtime environment
and for the XCGI via the data stream. Similar to the CGI, the XCGI transfers the
request body via standard I/O. In the XCGI, the environment variables precede
the request body. The structure is similar to that of the HTTP request. The data
stream begins line by line with the environment variables, followed by the 
character string `[CRLFCRLF]` and then the request body.

> Example of an XCGI request
> ```
> SERVER_PORT=80
> SERVER_PROTOCOL=HTTP/1.0
> CONTENT_LENGTH=25
> CONTENT_TYPE=application/x-www-form-urlencoded
> REQUEST=POST /example.xcgi HTTP/1.1
> REQUEST_METHOD=POST
> REMOTE_ADDR=127.0.0.1
> REMOTE_PORT=64638
> UNIQUE_ID=MHU014XLD048AN1A
> SCRIPT_NAME=/example.xcgi
> SCRIPT_URL=/example.xcgi
> HTTP_HOST=127.0.0.1
> HTTP_USER_AGENT=Mozilla/5.0
> HTTP_CONTENT_TYPE=application/x-www-form-urlencoded
> HTTP_CONTENT_LENGTH=25
> SCRIPT_URI=http://127.0.0.1/example.xcgi
> SCRIPT_FILENAME=c:/xcgi/example.xcgi
> PATH_TRANSLATED=c:/xcgi/example.xcgi
> REQUEST_URI=/example.xcgi
> GATEWAY_INTERFACE=CGI/1.1
>
> Content of Request(-Body)
> ```

The advantage of the XCGI is the simple transmission of the environment
variables, which means that applications can also be used that do not have an
exclusive environment or do not have access to the environment variables of the
operating system.

> __Scheme for Common Gateway Interface__
> ```
> FILE EXTENSION = METHODS > APPLICATION [OPTION]
> ```

_Overview of options_
<table>
  <tr>
    <th>Option</th>
    <th>Example Value</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>[C]</code></td>
    <td><code>.../application/test.cgi</code></td>
    <td>full path incl. filename and file extension</td>
  </tr>
  <tr>
    <td><code>[D]</code></td>
    <td><code>.../application</code>/</td>
    <td>directory</td>
  </tr>
  <tr>
    <td><code>[N]</code></td>
    <td><code>test</code></td>
    <td>File name without extension</td>
  </tr>
  <tr>
    <td><code>[X]</code></td>
    <td></td>
    <td>Using the XCGI</td>
  </tr>
  <tr>
    <td><code>[M]</code></td>
    <td></td>
    <td>Use as module</td>
  </tr>
</table>

_Examples of Common Gateway Interface setup_
> ```
> CGI = POST GET > c:/cgi/bin/example.exe
> ```
> The file extension `cgi` is assigned to the application _example.exe_, which
> is started in case of requests with the file extension `cgi` in the path: The
> path to the script file is passed here via the environment variables
> `SCRIPT_FILENAME` and `PATH_TRANSLATED`.

> ```
> CGI = POST GET > c:/cgi/bin/example.exe [C]
> ```
> The file extension `cgi` is assigned to the application _example.exe_, which
> is started with the file extension `cgi` in the path, where the path to the
> script file is passed to the application as a start argument.

> ```
> CGI = POST GET > c:/cgi/bin/example.exe [X]
> ```
> The file extension `cgi` is assigned to the application _example.exe_, which
> is started in case of requests with the file extension `cgi` in the path using
> XCGI. The path to the script file is also passed here via the environment
> variables `SCRIPT_FILENAME` and `PATH_TRANSLATED`.

> __Example of using PHP, Perl, Java and native web applications__
> ```
> PHP = POST GET > c:/php/php.exe
> ```
> The file extension `php` is assigned php.exe as a CGI application.
>
> ```
> CGI = POST GET > c:/perl/bin/perl.exe [C]
> ```
> The file extension `cgi` is assigned to perl.exe as a CGI application.
>
> ```
> JAR = POST GET > java -jar [C] [X]
> ```
> The file extension `jar` is assigned to java.exe as an XCGI application.
> 
> ```
> EXE = POST GET > [C]
> ```
> With the file extension `exe` each Windows application runs as a CGI
> application.
> 
> ```
> EXE = POST GET > [C] [X]
> ```
> With the file extension `exe` each Windows application runs as an XCGI
> application.
> 
> ```
> CMD = POST GET > [C]
> ```
> The file extension `cmd` executes each batch script as a CGI application.

In addition to XCGI and CGI, modules can also be set up via the gateway. Unlike
XCGI and CGI, modules are not loaded as external applications, but are executed
directly in the runtime environment of the server via the Module API (XAPI),
which provides very fast access times and direct access to the components from
the server and the server engine.

_Example of how to set up modules_
> ```
> SSX = POST GET > com.seanox.ssx.Connector [M]
> ```
> The file extension `ssx` is assigned to module `com.seanox.ssx.Connector` in
> the class path.

_Overview of environment variables (selection)_
<table>
  <tr>
    <th>Environment Variable</th>
    <th>Example Value</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>CONTENT_LENGTH</code></td>
    <td><code>1000</code></td>
    <td>Length of the transferred request body in bytes</td>
  </tr>
  <tr>
    <td><code>CONTENT_TYPE</code></td>
    <td><code>text/plain</code></td>
    <td>Media type of the data transferred with the request</td>
  </tr>
  <tr>
    <td><code>DOCUMENT_ROOT</code></td>
    <td><code>.../devwex/documents</code></td>
    <td>Physical path from the root directory of the Web documents</td>
  </tr>
  <tr>
    <td><code>GATEWAY_INTERFACE</code></td>
    <td><code>CGI/1.1</code></td>
    <td>Version of the CGI interface supported by the server</td>
  </tr>
  <tr>
    <td><code>HTTP_ACCEPT</code></td>
    <td><code>*/*</code></td>
    <td>List of media types supported by the client</td>
  </tr>
  <tr>
    <td><code>HTTP_ACCEPT_ENCODING</code></td>
    <td><code>gzip, deflate</code></td>
    <td>List of encoding methods supported by the client</td>
  </tr>
  <tr>
    <td><code>HTTP_ACCEPT_LANGUAGE</code></td>
    <td><code>en</code></td>
    <td>List of languages supported by the client</td>
  </tr>
  <tr>
    <td><code>HTTP_CONNECTION</code></td>
    <td><code>Keep-Alive</code></td>
    <td>Status of the HTTP connection between server and client</td>
  </tr>
  <tr>
    <td><code>HTTP_HOST</code></td>
    <td><code>seanox.local</code></td>
    <td>
      Domain name or IP address of the target address requested by the client
    </td>
  </tr>
  <tr>
    <td><code>HTTP_USER_AGENT</code></td>
    <td><code>Mozilla/4.0 (compatible; ...)</code></td>
    <td>Client product and version information</td>
  </tr>
  <tr>
    <td><code>MODULE_OPTS</code></td>
    <td><code>...ssi.Connector [allow:all] [M]</code></td>
    <td>Module call with all parameters</td>
  </tr>
  <tr>
    <td><code>PATH</code></td>
    <td><code>...</code></td>
    <td>
      List of paths for searching for resources and applications, optionally set
      by <code>[SERVER:ENV]</code>
    </td>
  </tr>
  <tr>
    <td><code>PATH_CONTEXT</code></td>
    <td><code>/example.ssi</code></td>
    <td>real path specification of the URL for absolute paths</td>
  </tr>
  <tr>
    <td><code>PATH_INFO</code></td>
    <td><code>/folder/...</code></td>
    <td>extended path specification of the URL for absolute paths</td>
  </tr>          
  <tr>
    <td><code>PATH_URL</code></td>
    <td><code>/example.ssi/folder/...</code></td>
    <td>Complete path specification of the URL</td>
  </tr>
  <tr>
    <td><code>PATH_TRANSLATED</code></td>
    <td><code>.../devwex/documents/example.ssi</code></td>
    <td>Physical path of the called resource in the file system</td>
  </tr>
  <tr>
    <td><code>QUERY_STRING</code></td>
    <td><code>parameter1=value1&amp;parameter2=value2&amp;...</code></td>
    <td>
      Query string passed with the request URI with parameters and values
    </td>
  </tr>
  <tr>
    <td><code>REMOTE_ADDR</code></td>
    <td><code>sirius.seanox.com</code></td>
    <td>Name or IP address of the client</td>
  </tr>
  <tr>
    <td><code>REMOTE_PORT</code></td>
    <td><code>1573</code></td>
    <td>Port from client</td>
  </tr>
  <tr>
    <td><code>REQUEST_METHOD</code></td>
    <td><code>POST</code></td>
    <td>HTTP request method</td>
  </tr>
  <tr>
    <td><code>REQUEST_URI</code></td>
    <td><code>/example.ssi/...?parameter=value</code></td>
    <td>Path of HTTP request including passed parameters</td>
  </tr>
  <tr>
    <td><code>SCRIPT_FILENAME</code></td>
    <td><code>.../devwex/documents/example.ssi</code></td>
    <td>Physical path of the called resource in the file system</td>
  </tr>
  <tr>
    <td><code>SCRIPT_NAME</code></td>
    <td><code>/example.ssi/...</code></td>
    <td>HTTP path of the called resource</td>
  </tr>
  <tr>
    <td><code>SCRIPT_URI</code></td>
    <td><code>http://...seanox.com/example.ssi/...</code></td>
    <td>Complete HTTP path of the called resource</td>
  </tr>
  <tr>
    <td><code>SCRIPT_URL</code></td>
    <td><code>/example.ssi</code></td>
    <td>Relative path in the HTTP request to the requested resource</td>
  </tr>
  <tr>
    <td><code>SERVER_NAME</code></td>
    <td><code>seanox.local</code></td>
    <td>Name or IP address of the server in the network</td>
  </tr>
  <tr>
    <td><code>SERVER_PORT</code></td>
    <td><code>80</code></td>
    <td>Port of the called server</td>
  </tr>
  <tr>
    <td><code>SERVER_PROTOCOL</code></td>
    <td><code>HTTP/1.0</code></td>
    <td>Version of the HTTP protocol supported by the server</td>
  </tr>
  <tr>
    <td><code>SERVER_SOFTWARE</code></td>
    <td><code>Seanox-Devwex/...</code></td>
    <td>Product name of the installed server software</td>
  </tr>
  <tr>
    <td><code>SYSTEMDRIVE</code></td>
    <td><code>C:</code></td>
    <td>
      drive of the operating system, is optionally set for Microsoft Windows via
      <code>[SERVER:ENV]</code>
    </td>
  </tr>
  <tr>
    <td><code>SYSTEMROOT</code></td>
    <td><code>C:\Windows</code></td>
    <td>
      Path from the operating system, is optionally set for Microsoft Windows
      via <code>[SERVER:ENV]</code>
    </td>
  </tr>
  <tr>
    <td><code>UNIQUE_ID</code></td>
    <td><code>FEK2VFTY26DHI584C5</code></td>
    <td>unique identification number related to the request</td>
  </tr>
</table>

> [!TIP]
> If a key in the configuration ends with the '[+]' option, the line comment is
> deactivated and the complete value including the semicolon is used.
> ```
> PATH [+] = ./documents;./libraries;./system
> ```

> [!IMPORTANT]
> The working directory of the CGI applications is the working directory of
> Seanox Devwex, i.e. the directory in which the server was started. This must
> be taken into account when configuring the CGI applications, e.g. if paths for
> resources and configuration files need to be defined, as is the case with PHP
> for 'php.ini'.

> [!IMPORTANT]
> With the installation of PHP, the configuration file `php.ini` must be placed
> in the working directory of Seanox Devwex or the path with the parameter `-c`
> must be communicated to PHP.

> [!IMPORTANT]
> In PHP, configuration of parameter `cgi.rfc2616_headers = 1` is required for
> the correct function of PHP function `header();`, for the security of
> execution `cgi.force_redirect = 0` and those and
> `cgi.redirect_status_env = 302`.
> ```
> cgi.force_redirect      = 0
> cgi.redirect_status_env = 302
> cgi.rfc2616_headers     = 1
> ```

> [!TIP]
> XCGI/CGI applications and scripts can be used in all directories of the
> `DOCROOT`. A special `CGI-BIN` is not provided.

> [!TIP]
> Executable Windows applications (`*.exe`, `*.com`) can also be used under
> Windows with an alternative file extension, e.g. XCGI/CGI can be used as
> Windows applications (`*.exe` or `*.com`) for which any file extension can be
> used, e.g. the file `example.exe` can be renamed and used as `example.cgi`.
> ```
> CGI = POST GET > [C]
> ```
> In this way, not all Windows applications are executed via the HTTP request.

> [!TIP]
> The method alias `ALL` passes all incoming HTTP methods to the CGI, but the
> alias does not bypass the restriction of `[SERVER:INI] METHODS`.


## Status Codes
The server responds all HTTP requests with a status code and informs the client
about the processing of the requests.Primary the server status is a numeric
value with an optional message. Status codes are global and are used by all
servers and virtual hosts.

> __Scheme for status code__
> ```
> CODE = TEXT [OPTION]
> ```

> __Examples of status codes__
> ```
> 404 = Document Not Found
> ```
> Resulting response header
> ```
> HTTP/1.0 404 Document Not Found
> ```

_Overview of options_
<table>
  <tr>
    <th>Option</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>[H]</code></td>
    <td>
      Responses with a status code that uses this option contain only a header
      and no content.
    </td>
  </tr>
</table>


## Media Types
The media type, also known as Internet Media Type, Content Type and Multipurpose
Internet Mail Extensions (MIME Type), classifies the data transmitted with the
server response by assigning media types and subtypes. In the configuration, the
media types are assigned a list of file extensions separated by spaces, which is
global and shared by all servers and virtual hosts.

> __Scheme for media types__
> ```
> MediaType = file extension file extension ...
> ```

> __Example of MediaType assignment__
> ```
> text/html = html htm shtml
> ```
> The media type `text/html` is assigned to the file extension `*.html`, `*.htm`
> and `*.shtml`.


- - -
&#9665; [Installation](installation.md)
&nbsp;&nbsp;&nbsp;&nbsp; &#8801; [Table of Contents](README.md)
&nbsp;&nbsp;&nbsp;&nbsp; [Starting and Stopping](starting-and-stopping.md) &#9655;
