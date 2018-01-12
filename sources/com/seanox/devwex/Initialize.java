/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 *  Devwex, Advanced Server Development
 *  Copyright (C) 2018 Seanox Software Solutions
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of version 2 of the GNU General Public License as published
 *  by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.seanox.devwex;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

/**
 *  Initialize, verarbeitet Initialisierungsdaten im INI-Format und stellt diese
 *  als Sektionen zur Verf&uuml;gung.
 *  
 *  <h3>Hinweis</h3>
 *  F&uuml;r eine optimale Verarbeitung von INI-Dateien sollte immer die 
 *  Kombination von Initialize mit Section verwendet werden. So basiert die
 *  nachfolgende Beschreibung auf der Kombination beider Komponenten.<br>
 *  <br>
 *  Das für die Konfiguration verwendete INI-Format ist eine kompatible
 *  Erweiterung zum klassischen Format. Es ist ebenfalls zeilenbasiert und
 *  verwendet Sektionen in denen Schl&uuml;ssel mit Werten abgelegt sind. Beim
 *  Namen von Sektion und Schl&uuml;ssel wird die Gross- und Kleinschreibung
 *  ignoriert. Gleichnamige Deklarationen f&uuml;hren zum &Uuml;berschreiben
 *  von Sektionen und Schl&uuml;sseln.<br>
 *  <br>
 *  In der Erweiterung lassen sich u.a. Sektionen vererben. Dazu folgt einer
 *  Sektion das Sch&uuml;sselwort {@code EXTENDS} und gefolgt von den Namen
 *  referenzierter Sektionen. Damit &uuml;bernimmt die Sektion alle
 *  Schl&uuml;ssel und Werte der referenzierten Sektionen und kann diese
 *  erweitern oder &uuml;berschreiben.<br>
 *  <br>
 *  Das Zuweisen von Werten zu Schl&uuml;sseln in einer Sektion erfolgt mit dem
 *  Gleichheitszeichen. Abweichend vom Orginalformat, kann die Zuweisung in der
 *  Folgezeile ohne erneute Angabe des Schl&uuml;ssels durch die Verwendung vom
 *  Pluszeichen fortgesetzt werden. Werte lassen sich zudem fest, variabel und
 *  optional zuweisen.<br>
 *  Durch die Option {@code [?]} am Ende eines Schl&uuml;ssels, wird nach einem
 *  gleichnamigen Schl&uuml;ssel in den System-Properties der
 *  Java-Laufzeitumgebung gesucht. Kann keiner ermittelt werden, wird der
 *  optional angegebene Wert verwendet. Ohne Wert gilt ein solcher
 *  Schl&uuml;ssel als nicht angegeben und wird ignoriert.<br>
 *  <br>
 *  Kommentare beginnen mit einem Semikolon, sind optional und lassen sich an
 *  jeder beliebigen Stelle in einer Zeile verwenden. Die nachfolgenden Zeichen
 *  sind somit kein Bestandteil von Sektion, Schl&uuml;ssel oder Wert.<br>
 *  Mit der Option {@code [+]} am Ende eines Schl&uuml;ssels, kann die
 *  Verwendung von Kommentaren f&uuml;r diesen Schl&uuml;ssel deaktiviert und
 *  das Semikolon im Wert verwendet werden.<br>
 *  <br>
 *  Sektionen, Schl&uuml;ssel und Werte unterst&uuml;tzen auch eine
 *  hexadezimale Schreibweise. Diese beginnt mit {@code 0x...}, gefolgt von der
 *  hexadezimalen Zeichenfolge. Diese Schreibweise kann immer nur auf das
 *  komplette Element angewandt werden. Die Kombination oder Unterbrechung ist
 *  nicht m&ouml;glich.
 *  
 *  <h3>Beispiel</h3>
 *  <pre>
 *     001 [SECTION] EXTENDS SECTION-A SECTION-B      ;Kommentar
 *     002   PARAM-A                 = WERT-1         ;Kommentar
 *     003   PARAM-B             [+] = WERT-2; WERT-3
 *     004                           + WERT-4; WERT-5
 *     005   PARAM-C          [?][+] = WERT-6; WERT-7
 *     006   PARAM-E          [?]                     ;Kommentar
 *     007
 *     008 [0x53454354494F4E2D41]
 *     009   PARAM-A                 = 0x574552542D31 ;Kommentar
 *     010   0x504152414D2D42        = WERT-2         ;Kommentar 
 *     011   0x504152414D2D43    [+] = 0x574552542D33
 *     012   PARAM-D                 = 0x574552542D34
 *     013                           + 0x574552542D35
 *     014   PARAM-E          [?][+] = 0x574552542D363B20574552542D37
 *     015   0x504152414D2D45 [?]                     ;Kommentar
 *  </pre>
 *  
 *  <h4>Zeile 1</h4>
 *  Die Sektion mit dem Namen {@code SECTION} wird definiert. Das
 *  Schl&uuml;sselwort {@code EXTENDS} verweist auf die Ableitung von den
 *  Sektionen {@code SECTION-A} und {@code SECTION-B}. Somit basiert
 *  {@code SECTION} auf den Schl&uuml;sseln und Werten der Sektionen
 *  {@code SECTION-A} und {@code SECTION-B}. Ab dem Semikolon werden die
 *  nachfolgenden Zeichen als Kommentar interpretiert.
 *  
 *  <h4>Zeile 2</h4>
 *  Dem Schl&uuml;ssel {@code PARAM-A} wird der Wert {@code WERT-1} zugewiesen.
 *  Die nachfolgenden Zeichen werden ab dem Semikolon als Kommentar
 *  interpretiert.
 *  
 *  <h4>Zeile 3</h4>
 *  Dem Schl&uuml;ssel {@code PARAM-B} wird {@code WERT-2; WERT-3} als Wert
 *  zugewiesen. Durch die Option {@code [+]} am Ende vom Schl&uuml;ssel, wird
 *  der Zeilenkommentar abgeschaltet und alle Zeichen f&uuml;r die
 *  Wertzuweisung verwendet. Die Angabe eines Kommentars ist in dieser Zeile
 *  nicht m&ouml;glich.
 *  
 *  <h4>Zeile 4</h4>
 *  Die Wertzuweisung von Zeile 3 wird fortgesetzt und der Wert
 *  {@code WERT-4; WERT-5} dem bestehenden Wert vom Schl&uuml;ssel
 *  {@code PARAM-B} hinzugef&uuml;gt. Die Option {@code [+]} aus Zeile 3 wird
 *  ebenfalls in Zeile 4 &uuml;bernommen, womit auch hier der Zeilenkommentar
 *  abgeschaltet ist und alle Zeichen als Wertzuweisung verwendet werden. Die
 *  Eingabe eines Kommentars ist in dieser Zeile nicht m&ouml;glich. Weitere
 *  vorangestellte Optionen sind nicht m&ouml;glich.
 *
 *  <h4>Zeile 5</h4>
 *  Die Wertzuweisung f&uuml;r den Schl&uuml;ssel {@code PARAM-C} ist
 *  dynamisch. In den System-Properties der Java-Laufzeitumgebung wird dazu
 *  nach dem gleichnamigen Schl&uuml;ssel {@code PARAM-C} gesucht, wobei die
 *  Gross- und Kleinschreibung ignoriert wird. Der Schl&uuml;ssel muss dazu
 *  Bestandteile der Laufzeitumgebung sein oder kann beim Programmstart in der
 *  Form {@code -Dschluessel=wert} gesetzt werden.<br>
 *  Enthalten die System-Properties der Java-Laufzeitumgebung keinen
 *  entsprechenden Schl&uuml;ssel, wird alternativ {@code WERT-6; WERT-7} als
 *  Wert verwendet.<br>
 *  Durch die Kombination mit der Option {@code [+]} am Ende vom
 *  Schl&uuml;ssel, wird der Zeilenkommentar abgeschaltet und alle Zeichen
 *  f&uuml;r die Wertzuweisung verwendet. Die Angabe eines Kommentars ist in
 *  dieser Zeile nicht m&ouml;glich.
 *
 *  <h4>Zeile 6</h4>
 *  Die Wertzuweisung f&uuml;r den Schl&uuml;ssel {@code PARAM-E} ist
 *  dynamisch. In den System-Properties der Java-Laufzeitumgebung wird dazu
 *  nach dem gleichnamigen Schl&uuml;ssel gesucht, wobei die Gross- und
 *  Kleinschreibung ignoriert wird. Der Schl&uuml;ssel muss dazu Bestandteil
 *  der Laufzeitumgebung sein oder kann beim Programmstart in der Form
 *  {@code -Dschluessel=wert} gesetzt werden.<br>
 *  Enthalten die System-Properties der Java-Laufzeitumgebung keinen
 *  entsprechenden Schl&uuml;ssel, wird dieser Schl&uuml;ssel ignoriert, da
 *  auch kein alternativer Wert angegeben wurde.<br>
 *  Kommentare werden in dieser Zeile unterst&uuml;tzt.
 *
 *  <h4>Zeile 8 - 15</h4>
 *  Analog den Beispielen aus Zeile 1 - 6 wird f&uuml;r Sektionen,
 *  Schl&uuml;ssel und Werte die hexadezimale Schreibweise verwendet.<br>
 *  <br>
 *  Initialize 5.0.1 20180109<br>
 *  Copyright (C) 2018 Seanox Software Solutions<br>
 *  Alle Rechte vorbehalten.
 *
 *  @author  Seanox Software Solutions
 *  @version 5.0.1 20180109
 */
