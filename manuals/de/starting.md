[Konfiguration](configuration.md) | [Inhalt](README.md) | [Steuerung und �berwachung](control.md)
- - -

# Starten und Stoppen

Seanox Devwex ist eine Java-Konsolen-Anwendung. Nach dem �ffnen der Konsole
(Shell/Eingabeaufforderung) wird in das Arbeitsverzeichnis `./devwex/program`
gewechselt und Seanox Devwex mit dem auszuf�hrenden Befehl als Java-Binary oder
�ber die verf�gbaren Startskripte aufgerufen. Die Gross- und Kleinschreibung
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

Mit dem Start k�nnen weitere VM-Argumente als Eigenschaften oder
Umgebungsvariablen �bergeben werden. Eigenschaften werden dabazu im Format
`-Dproperty=value` direkt beim Java-Aufruf gesetzt. Umgebungsvariablen werden
vor dem Programmaufruf im Betriebssystem gesetzt. In beiden F�llen lassen sich
VM-Argumente und Umgebungsvariablen als dynamische Schl�ssel in der
Konfigurationsdatei `devwex.ini` verwenden. Dabei haben Eigenschaften
(VM-Argumente) eine h�here Priorit�t als Umgebungsvariablen.

> �bergabe von VM-Argumenten mit dem Programmstart
>
> ```
> java -cp devwex.jar -Dargument1=value1 -Dargument2=value2 com.seanox.devwex.Service start
> ```
>
> Mit dem Programmaufruf zum Start werden die VM-Argumente `argument1` und
> `argument2` �bergeben. Diese lassen sich dann in der Java-Implementierung und
> in der Konfiguration verwenden. In der Konfiguration sind die VM-Argumente als
> dynamische Schl�ssel verf�gbar (siehe Abschnitt [Konfiguration -
>    Konfigurationsdatei](configuration.md#configurationsdatei).

Einen festen Bestandteil bildet das VM-Argument `libraries`. Mit diesem k�nnen
weitere Ressourcen, Bibliotheken und Klassen �bergeben werden, deren Pfade durch
den f�r das Betriebssystem festgelegten Path-Separator getrennt werden. Beim
Start erweitert Seanox Devwex automatisch seinen Klassenpfad (ClassPath) um alle
so angegebenen Dateien sowie um die Dateien der so angegebenen Verzeichnisse.

> �bergabe von VM-Argumenten mit dem Programmstart
>
> ```
> java -cp devwex.jar -Dlibraries="../libraries" com.seanox.devwex.Service start
> ```
>
> Mit dem Programmaufruf zum Start wird das VM-Argument `libraries` �bergeben.
> Dieses l�sst sich dann in der Java-Implementierung und in der Konfiguration
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

[Konfiguration](configuration.md) | [Inhalt](README.md) | [Steuerung und �berwachung](control.md)
