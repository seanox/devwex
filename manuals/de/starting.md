[Konfiguration](configuration.md) | [Inhalt](README.md) | [Steuerung und Überwachung](control.md)
- - -

# Starten und Stoppen

Seanox Devwex ist eine Java-Konsolen-Anwendung. Nach dem Öffnen der Konsole
(Shell/Eingabeaufforderung) wird in das Arbeitsverzeichnis `./devwex/program`
gewechselt und Seanox Devwex mit dem auszuführenden Befehl als Java-Binary oder
über die verfügbaren Startskripte aufgerufen. Die Gross- und Kleinschreibung
kann bei den Befehlen unbeachtet bleiben. 

> Aufruf mit dem Java-Binary
>
> ```
> java -cp devwex.jar com.seanox.devwex.Service &lg;command&gt;

> Aufruf per Batch-Skript in Windows
>
> ```
> devwex.cmd &lg;command&gt;
> ```

> Aufruf per Shell-Skript in Unix/Linux/MacOS
> 
> ```
> devwex.sh &lg;command&gt;
> ```

Bei laufwender Anwendung kann die Server-Steuerung zudem per Telnet aufgerufen
werden.

> Aufruf mit Telnet
> 
> ```
> telnet 127.0.0.1:25000
> &lg;command&gt;
> ```


## Inhalt
- [Start](#start)
- [Status](#status)
- [Restart](#restart)
- [Stop](#stop)


## Start

Startet Seanox Devwex entsprechend der aktuellen Konfiguration.

Mit dem Start können weitere VM-Argumente als Eigenschaften oder
Umgebungsvariablen übergeben werden. Eigenschaften werden dabazu im Format
`-Dproperty=value` direkt beim Java-Aufruf gesetzt. Umgebungsvariablen werden
vor dem Programmaufruf im Betriebssystem gesetzt. In beiden Fällen lassen sich
VM-Argumente und Umgebungsvariablen als dynamische Schlüssel in der
Konfigurationsdatei `devwex.ini` verwenden. Dabei haben Eigenschaften
(VM-Argumente) eine höhere Priorität als Umgebungsvariablen.

> Übergabe von VM-Argumenten mit dem Programmstart
>
> ```
> java -cp devwex.jar -Dargument1=value1 -Dargument2=value2 com.seanox.devwex.Service start
> ```
>
> Mit dem Programmaufruf zum Start werden die VM-Argumente `argument1` und
> `argument2` übergeben. Diese lassen sich dann in der Java-Implementierung und
> in der Konfiguration verwenden. In der Konfiguration sind die VM-Argumente als
> dynamische Schlüssel verfügbar (siehe Abschnitt [Konfiguration -
>    Konfigurationsdatei](configuration.md#configurationsdatei).

Einen festen Bestandteil bildet das VM-Argument `libraries`. Mit diesem können
weitere Ressourcen, Bibliotheken und Klassen übergeben werden, deren Pfade durch
den für das Betriebssystem festgelegten Path-Separator getrennt werden. Beim
Start erweitert Seanox Devwex automatisch seinen Klassenpfad (ClassPath) um alle
so angegebenen Dateien sowie um die Dateien der so angegebenen Verzeichnisse.

> Übergabe von VM-Argumenten mit dem Programmstart
>
> ```
> java -cp devwex.jar -Dlibraries="../libraries" com.seanox.devwex.Service start
> ```
>
> Mit dem Programmaufruf zum Start wird das VM-Argument `libraries` übergeben.
> Dieses lässt sich dann in der Java-Implementierung und in der Konfiguration
> verwenden.


## Status

Zeigt Informationen zu Version, Start- und Systemzeit sowie den geladenen
Modulen und Servern.


## Restart

Beendet Seanox Devwex inkl. aller aktiven Module und Server und startet mit neu
geladener Konfiguration. Sollte die neue Konfiguration betriebsbehindernde
Fehler enthalten, wird die zuletzt verwendete Konfiguration wiederverwendet.


## Stop

Beendet Seanox Devwex inkl. aller aktiven Module und Server.


- - -

[Konfiguration](configuration.md) | [Inhalt](README.md) | [Steuerung und Überwachung](control.md)
