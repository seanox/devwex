[Steueren und Überwachen](control.md) | [Inhalt](README.md)
- - -

# Entwicklung

Die Erweiterbarkeit von Seanox Devwex ist ein wichtiges Merkmal. So ist die
Implementierung vom Laufzeit-Container und der Server eine Herausforderung um
mit minimalsten Mitteln ein Maximum an Funktionalität zu erreichen und dabei
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
- Alle Module aus der Sektion `INITIALIZE` werden geladen und über den
  Konstruktor `Module(String options)` initialisiert und registriert.
- Alle Server werden ermittelt, indem nach Sektionen gesucht wird, die auf
 `INI` enden und zu denen eine Implementierung im Klassenpfad gefunden werden
  kann. Die gefundenen Server werden geladen, registriert und über den
  Konstruktor `Server(String name, Initialize initialize)` oder
  `Server(String name, Object initialize)` initialisiert. Dazu werden jedem
  Server der Name entsprechend der ermittelten Sektion sowie eine komplette
  Kopie der zentralen Konfiguration als Initialize-Objekt übergeben. Nach
  erfolgreicher Initialisierung wird der Server als (Daemon)Thread gestartet und
  kann seine Arbeit in der Methode `Server.run()` aufnehmen.

### Modulaufruf

- Ist das Modul noch nicht geladen, wird dies aus dem aktuellen Klassenpfad
  ermittelt, über `Module(String options)` initialisiert und registriert. Eine
  Konfiguration wird dabei nicht übergeben, da für Module nur eine zentrale
  Konfiguration in der Sektion `INITIALIZE` vorgesehen ist.
- Ist das Modul bereits geladen, wird die aktuelle Instanz verwendet.

### Neustart

Die Sequenz ist eine Kombination aus [Beenden](#beenden) und [Start](#start).

- Alle registrierten Server-Instanzen werden über die Methode `Server.destroy()`
  zum Beenden aufgefordert.
- Alle registrierten Module werden über die Methode `Module.destroy()` zum
  Beenden aufgefordert.
- Das Einleiten vom Beenden der Server verläuft asynchron. Der
  Laufzeit-Container wartet auf das Ende aller registrierten Server.
- Alle Module und Server werden durch das Verwerfen vom aktuell verwendeten
  ClassLoader entladen.
- Der Klassenpfad wird um alle Dateien der Verzeichnisse erweitert, die mit dem
  VM-Argument `-Dlibraries` angegeben wurden.
- Alle Module aus der Sektion `INITIALIZE` werden geladen und über den
  Konstruktor `Module(String options)` initialisiert und registriert.
- Alle Server werden ermittelt, indem nach Sektionen gesucht wird, die auf `INI`
  enden und zu denen eine Implementierung im Klassenpfad gefunden werden kann.
  Die gefundenen Server werden geladen, registriert und über den Konstruktor
  `Server(String name, Initialize initialize)` oder
  `Server(String name, Object initialize)` initialisiert. Dazu werden jedem
  Server der Name entsprechend der ermittelten Sektion sowie eine komplette
  Kopie der zentralen Konfiguration als Initialize-Objekt übergeben. Nach
  erfolgreicher Initialisierung wird der Server als (Daemon)Thread gestartet und
  kann seine Arbeit in der Methode `Server.run()` aufnehmen.

### Beenden

- Alle registrierten Server-Instanzen werden über die Methode `Server.destroy()`
  zum Beenden aufgefordert.
- Alle registrierten Module werden über die Methode `Module.destroy()` zum
  Beenden aufgefordert.
- Das Einleiten vom Beenden der Server verläuft asynchron. Der
  Laufzeit-Container wartet auf das Ende aller registrierten Server.
- Alle Module und Server werden durch das Verwerfen vom aktuell verwendeten
  ClassLoader entladen.


## Erweiterung

Die Erweiterung von Seanox Devwex ist auf mehrere Arten möglich. Der Service
stellt dazu eine Server-, Module- und HTTP-Module-API bereit. Spezielle
Interfaces und Abstraktionen sind im Service selbst nicht enthalten, dieser
steuert alle Server und Module über Reflections und stille Standards/Interfaces,
was u.U. detaillierte Kenntnisse über die Komponenten und Arbeitsweise von
Seanox Devwex erfordert.

Einfacher und komfortabler gestaltet sich die Implementierung mit Seanox Commons
als Bestandteil vom Seanox Devwex SDK (für Seanox Devwex 5.x in Entwicklung),
welche die erforderlichen Interfaces sowie Abstraktionen enthalten und
zusätzliche ClassLoader-Ebenen bereitstellen.

Im Nachfolgenden werden ausschliesslich die im Service enthaltene Server-,
Module- und HTTP-Module-API beschrieben.


## Server

Server stellen den physischen Zugriff im Netzwerk für ein Protokoll an einer
Adresse und einem Port zur Verfügung. Seanox Devwex bindet Server über die
Server-API (SAPI) ein. Mit der API lassen sich auch bestehende Server- und
Netzwerk-Funktionalitäten ändern bzw. neue bereitstellen.

### API (SAPI)

Die Server-API basiert auf der Implementierung vom Runnable-Interface. Die
Initialisierung erfolgt über den Konstruktor `Server(String name, Initialize
    initialize)` oder `Server(String name, Object initialize)`. Die Reihenfolge
bei der Initialisierung wird durch die Reihenfolge der Server-Konfigurationen in
der Konfigurationsdatei festgelegt. Die erstellten Server-Instanzen werden vom
Laufzeit-Container (Service) mit der Methode `Server.run()` gestartet und per
`Service.destroy()` zum Beenden aufgefordert. Der Aufruf beider Methoden ist
asynchron. Verzögert sich das Beenden, wartet der Laufzeit-Container auf das
Ende aller registrierten Server-Instanzen. Optional wird die Implementierung der
Methode `Server.explain()` zur Abfrage allgemeiner Informationen, wie Protokoll
und Netzwerkverbindung, unterstützt.


- - -

[Steueren und Überwachen](control.md) | [Inhalt](README.md)
