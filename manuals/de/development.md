[Steueren und �berwachen](control.md) | [Inhalt](README.md)
- - -

# Entwicklung

Die Erweiterbarkeit von Seanox Devwex ist ein wichtiges Merkmal. So ist die
Implementierung vom Laufzeit-Container und der Server eine Herausforderung um
mit minimalsten Mitteln ein Maximum an Funktionalit�t zu erreichen und dabei
eine saubere Architektur zu bewahren.


## Contents Overview
- [Architektur](#architektur)
- [Sequenzen](#sequenzen)
  - [Start](#start)
  - [Modulaufruf](#modulaufruf)
  - [Neustart](#neustart)
  - [Beenden](#beenden)
- [Erweiterung](#erweiterung)  

## Architektur

TODO:


## Sequenzen

Start, Neustart und Beenden der Server sowie das Laden, Anfordern und Entladen
von Modulen sind feste Abfolgen vom Laufzeit-Container.

### Start

- Der Klassenpfad wird um alle Dateien der Verzeichnisse erweitert, die mit dem
  VM-Argument `-Dlibraries` angegeben wurden.
- Alle Module aus der Sektion `INITIALIZE` werden geladen und �ber den
  Konstruktor `Module(String options)` initialisiert und registriert.
- Alle Server werden ermittelt, indem nach Sektionen gesucht wird, die auf
 `INI` enden und zu denen eine Implementierung im Klassenpfad gefunden werden
  kann. Die gefundenen Server werden geladen, registriert und �ber den
  Konstruktor `Server(String name, Initialize initialize)` oder
  `Server(String name, Object initialize)` initialisiert. Dazu werden jedem
  Server der Name entsprechend der ermittelten Sektion sowie eine komplette
  Kopie der zentralen Konfiguration als Initialize-Objekt �bergeben. Nach
  erfolgreicher Initialisierung wird der Server als (Daemon)Thread gestartet und
  kann seine Arbeit in der Methode `Server.run()` aufnehmen.

### Modulaufruf

- Ist das Modul noch nicht geladen, wird dies aus dem aktuellen Klassenpfad
  ermittelt, �ber `Module(String options)` initialisiert und registriert. Eine
  Konfiguration wird dabei nicht �bergeben, da f�r Module nur eine zentrale
  Konfiguration in der Sektion `INITIALIZE` vorgesehen ist.
- Ist das Modul bereits geladen, wird die aktuelle Instanz verwendet.

### Neustart

Die Sequenz entspricht der Kombination aus [Beenden](#beenden) und [Start](
    #start).

- Alle registrierten Server-Instanzen werden �ber die Methode `Server.destroy()`
  zum Beenden aufgefordert.
- Alle registrierten Module werden �ber die Methode `Module.destroy()` zum
  Beenden aufgefordert.
- Das Einleiten vom Beenden der Server verl�uft asynchron. Der
  Laufzeit-Container wartet auf das Ende aller registrierten Server.
- Alle Module und Server werden durch das Verwerfen vom aktuell verwendeten
  ClassLoader entladen.
- Der Klassenpfad wird um alle Dateien der Verzeichnisse erweitert, die mit dem
  VM-Argument `-Dlibraries` angegeben wurden.
- Alle Module aus der Sektion `INITIALIZE` werden geladen und �ber den
  Konstruktor `Module(String options)` initialisiert und registriert.
- Alle Server werden ermittelt, indem nach Sektionen gesucht wird, die auf `INI`
  enden und zu denen eine Implementierung im Klassenpfad gefunden werden kann.
  Die gefundenen Server werden geladen, registriert und �ber den Konstruktor
  `Server(String name, Initialize initialize)` oder
  `Server(String name, Object initialize)` initialisiert. Dazu werden jedem
  Server der Name entsprechend der ermittelten Sektion sowie eine komplette
  Kopie der zentralen Konfiguration als Initialize-Objekt �bergeben. Nach
  erfolgreicher Initialisierung wird der Server als (Daemon)Thread gestartet und
  kann seine Arbeit in der Methode `Server.run()` aufnehmen.

### Beenden

- Alle registrierten Server-Instanzen werden �ber die Methode `Server.destroy()`
  zum Beenden aufgefordert.
- Alle registrierten Module werden �ber die Methode `Module.destroy()` zum
  Beenden aufgefordert.
- Das Einleiten vom Beenden der Server verl�uft asynchron. Der
  Laufzeit-Container wartet auf das Ende aller registrierten Server.
- Alle Module und Server werden durch das Verwerfen vom aktuell verwendeten
  ClassLoader entladen.


## Erweiterung

Die Erweiterung von Seanox Devwex ist auf mehrere Arten m�glich. Der Service
stellt dazu eine Server-, Module- und HTTP-Module-API bereit. Spezielle
Interfaces und Abstraktionen sind im Service selbst nicht enthalten, dieser
steuert alle Server und Module �ber Reflections und stille Standards/Interfaces,
was u.U. detaillierte Kenntnisse �ber die Komponenten und Arbeitsweise von
Seanox Devwex erfordert.

Einfacher und komfortabler gestaltet sich die Implementierung mit Seanox Commons
als Bestandteil vom Seanox Devwex SDK (f�r Seanox Devwex 5.x in Entwicklung),
welche die erforderlichen Interfaces sowie Abstraktionen enthalten und
zus�tzliche ClassLoader-Ebenen bereitstellen.

Im Nachfolgenden werden ausschliesslich die im Service enthaltene Server-,
Module- und HTTP-Module-API beschrieben.


## Server

Server stellen den physischen Zugriff im Netzwerk f�r ein Protokoll an einer
Adresse und einem Port zur Verf�gung. Seanox Devwex bindet Server �ber die
Server-API (SAPI) ein. Mit der API lassen sich auch bestehende Server- und
Netzwerk-Funktionalit�ten �ndern bzw. neue bereitstellen.


- - -

[Steueren und �berwachen](control.md) | [Inhalt](README.md)