public class Initialize implements Cloneable {

    /** Hashtable der Sektionen */
    private volatile LinkedHashMap entries;
    
    /** Option zur Aktivierung vom Smart-Modus */
    private volatile boolean smart;

    /** Konstruktor, richtet Initialize ein.*/
    public Initialize() {
        this(false);
    }
    
    /** 
     *  Konstruktor, richtet Initialize ein.
     *  @param smart aktiviert den smarten Modus
     */
    public Initialize(boolean smart) {
        
        this.entries = new LinkedHashMap();
        this.smart   = smart;
    }

    /**
     *  Dekodiert ggf. hexadezimale Werte in einen String.
     *  @param  string zu dekodierender String
     *  @return der dekodierte und getrimmte String
     */                   
    private static String decode(String string) {

        string = string == null ? "" : string.toUpperCase().trim();
        if (string.matches("^(?i)0x([0-9a-f]{2})+$"))
            return new String(new BigInteger(string.substring(2), 16).toByteArray()).toUpperCase().trim();
        return string;
    }

    /**
     *  Ermittelt aus dem String die enthaltenen Sektionen.
     *  Das Parsen ignoriert ung&uuml;ltige Sektionen, Schl&uuml;ssel und Werte.
     *  R&uuml;ckgabe die ermittelten Sektionen als Initialize.
     *  @param  text zu parsende Sektionen
     *  @return die ermittelten Sektionen als Initialize
     */
    public static Initialize parse(String text) {
        return Initialize.parse(text, false);
    }
    
