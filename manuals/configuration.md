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
  - [Remote Control](#remote-control)
  - [Hypertext Transfer Protocol](#hypertext-transfer-protocol)
  - [Server](#server)
  - [Transport Layer Security / Secure Socket Layer](#transport-layer-security--secure-socket-layer)
  - [Client Authentication / Mutual Authentication](#client-authentication--mutual-authentication)
  - [Virtual Hosts / Virtual Hosting](#virtual-hosts--virtual-hosting)
  - [Filters](#filters)
  - [Virtual Paths (Aliasing and Redirection)]()
  - [Basic Access Authentication / Digest Access Authentication]()
  - [Directory Listing]()
  - [Environment Variables]()
  - [Common Gateway Interface]()
- [Status Codes]()
- [Media Types]()


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

Comments begin with a semicolon, are optional and can be used anywhere in a
line, so the following characters are not part of section, key or value. With
the option `[+]` at the end of a key, the use of comments for that key can be
disabled and the semicolon used in the value.

Sections, keys and values also support a hexadecimal notation, starting with
`0x...` followed by the hexadecimal string, which can only be used for the
complete element.

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
    <td>[COMMON]</td>
    <td>
      General application configuration.
    </td>
  </tr>
  <tr>
    <td>[INITIALIZE]</td>
    <td>
      Central module configuration.<br>
      Initialization with the (re)start of the service.
    </td>
  </tr>
  <tr>
    <td>[REMOTE]</td>
    <td>
      Configuration area of the remote control of the server engine.
    </td>
  </tr>
  <tr>
    <td>[SERVER]</td>
    <td>
      Configuration area of the HTTP server.
    </td>
  </tr>
  <tr>
    <td>[VIRTUAL]</td>
    <td>
      Configuration area of the virtual hosts for HTTP servers.
    </td>
  </tr>
  <tr>
    <td>[STATUSCODES]</td>
    <td>
      HTTP server status codes.
    </td>
  </tr>
  <tr>
    <td>[MEDIATYPES]</td>
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
    <td>CLEANUP</td>
    <td>ON|OFF</td>
    <td>
      Option for intelligent resource cleanup.<br>
      The Java garbage collector is triggered if released resources are
      expected.
    </td>
  </tr>
  <tr>
    <td>RELOAD</td>
    <td>ON|OFF</td>
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
Servers are an essential part of the server engine and it requires at least one
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


### Remote Control
The included remote access supports a Telnet-based control of the server engine
(restart and stop) and queries about the operating status of the running servers
and modules. In addition to the server implementation, Seanox Devwex also
includes a compatible client implementation. Both use the `[REMOTE:INI]` section
in the configuration file for configuration.

_Overview of configuration_
<table>
  <tr>
    <th>Key</th>
    <th>Value</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>ADDRESS</td>
    <td>AUTO|IP|NAME</td>
    <td>
      Local address or name of the server in the network.<br>
      `AUTO` corresponds to 0.0.0.0 and uses all IP addresses available in the
       system.
    </td>
  </tr>
  <tr>
    <td>PORT</td>
    <td>...</td>
    <td>
      Local port of the server.
    </td>
  </tr>
</table>


### Hypertext Transfer Protocol
The server and the protocol have many configuration options, which are divided
into different sections, whose order is arbitrary. All the sections have the
same name as the server section, but differ in the section identifier at the
end.

_Overview of sections_
<table>
  <tr>
    <th>Section</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>[SERVER:X:INI]</td>
    <td>
      Server configuration, network and runtime behavior
    </td>
  </tr>
  <tr>
    <td>[SERVER:X:SSL]</td>
    <td>TLS/SSL configuration</td>
  </tr>
  <tr>
    <td>[SERVER:X:REF]</td>
    <td>Virtual paths</td>
  </tr>
  <tr>
    <td>[SERVER:X:ACC]</td>
    <td>Access control (Access Control List)</td>
  </tr>
  <tr>
    <td>[SERVER:X:CGI]</td>
    <td>XCGI/CGI assignment and configuration</td>
  </tr>
  <tr>
    <td>[SERVER:X:ENV]</td>
    <td>Environment variables</td>
  </tr>
  <tr>
    <td>[SERVER:X:FLT]</td>
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
      <td>ADDRESS</td>
      <td>AUTO|IP|NAME</td>
      <td>
        Local address or name of the server in the network.<br>
        <code>AUTO</code> corresponds to 0.0.0.0 and uses all IP addresses
        available in the system.
      </td>
    </tr>
    <tr>
      <td>BACKLOG</td>
      <td>500</td>
      <td>
        Maximum number of disconnected connections when the number of
        simultaneous connections is exhausted.
      </td>
    </tr>
    <tr>
      <td>ISOLATION</td>
      <td>300000</td>
      <td>
        Maximum runtime of XCGI/CGI processes in milliseconds. If exceeded, the
        process is terminated by the server. The value <code>0</code> or less
        ignores the runtime limit.
      </td>
    </tr>
    <tr>
      <td>MAXACCESS</td>
      <td>100</td>
      <td>
        Maximum number of simultaneous connections.
      </td>
    </tr>
    <tr>
      <td>PORT</td>
      <td>...</td>
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
      <td>ACCESSLOG</td>
      <td>... &gt; ../system/access.log</td>
      <td>
        Format and path of the log file for logging accesses.<br>
        Without path the Standard I/O is used, <code>OFF</code> disables
        logging.<br>
        Format and Path supports a print formatted syntax, with which CGI
        environment variables in the format <code>%[...]</code>> and symbols of
        date and time in the format <code>%t...</code> can be used.<br>
        The date/time symbols are based on the <a target="_extern"
            href="https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#dt">
          Date/Time Conversions</a> of the formatter class.<br>
      </td>
    </tr>
    <tr>
      <td>BLOCKSIZE</td>
      <td>65535</td>
      <td>
        Maximum size of the data blocks during data transfer in bytes.
      </td>
    </tr>
    <tr>
      <td>DEFAULT</td>
      <td>index.htm index.html ...</td>
      <td>
        A list of standard documents, separated by spaces, to be displayed when
        directories are called, using the first existing document in the list,
        if none of the listed documents exist, the contents of the directory
        will be displayed if this is allowed.
      </td>
    </tr>
    <tr>
      <td>DOCROOT</td>
      <td>../documents</td>
      <td>
        Path from the document directory.
      </td>
    </tr>
    <tr>
      <td>IDENTITY</td>
      <td>ON|OFF</td>
      <td>
        Option to transfer the server name with the XCGI/CGI environment
        variable and the HTTP response.
      </td>
    </tr>
    <tr>
      <td>INDEX</td>
      <td>ON|OFF</td>
      <td>
        Option for displaying directories as a navigable list view (Directory
        Listing).<br>
        With the addition <code>[S]</code>> hidden entries of the file system
        can be suppressed for display.
      </td>
    </tr>
    <tr>
      <td>INTERRUPT</td>
      <td>10</td>
      <td>
        Interruption for system processes in milliseconds.<br>
        This setting minimizes the CPU utilization of labor-intensive processes,
        in which they do without a portion of their execution time and thus
        leave time slots/time slices to other processes. 
      </td>
    </tr>
    <tr>
      <td>MEDIATYPE</td>
      <td>application/octet-stream</td>
      <td>
        Standard media type that is used when the requested media type is not
        included in the list of media types (section <code>[MEDIATYPES]</code>).
      </td>
    </tr>
    <tr>
      <td>METHODS</td>
      <td>GET POST HEAD OPTIONS PUT DELETE ...</td>
      <td>
        List of methods that the server is allowed to process, separated by
        spaces.
      </td>
    </tr>
    <tr>
      <td>SYSROOT</td>
      <td>../system</td>
      <td>
        Path from the directory of the system files.
      </td>
    </tr>
    <tr>
      <td>TIMEOUT</td>
      <td>30000</td>
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
    <td>PROTOCOL</td>
    <td>
      TLS|TLSv1|TLSv1.1|TLSv1.2|...<br>
      SSL|SSLv2|SSLv3|...
    </td>
    <td>
      Protocol: <code>TLS</code> (Transport Layer Security), <code>SSL</code>
      (Secure Socket Layer).<br>
      Default value: <code>TLS</code> (if not specified)
    </td>
  </tr>
  <tr>
    <td>CLIENTAUTH</td>
    <td>OFF|ON|AUTO</td>
    <td>
      (De)activation of the client authorization<br>
      <code>ON</code> requires mandatory client authorization, without which the
      connection to the client is terminated.<br>
      <code>AUTO</code> requires an optional client authorization and leaves the
      authorization decision to the client and the server establishes the
      connection even if the client does not provide authorization.<br>
      Default value: <code>OFF</code> (if not specified)
    </td>
  </tr>
  <tr>
    <td>KEYSTORE</td>
    <td>...</td>
    <td>
      Path of the keystore file
    </td>
  </tr>
  <tr>
    <td>TYPE</td>
    <td>JCEKS|JKS|DKS|PKCS11|PKCS12|...</td>
    <td>
      Type of used keystore.<br>
      Default value from the JDK: <code>PKCS12</code> (if not specified)
    </td>
  </tr>
  <tr>
    <td>ALGORITHM</td>
    <td>SunX509|PKIX|...</td>
    <td>
      Algorithm of encryption from keystore.<br>
      Default value from the JDK: <code>SunX509</code> / <code>PKIX</code> (if
      not specified)
    </td>
  </tr>
  <tr>
    <td>PASSWORD</td>
    <td>...</td>
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

> __TODO: Example of the export__
> ```
> ./java/bin/keytool -genkeypair -alias Client -dname 'CN=Client' -validity 365
>     -keystore client.keystore -keyalg RSA -keysize 2048 -keypass changeIt
>     -storetype PKCS12 -storepass changeIt
> ```
> ```
> ./java/bin/keytool -exportcert -alias Client -file client.cer
>     -keystore client.keystore -storepass changeIt
> ```

The keystores of the server and the client are migrated into each other, because
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
    <td>NAME</td>
    <td>...</td>
    <td>freely selectable</td>
  </tr>
  <tr>
    <td>METHOD</td>
    <td>GET|POST|PUT|ALL|...</td>
    <td>HTTP method to be reacted to</td>
  </tr>
  <tr>
    <td>CONDITION</td>
    <td>IS|NOT|ALWAYS</td>
    <td>stipulation</td>
  </tr>
  <tr>
    <td></td>
    <td>IS</td>
    <td>The filter responds when the condition is fulfilled.</td>
  </tr>
  <tr>
    <td></td>
    <td>NOT</td>
    <td>
      The filter responds if the condition is not fulfilled.
    </td>
  </tr>
  <tr>
    <td></td>
    <td>ALWAYS</td>
    <td>
      The filter always responds.<br>
      The specification of function and value is not required for this
      condition.
    </td>
  </tr>
  <tr>
    <td>FUNCTION</td>
    <td>STARTS|EQUALS|CONTAINS|ENDS|EMPTY</td>
    <td>Type of comparison</td>
  </tr>
  <tr>
    <td></td>
    <td>STARTS</td>
    <td>The value of the variable starts with the comparison value.</td>
  </tr>
  <tr>
    <td></td>
    <td>EQUALS</td>
    <td>
      The value of the variable corresponds to the comparison value.
    </td>
  </tr>
  <tr>
    <td></td>
    <td>CONTAINS</td>
    <td>The value of the variable contains the comparison value.</td>
  </tr>
  <tr>
    <td></td>
    <td>ENDS</td>
    <td>The value of the variable ends with the comparison value.</td>
  </tr>
  <tr>
    <td></td>
    <td>EMPTY</td>
    <td>
      The value of the variables is empty or does not exist.<br>
      The specification of function and value is not required for this
      condition.
    </td>
  </tr>
  <tr>
    <td></td>
    <td>MATCH</td>
    <td>
      The value of the variable corresponds to the comparison value as a regular
      expression.
    </td>
  </tr>
  <tr>
    <td>VARIABLE</td>
    <td>...</td>
    <td>
      All environment variables / CGI variables are available to the filter.
    </td>
  </tr>
  <tr>
    <td>VALUE</td>
    <td>...</td>
    <td>comparative value</td>
  </tr>
  <tr>
    <td>OPTION</td>
    <td>[+] [R] [M]</td>
    <td>Additional filter control options</td>
  </tr>
  <tr>
    <td></td>
    <td>[+]</td>
    <td>Logical AND operation of several conditions.</td>
  </tr>
  <tr>
    <td></td>
    <td>[R]</td>
    <td>Reference for forwarding (redirect).</td>
  </tr>
  <tr>
    <td></td>
    <td>[M]</td>
    <td>
      Reference to the use of a HTTP module.<br>
      Addressing the module is not a final step, but terminating the filter
      sequence only occurs if a module changes the response status and/or sends
      data to the client.
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
    <td>Request</td>
    <td>REQUEST_METHOD</td>
    <td>GET</td>
    <td>HTTP request methods</td>
  </tr>
  <tr>
    <td>Request</td>
    <td>REQUEST_URI</td>
    <td>/directory/file.cgi?value=123</td>
    <td>Path of HTTP request including passed parameters</td>
  </tr>
  <tr>
    <td>Request</td>
    <td>QUERY_STRING</td>
    <td>value=123</td>
    <td>List of parameters passed with the request URI</td>
  </tr>
  <tr>
    <td>Host</td>
    <td>HTTP_HOST</td>
    <td>www.xxx.zzz</td>
    <td>
      Domain name or IP address of the target address requested by the client
    </td>
  </tr>
  <tr>
    <td>Accept-Encoding</td>
    <td>HTTP_ACCEPT_ENCODING</td>
    <td>gzip, deflate</td>
    <td>List of encoding methods supported by the client</td>
  </tr>
  <tr>
    <td>Accept-Language</td>
    <td>HTTP_ACCEPT_LANGUAGE</td>
    <td>de</td>
    <td>List of languages supported by the client</td>
  </tr>
  <tr>
    <td>Accept</td>
    <td>HTTP_ACCEPT</td>
    <td>*/*</td>
    <td>List of media types supported by the client</td>
  </tr>
  <tr>
    <td>User-Agent</td>
    <td>HTTP_USER_AGENT</td>
    <td>Browser</td>
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

TODO:



- - -
&#9665; [Installation](installation.md)
&nbsp;&nbsp;&nbsp;&nbsp; &#8801; [Table of Contents](README.md)
&nbsp;&nbsp;&nbsp;&nbsp; [Starting and Stopping](starting-and-stopping.md) &#9655;
