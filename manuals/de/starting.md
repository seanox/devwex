[Konfiguration](configuration.md) | [Inhalt](README.md) | [Steuerung und Überwachung](control.md)
- - -

# Starten und Stoppen

Seanox Devwex ist eine Java-Konsolen-Anwendung. Nach dem Öffnen der Konsole
(Shell/Eingabeaufforderung) wird in das Arbeitsverzeichnis `./devwex/program`
gewechselt und Seanox Devwex mit dem auszuführenden Befehl als Java-Binary oder
über die verfügbaren Startskripte aufgerufen.


## Inhalt
- [Start](#start)
- [Restart](#restart)
- [Status](#status)
- [Stop](#stop)


## Start

Startet Seanox Devwex entsprechend der aktuellen Konfiguration.

> Start mit dem Java-Binary
>
> ```
> java -cp devwex.jar com.seanox.devwex.Service start

> Start per Batch-Skript in Windows
>
> ```
> devwex.cmd restart
> ```

> Start per Shell-Skript in Unix/Linux/MacOS
> 
> ```
> devwex.sh restart
> ```

Mit dem Aufruf können weitere VM-Argumente als Eigenschaften oder
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
> Mit dem Programmaufruf werden die VM-Argumente `argument1` und `argument2`
> übergeben. Diese lassen sich dann in der Java-Implementierung und in der
> Konfiguration verwenden. In der Konfiguration sind die VM-Argumente als
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

## Restart

Beendet Seanox Devwex inkl. aller aktiven Module und Server und startet mit neu
geladener Konfiguration. Sollte die neue Konfiguration betriebsbehindernde
Fehler enthalten, wird die zuletzt verwendete Konfiguration wiederverwendet.

> Restart mit dem Java-Binary
>
> ```
> java -cp devwex.jar com.seanox.devwex.Service restart

> Restart per Batch-Skript in Windows
>
> ```
> devwex.cmd restart
> ```

> Restart per Shell-Skript in Unix/Linux/MacOS
> 
> ```
> devwex.sh restart
> ```

> Restart mit Telnet
> 
> ```
> telnet 127.0.0.1:25000 restart
> ```

## Status

Zeigt Informationen zu Version, Start- und Systemzeit sowie den geladenen
Modulen und Servern.

> Statusabfrage mit dem Java-Binary
>
> ```
> java -cp devwex.jar com.seanox.devwex.Service status
> ```

> Statusabfrage per Batch-Skript in Windows
>
> ```
> devwex.cmd status
> ```

> Statusabfrage per Shell-Skript in Unix/Linux/MacOS
>
> ```
> devwex.sh status
> ```

> Statusabfrage mit Telnet
>
> ```
> telnet 127.0.0.1:25000 status
> ```


## Stop

Beendet Seanox Devwex inkl. aller aktiven Module und Server.

> Stoppen mit dem Java-Binary
>
> ```
> java -cp devwex.jar com.seanox.devwex.Service stop
> ```

> Stoppen per Batch-Skript in Windows
>
> ```
> devwex.cmd stop
> ```

> Stoppen per Shell-Skript in Unix/Linux/MacOS
>
> ```
> devwex.sh stop
> ```

> Stoppen mit Telnet
>
> ```
> telnet 127.0.0.1:25000 stop
> ```


- - -

[Konfiguration](configuration.md) | [Inhalt](README.md) | [Steuerung und Überwachung](control.md)
