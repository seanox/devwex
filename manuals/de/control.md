[Starten und Stoppen](starting.md) | [Inhalt](README.md) | [Entwicklung](development.md)
- - -

# Steuerung und Überwachung

Funktionen für Neustart, Stopp und zur Abfrage vom Betriebsstatus sind über
einen Telnet-basierten Fernzugriff zugänglich. Die meisten Betriebssysteme
stellen einen entsprechenden Client bereit. Seanox Devwex selbst enthält
ebenfalls eine passende Client-Komponente.

Der Telnet-Request erwartet lediglich den auszuführenden Befehl, wobei die
Gross- und Kleinschreibung unbeachtet bleiben kann. 


## Inhalt
- [Client](#client)
- [Status](#status)
- [Restart](#restart)
- [Stop](#stop)


## Client

Bei der Verwendung von Seanox Devwex als Telnet-Client, werden die
Verbindungsdaten aus der Server-Konfiguration `devwex.ini`, welche sich im
Arbeitsverzeichnis befinden muss, ermittelt.

> Aufruf mit dem Java-Binary
>
> ```
> java -cp devwex.jar com.seanox.devwex.Service <command>

> Aufruf per Batch-Skript in Windows
>
> ```
> devwex.cmd <command>
> ```

> Aufruf per Shell-Skript in Unix/Linux/MacOS
> 
> ```
> devwex.sh <command>
> ```

> Aufruf mit Telnet
> 
> ```
> telnet 127.0.0.1 25000
> <command>
> ```


## Status

Zeigt Informationen zu Version, Start- und Systemzeit sowie den geladenen
Modulen und Servern.

> ```
> telnet 127.0.0.1 25000
> status
> ```
>
> Ausgabe 
>
> ```
> VERS: 0.0.0 00000000
> TIME: 2017-07-01 06:00:00
> TIUP: 2017-07-01 06:00:00
> XAPI: Seanox-SSX/5.0 20170101
> XAPI: Seanox-SSI/5.0 20170101
> XAPI: Seanox-WebDAV/5.0 20170101
> SAPI: TCP 127.0.0.1:25000
> SAPI: TCP 127.0.0.1:443
> SAPI: TCP 127.0.0.1:80
> ```
>
> Die Ausgabe vom Betriebsstatus ist mehrzeilig und jede Zeile beginnt mit einem
> Präfix gefolgt vom Wert.
> 
> `VERS` Versionsinformation
> `TIME` Aktuelle Systemzeit
> `TIUP` Initiale Startzeit vom Service
> `XAPI` Extension-API (Modulidentifikation: Hersteller-Name/Version)  
> `SAPI` Server-API (Serveridentifikation: Protokoll Adresse:Port)


## Restart

Beendet Seanox Devwex inkl. aller aktiven Module und Server und startet mit neu
geladener Konfiguration. Sollte die neue Konfiguration betriebsbehindernde
Fehler enthalten, wird die zuletzt verwendete Konfiguration wiederverwendet.

> ```
> telnet 127.0.0.1 25000
> restart
> ```
>
> Ausgabe
> 
> ```
> SERVICE RESTARTED
> ```


## Stop

Beendet Seanox Devwex inkl. aller aktiven Module und Server.

> ```
> telnet 127.0.0.1 25000
> stop
> ```
>
> Ausgabe
> 
> ```
> SERVICE STOPPED
> ```
      
      
- - -

[Starten und Stoppen](starting.md) | [Inhalt](README.md) | [Entwicklung](development.md)
