[Lizenzbedingungen](license.md) | [Inhalt](README.md) | [Systemanforderung](requirements.md)
- - -

# Merkmale


## Inhalt
- [Architektur](#architektur)
- [Hypertext Transfer Protocol](#hypertext-transfer-protocol)
- [Fern�berwachung](#fern�berwachung)
- [Konfiguration](#konfiguration)
- [Schnittstellen](#schnittstellen)
  - [Server-API (SAPI)](#server-api-sapi)
  - [Module-API (XAPI)](#module-api-xapi)
  - [HTTP](#http)
  - [HTTP-Module-API (XAPI+)](#http-module-api-xapi)
  - [(Fast)CGI](#fast-cgi)
  - [XCGI](#xcgi)
  - [Telnet](#telnet)
- [Sicherheit](#sicherheit)
- [Virtualisierung](#virtualisierung)
- [Individuelle Anpassung](#individuelle-anpassung)
- [Erweiterbarkeit](#erweiterbarkeit)


## Architektur

Seanox Devwex ist ein multithreading-f�higer Laufzeit-Container f�r
(Server-)Module, welche �ber die bereitgestellte Server- und Module-API
eingebunden werden. Zudem unterst�tzt der enthaltene ClassLoader das Laden und
Entladen von Servern und Modulen zur Laufzeit.


## Hypertext Transfer Protocol

Die HTTP-Server-Implementierung unterst�tzt u.a. Virtual Hosting, Filter,
HTTP-Module, SSL/TLS, XCGI/CGI1.1, Directory Listing, Templates.


## Fern�berwachung

Der Laufzeit-Container kann per Telnet gesteuert werden (Neustart und Stopp) und
der Betriebsstatus zu den laufenden Servern und Modulen l�sst sich abfragen.
Neben der Implementierung vom Server ist auch ein Client enthalten.

        
## Konfiguration

Die Konfiguration verwendet eine zentrale Datei in einem erweiterten INI-Format,
das in Sektionen mit Schl�sseln und Werten unterteilt ist. Mehrfachvererbung bei
den Sektionen, dynamische Werte sowie der Zugriff auf System- und
Umgebungsvariablen werden unterst�tzt.


## Schnittstellen

### Server-API (SAPI)

Mit der Server-API werden die Implementierungen eingebunden, welche den
physischen Zugriff im Netzwerk f�r ein Protokoll an einer Adresse und einem Port
zur Verf�gung stellen. Bestehende Server- und Netzwerk-Funktionalit�ten lassen
sich damit �ndern bzw. neue bereitstellen.

### Module-API (XAPI)

Mit der Module-API werden die Implementierungen eingebunden, welche im
Hintergrund agieren und nach Aussen keine direkten Funktionen bereitstellen.

### HTTP

Entsprechend der Spezifikation 1.0 werden GET, POST und HEAD sowie OPTIONS, PUT
und DELETE vom HTTP 1.1 unterst�tzt. Weitere Methoden lassen sich �ber
HTTP-Module, XCGI bzw. CGI bereitstellen.

### HTTP-Module-API (XAPI+)

Die HTTP-Module-API ist eine Erweiterung der Module-API f�r den HTTP-Server zur
Implementierung von Filter- und Service-Funktionalit�ten, die im Kontext vom
Server laufen.

### (Fast)CGI

Zum Datenaustausch sowie zur Anbindung externer Laufzeitumgebungen und
Anwendungen werden die Spezifikation 1.1 des Common Gateway Interfaces und somit
PHP, Perl, Python und andere unterst�tz. Optional ist auch FastCGI verf�gbar.

### XCGI

Das XCGI ist eine an das CGI angelehnte Schnittstelle mit gleichem Grundprinzip
zur Kommunikation �ber den Standard-I/O, �bermittelt dabei aber auch die
serverrelevanten Informationen und Umgebungsvariablen. Womit Anwendungen genutzt
werden k�nnen, welche keine exklusive Umgebung besitzen oder keinen Zugriff auf
die Umgebungsvariablen vom Betriebssystem haben.

### Telnet

Der enthaltene Fernzugriff unterst�tzt eine auf Telnet basierende Steuerung f�r
Statusabfragen, Neustart und Stopp. Neben der Server-Implementierung ist auch
eine Client-Implementierung enthalten.


## Sicherheit

F�r eine gesicherte �bertragung von Daten werden unter anderem Transport Layer
Security (TLS) und Secure Socket Layer (SSL) mit Server- und Client-Zertifikaten
unterst�tzt. Zertifikate k�nnen dabei f�r jeden physischen Host einzeln, durch
Vererbung in Gruppen oder global zugewiesen werden.

Der Zugriff auf Verzeichnisse und Dateien kann mit Basic- sowie
Digest-Access-Authentication versehen werden, welche Gruppen unterst�tzen. Zudem
kann der Zugriff �ber Filter gesteuert werden, welche frei definierbare Regeln,
individuelle Fehlerseiten, automatische Weiterleitungen und Module unterst�tzen.


## Virtualisierung

Der HTTP-Server unterst�tzt Virtual Hosting und Aliasing f�r virtuelle Pfade.


## Individuelle Anpassung

HTTP-Server und virtuelle Hosts verwenden f�r die Fehlerseiten und die
Listenansicht der Verzeichnisse (Directory Listing) Templates, die sich
individuell anpassen lassen. Mit Unterst�tzung von CGI-Umgebungsvariablen kann
der Inhalt dynamisch gestaltet werden.


## Erweiterbarkeit

Die modulare Architektur und zahlreichen Schnittstellen erm�glichen das �ndern,
Erweitern und Hinzuf�gen von Funktionalit�ten.


- - -

[Lizenzbedingungen](license.md) | [Inhalt](README.md) | [Systemanforderung](requirements.md)
