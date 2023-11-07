[Lizenzbedingungen](license.md) | [Inhalt](README.md) | [Systemanforderung](requirements.md)
- - -

# Merkmale


## Inhalt
- [Architektur](#architektur)
- [Hypertext Transfer Protocol](#hypertext-transfer-protocol)
- [Fernüberwachung](#fernüberwachung)
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

Seanox Devwex ist ein multithreading-fähiger Laufzeit-Container für
(Server-)Module, welche über die bereitgestellte Server- und Module-API
eingebunden werden. Zudem unterstützt der enthaltene ClassLoader das Laden und
Entladen von Servern und Modulen zur Laufzeit.


## Hypertext Transfer Protocol

Die HTTP-Server-Implementierung unterstützt u.a. Virtual Hosting, Filter,
HTTP-Module, SSL/TLS, XCGI/CGI1.1, Directory Listing, Templates.


## Fernüberwachung

Der Laufzeit-Container kann per Telnet gesteuert werden (Neustart und Stopp) und
der Betriebsstatus zu den laufenden Servern und Modulen lässt sich abfragen.
Neben der Implementierung vom Server ist auch ein Client enthalten.

        
## Konfiguration

Die Konfiguration verwendet eine zentrale Datei in einem erweiterten INI-Format,
das in Sektionen mit Schlüsseln und Werten unterteilt ist. Mehrfachvererbung bei
den Sektionen, dynamische Werte sowie der Zugriff auf System- und
Umgebungsvariablen werden unterstützt.


## Schnittstellen

### Server-API (SAPI)

Mit der Server-API werden die Implementierungen eingebunden, welche den
physischen Zugriff im Netzwerk für ein Protokoll an einer Adresse und einem Port
zur Verfügung stellen. Bestehende Server- und Netzwerk-Funktionalitäten lassen
sich damit ändern bzw. neue bereitstellen.

### Module-API (XAPI)

Mit der Module-API werden die Implementierungen eingebunden, welche im
Hintergrund agieren und nach Aussen keine direkten Funktionen bereitstellen.

### HTTP

Entsprechend der Spezifikation 1.0 werden GET, POST und HEAD sowie OPTIONS, PUT
und DELETE vom HTTP 1.1 unterstützt. Weitere Methoden lassen sich über
HTTP-Module, XCGI bzw. CGI bereitstellen.

### HTTP-Module-API (XAPI+)

Die HTTP-Module-API ist eine Erweiterung der Module-API für den HTTP-Server zur
Implementierung von Filter- und Service-Funktionalitäten, die im Kontext vom
Server laufen.

### (Fast)CGI

Zum Datenaustausch sowie zur Anbindung externer Laufzeitumgebungen und
Anwendungen werden die Spezifikation 1.1 des Common Gateway Interfaces und somit
PHP, Perl, Python und andere unterstütz. Optional ist auch FastCGI verfügbar.

### XCGI

Das XCGI ist eine an das CGI angelehnte Schnittstelle mit gleichem Grundprinzip
zur Kommunikation über den Standard-I/O, übermittelt dabei aber auch die
serverrelevanten Informationen und Umgebungsvariablen. Womit Anwendungen genutzt
werden können, welche keine exklusive Umgebung besitzen oder keinen Zugriff auf
die Umgebungsvariablen vom Betriebssystem haben.

### Telnet

Der enthaltene Fernzugriff unterstützt eine auf Telnet basierende Steuerung für
Statusabfragen, Neustart und Stopp. Neben der Server-Implementierung ist auch
eine Client-Implementierung enthalten.


## Sicherheit

Für eine gesicherte Übertragung von Daten werden unter anderem Transport Layer
Security (TLS) und Secure Socket Layer (SSL) mit Server- und Client-Zertifikaten
unterstützt. Zertifikate können dabei für jeden physischen Host einzeln, durch
Vererbung in Gruppen oder global zugewiesen werden.

Der Zugriff auf Verzeichnisse und Dateien kann mit Basic- sowie
Digest-Access-Authentication versehen werden, welche Gruppen unterstützen. Zudem
kann der Zugriff über Filter gesteuert werden, welche frei definierbare Regeln,
individuelle Fehlerseiten, automatische Weiterleitungen und Module unterstützen.


## Virtualisierung

Der HTTP-Server unterstützt Virtual Hosting und Aliasing für virtuelle Pfade.


## Individuelle Anpassung

HTTP-Server und virtuelle Hosts verwenden für die Fehlerseiten und die
Listenansicht der Verzeichnisse (Directory Listing) Templates, die sich
individuell anpassen lassen. Mit Unterstützung von CGI-Umgebungsvariablen kann
der Inhalt dynamisch gestaltet werden.


## Erweiterbarkeit

Die modulare Architektur und zahlreichen Schnittstellen ermöglichen das Ändern,
Erweitern und Hinzufügen von Funktionalitäten.


- - -

[Lizenzbedingungen](license.md) | [Inhalt](README.md) | [Systemanforderung](requirements.md)