    /**
     *  Ermittelt aus dem String die enthaltenen Sektionen.
     *  Das Parsen ignoriert ung&uuml;ltige Sektionen, Schl&uuml;ssel und Werte.
     *  R&uuml;ckgabe die ermittelten Sektionen als Initialize.
     *  Mit der Option {@code smart} kann ein smartes Verhalten aktiviert
     *  werden. Dadurch &uml;bernimmt {@link #parse(String, boolean)} nur
     *  Schl&uuml;ssel mit Sektionen die nicht leer sind. Die Methoden
     *  {@link #get(String)} verh&auml;lt sich bei nicht existierenden
     *  Schl&uuml;sseln so, als l&auml;gen diese mit einer leeren Sektion vor
     *  und liefert so nie {@code null}. Zudem reagiert im Smart-Modus
     *  {@link #set(String, String)} bei einem leeren Wert wie die Methode
     *  {@link #remove(String)} und entfernt den Schl&uuml;ssel.
     *  @param  text  zu parsender String
     *  @param  smart aktiviert den smarten Modus
     *  @return die ermittelten Sektionen als Initialize
     */
    public static Initialize parse(String text, boolean smart) {

        Enumeration     enumeration;
        Initialize      initialize;
        LinkedHashMap   entries;
        String          line;
        String          section;
        String          value;
        StringBuffer    buffer;
        StringTokenizer tokenizer;
        
        String[]        strings;
        
        int             index;
        
        initialize = new Initialize(smart);

        if (text == null)
            return initialize;

        entries = new LinkedHashMap();
        buffer  = null;

        tokenizer = new StringTokenizer(text, "\r\n");
        while (tokenizer.hasMoreTokens()) {

            line = ((String)tokenizer.nextElement()).trim();
            
            if (line.startsWith("[")) {
                
                buffer = new StringBuffer();
                
                //die Zeile wird wie folgt verarbeitet:
                //  - der eindeutige/gueltige Name der Sektion wird ermittelt
                //  - der Kommentarteil wird entfernt
                //  - nachfolgende Sektionen sind nicht zulaessig und werden entfernt
                //  - ggf. existierende Ableitungen werden
                strings = line.replaceAll("^(?i)(?:\\[\\s*([^\\[\\]\\;]+)\\s*\\]\\s*(?:extends\\s+([^\\[\\]\\;]+))*)*.*$", "$1 \00 $2").split("\00");
                
                //die Sektion wird ggf. dekodiert und optimiert 
                section = Initialize.decode(strings[0]);
                
                //nur gueltige Sektionen werden geladen
                if (section.isEmpty())
                    continue;
                
                entries.put(section, buffer);
                
                //ggf. existierende Ableitungen werden registriert und geladen
                strings = strings[1].split("\\s+");
                for (index = 0; index < strings.length; index++) {
                    section = Initialize.decode(strings[index]);
                    if (entries.containsKey(section))
                        buffer.append("\r\n").append(entries.get(section));
                }
                
            } else if (buffer != null) {

                //Inhalt wird nur mit gueltiger Sektion verarbeitet
                buffer.append("\r\n").append(line);
            }
        }
        
        enumeration = Collections.enumeration(entries.keySet());
        while (enumeration.hasMoreElements()) {
            section = (String)enumeration.nextElement();
            value   = ((StringBuffer)entries.get(section)).toString().trim();
            if (!smart || !value.isEmpty())
                initialize.entries.put(section, Section.parse(value, smart));
        }

        return initialize;
    }

