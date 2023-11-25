[Systemanforderung](requirements.md) | [Inhalt](README.md) | [Konfiguration](configuration.md)
- - -

# Installation

Seanox Devwex wird als ZIP-Archiv bereitgestellt, das an einer beliebigen Stelle
im Dateisystem entpackt wird.


## Inhalt

- [Windows Service](#windows-service)
- [Linux Service](#linux-service)
- [MacOS Service](#macos-service)

## Windows Service

Für Windows ist eine Distribution mit [Procrun](
    https://commons.apache.org/daemon/procrun.html) verfügbar, die Seanox Devwex
in Windows als Dienst installiert. Die Distribution basiert auf der Java-Version
und enthält zusätzlich die Service-Runner für 32 und 64 Bit sowie die
Batch-Datei `service.cmd` für einen einfachen Umgang mit dem Service. Auch die
Windows-Distribution wird an einer beliebigen Stelle im Dateisystem entpackt. Da
der Service mit einem Windows-Dienstkonto und entsprechenden Zugriffsrechten
laufen sollte, wird die Ablage und Installation ausserhalb der Benutzer-Profile
empfohlen. Standardmässig wird der Service mit dem Windows-Dienstkonto
`NetworkService` eingerichtet, andere Dienstkontos lassen sich ebenfalls
nutzen. Die für das Programverzeichnis benötigten Zugriffsrechte werden bei der
Einrichtung gesetzt. Die Parameter zur Konfiguration des einzurichtenden
Services wurden in der Batch-Datei gebündelt und sind leicht zugänglich.

Zur Installation vom Service wird die Batch-Datei `service.cmd` verwendet.
Dazu wird die Konsole (Shell/Eingabeaufforderung) als Administrator geöffnet und
in das Programm-Verzeichnis `./devwex/program` gewechselt und die Batch-Datei
mit der gewünschten Funktion aufgerufen.

```
usage: service.cmd [command]
```

Übersicht der verfügbaren Befehle:  
Die Schreibweise (Gross- und Kleinschreibung) ist bei den Befehlen zubeachten.

| Befehl      | Beschreibung                                                                   |
| :---        | :---                                                                           |
| `install`   | Installiert den Service.                                                      |
| `update`    | Entfernt den Service und legt diesen mit aktualisierter Konfiguration neu an. |
| `uninstall` | Entfernt den Service.                                                         |
| `start`     | Startet den Service.                                                          |
| `status`    | Zeigt den Status zum laufenden Service an.                                    |
| `restart`   | Beendet den Service und startet diesen neu.                                   |
| `stop`      | Beendet den Service.                                                          |

> [!NOTE]
> Die Distribution enthält kein JRE.
>
> Soll ein bereits installiertes bzw. im System verfügbares verwendete werden,
> kann das Java-Home in `service.cmd` mit der Variablen `java` festgelegt
> werden. Alternativ lässt sich das JRE auch in `./runtime/java`
> bereitgestellen. In dem Fall findet das Batch-Skript die JRE automatisch. 


## Linux Service

TODO:


## MacOS Service

TODO:


- - -

[Systemanforderung](requirements.md) | [Inhalt](README.md) | [Konfiguration](configuration.md)