    /**
     *  R&uuml;ckgabe aller Sektionen als Enumeration.
     *  @return alle Sektionen als Enumeration
     */
    public synchronized Enumeration elements() {
        return Collections.enumeration(this.entries.keySet());
    }

    /**
     *  R&uuml;ckgabe {@code true}, wenn die Sektion enthalten ist.
     *  @param  key Name der Sektion
     *  @return {@code true} wenn die Sektion enthalten ist
     */
    public synchronized boolean contains(String key) {
        
        if (key != null)
            key = key.toUpperCase().trim();
        if (key == null || key.isEmpty())
            return false;
        return this.entries.containsKey(key);
    }

    /**
     *  R&uuml;ckgabe der angegebenen Section.
     *  Ist dieser der Schl&uuml;ssel nicht enthalten bzw. kann nicht ermittelt
     *  werden, liefert die Methode {@code null} und im Smart-Modus eine leere
     *  Sektion, die nur in Verbindung mit einem g&uml;ltigen Schl&uuml;ssel in
     *  Initialize eingetragen wird. Andernfalls ist die im Smart-Modus
     *  erstellte Sektion ungebunden. 
     *  @param  key Name der Section
     *  @return die ermittelte Section, sonst {@code null} bzw. im Smart-Modus
     *          eine leere Sektion
     */
    public synchronized Section get(String key) {
        
        Section section;
        
        if (key != null)
            key = key.toUpperCase().trim();
        if (key == null
                || key.isEmpty())
            return this.smart ? new Section(true) : null;
        
        section = (Section)this.entries.get(key);
        if (section == null
                && this.smart) {
            section = new Section(true);
            this.set(key, section);
        }
        return section;
    }

    /**
     *  Setzt die entsprechende Sektion mit dem angegebenen Inhalt.
     *  Bestehende Sektionen werden entfernt und neu gesetzt.
     *  Leere Sch&uuml;ssel sind nicht zul&auml;ssig und f&uuml;hren zur
     *  {@link IllegalArgumentException}. Die Methode reagiert im Smart-Modus
     *  zudem bei einem leeren Wert wie {@link #remove(String)} und entfernt
     *  den Schl&uuml;ssel. 
     *  @param  key     Name der Sektion
     *  @param  section Sektion
     *  @return ggf. zuvor zugeordnete Sektion, sonst {@code null}
     */
    public synchronized Section set(String key, Section section) {
        
        if (key != null)
            key = key.toUpperCase().trim();
        if (key == null || key.isEmpty())
            throw new IllegalArgumentException();
        if (section == null && !this.smart)
            return (Section)this.entries.remove(key);
        if (section == null)
            section = new Section(this.smart);
        return (Section)this.entries.put(key, section);
    }

    /**
     *  Entfernt die angegebene Sektion aus Initialize.
     *  @param  key Name der zu entfernenden Sektion
     *  @return ggf. zuvor zugeordnete Sektion, sonst {@code null}
     */
    public synchronized Section remove(String key) {
  
        if (key != null)
            key = key.toUpperCase().trim();
        if (key == null || key.isEmpty())
            return null;
        return (Section)this.entries.remove(key);
    }

    /**
     *  F&uuml;hrt die &uuml;bergebenen Sektionen mit den bestehenden zusammen.
     *  Bereits vorhandene werden dabei aktualisiert, neue angelegt.
     *  @param  initialize zu &uuml;bernehmende Sektionen
     *  @return die aktuelle Instanz mit den zusammgef&uuml;hrten Sektionen
     */
    public synchronized Initialize merge(Initialize initialize) {
        
        Enumeration enumeration;
        Section     section;
        String      entry;
        
        if (initialize == null)
            return this;

        //die Sektionen werden zusammengefasst oder ggf. neu angelegt
        enumeration = Collections.enumeration(this.entries.keySet());
        while (enumeration.hasMoreElements()) {
            entry   = (String)enumeration.nextElement();
            section = initialize.get(entry);
            this.set(entry, section.merge(this.get(entry)));
        }
        
        return this;
    }

    /**
     *  R&uuml;ckgabe der Anzahl von Sektionen.
     *  @return die Anzahl von Sektionen
     */
    public synchronized int size() {
        return this.entries.size();
    }

    /** Setzt Initialize komplett zur&uuml;ck und verwirft alle Sektionen. */
    public synchronized void clear() {
        this.entries.clear();
    }

    /**
     *  R&uuml;ckgabe einer Kopie von Initialize.
     *  @return eine Kopie von Initialize
     */
    public synchronized Object clone() {

        Enumeration enumeration;
        Initialize  initialize;
        String      entry;

        //Initialize wird eingerichtet
        initialize = new Initialize(this.smart);

        //die Sektionen werden kopiert
        enumeration = Collections.enumeration(this.entries.keySet());
        while (enumeration.hasMoreElements()) {
            entry = (String)enumeration.nextElement();
            initialize.entries.put(entry, ((Section)this.entries.get(entry)).clone());
        }
        
        return initialize;
    }
}