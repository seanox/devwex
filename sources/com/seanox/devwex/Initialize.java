/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 *  Devwex, Advanced Server Development
 *  Copyright (C) 2016 Seanox Software Solutions
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *  Initialize, verarbeitet Initialisierungsdaten im INI-Format und stellt diese
 *  als Sektionen zur Verf&uuml;gung.<br>
 *  <br>
 *      <dir>Hinweis</dir>
 *  F&uuml;r eine optimale Verarbeitung von INI-Dateien sollte immer die 
 *  Kombination von Initialize mit Section verwendet werden. So basiert die
 *  nachfolgende Beschreibung auf der Kombination beider Komponenten.<br>
 *  <br>
 *  Das verarbeitete INI-Format wurde zur klassischen Form erweitert. Die
 *  Unterteilung erfolgt auch hier in Sektionen, in denen zeilenweise
 *  Schl&uuml;ssel mit zugeh&ouml;rigen Werten abgelegt sind. Beim Namen von
 *  Sektion und Schl&uuml;ssel wird die Gross- und Kleinschreibung nicht
 *  ber&uuml;cksichtig. Mehrfache Deklarationen werden zusammengef&uuml;hrt,
 *  bereits vorhandene Schl&uuml;ssel &uuml;berschrieben und neue
 *  hinzugef&uuml;gt. Dadurch k&ouml;nnen Sektionen auch geteilt werden, was die
 *  &Uuml;bersichtlichkeit aber meist erschwert.<br>
 *  <br>
 *  Als Erweiterung zum Orginalformat lassen sich Sektionen vererben. Dazu wird
 *  einer Sektion das Sch&uuml;sselwort <code>EXTENDS</code> gefolgt von Namen
 *  referenzierter Sektionen nachgestellt. Damit &uuml;bernimmt die abgeleitete
 *  Sektion alle Schl&uuml;ssel und Werte der referenzierten Sektionen und kann
 *  diese erweitern oder &uuml;berschreiben.<br>
 *  <br>
 *  Das Zuweisen eines Wertes zu einem Schl&uuml;ssel in einer Sektion erfolgt
 *  &uuml;ber das Gleichheitszeichen. Abweichend vom Orginalformat, kann die
 *  Zuweisung in der Folgezeile ohne erneute Angabe des Schl&uuml;ssels und
 *  durch die Verwendung des Pluszeichens fortgesetzt werden. Werte lassen sich
 *  zudem fest, variabel und optional zuweisen. Durch die zus&auml;tzliche
 *  Option <code>[?]</code> am Ende eines Schl&uuml;ssels, wird f&uuml;r diesen
 *  Schl&uuml;ssel den Wert &uuml;ber die System-Properties der
 *  Java-Laufzeitumgebung zu ermitteln. Kann kein Wert ermittelt werden, wird
 *  der optional eingetragene zugewiesen. Ohne Wert gilt ein Schl&uuml;ssel als
 *  nicht angegeben und wird dann ignoriert.<br>
 *  <br>
 *  Kommentare beginnen mit einem Semikolon und sind optional. Wiederum
 *  Abweichend vom Orginalformat kann ein Kommentar an jeder beliebigen Stelle
 *  in einer Zeile verwendet werden. Die nachfolgenden Zeichen sind somit kein
 *  Bestandteil von Sektion, Schl&uuml;ssel oder Wert.<br>
 *  <br>
 *  F&uuml;r Sektionen, Schl&uuml;ssel und Werte wird auch eine hexadezimale
 *  Schreibweise unterst&uuml;tzt. Diese beginnt mit <code>0x...</code>, gefolgt
 *  von der hexadezimalen Zeichenfolge. Diese Schreibweise kann immer nur auf
 *  das komplette Element anwenden. Die Kombination oder Unterbrechung ist nicht
 *  m&ouml;glich.<br>
 *  <br>
 *      <dir>Beispiel</dir>
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
 *      <dir>Zeile 001:</dir>
 *  Die Sektion mit dem Namen <code>SECTION</code> wird definiert. Die Option
 *  <code>EXTENDS</code> verweist auf die Ableitung von den Sektionen
 *  <code>SECTION-A</code> und <code>SECTION-B</code>. Somit basiert die
 *  <code>SECTION</code> auf den Schl&uuml;sseln und Werten der Sektionen
 *  <code>SECTION-A</code> und <code>SECTION-B</code>. Ab dem Semikolon werden
 *  die nachfolgenden Zeichen als Kommentar interpretiert.<br>
 *  <br>
 *      <dir>Zeile 002:</dir>
 *  Dem Schl&uuml;ssel <code>PARAM-A</code> wird der Wert <code>WERT-1</code>
 *  zugewiesen. Die nachfolgenden Zeichen werden ab dem Semikolon als Kommentar
 *  interpretiert.<br>
 *  <br>
 *      <dir>Zeile 003</dir>
 *  Dem Schl&uuml;ssel <code>PARAM-B</code> wird <code>WERT-2; WERT-3</code> als
 *  Wert zugewiesen. Durch die Option <code>[+]</code> am Ende vom
 *  Schl&uuml;ssel, wird der Zeilenkommentar abgeschaltet und alle Zeichen
 *  f&uuml;r die Wertzuweisung verwendet. Die Eingabe eines Kommentars ist in
 *  dieser Zeile nicht m&ouml;glich.<br>
 *  <br>
 *      <dir>Zeile 004</dir>
 *  Die Wertzuweisung von Zeile 003 wird fortgesetzt und der Wert
 *  <code>WERT-4; WERT-5</code> dem bestehenden Wert vom Schl&uuml;ssel
 *  <code>PARAM-B</code> hinzugef&uuml;gt. Die Option <code>[+]</code> aus
 *  Zeile 003 wird ebenfalls in Zeile 004 &uuml;bernommen, womit auch hier
 *  der Zeilenkommentar abgeschaltet ist und alle Zeichen als Wertzuweisung
 *  verwendet werden. Die Eingabe eines Kommentars ist in dieser Zeile nicht
 *  m&ouml;glich. Weitere vorangestellte Optionen sind nicht m&ouml;glich.<br>
 *  <br>
 *      <dir>Zeile 005</dir>
 *  Die Wertzuweisung f&uuml;r den Schl&uuml;ssel <code>PARAM-C</code> erfolgt
 *  dynamisch. In den System-Properties der Java-Laufzeitumgebung wird dazu nach
 *  einem Wert f&uuml;r zum Schl&uuml;ssel <code>PARAM-C</code> gesucht, wobei
 *  die Gross- und Kleinschreibung ignoriert wird. Dazu muss der Schl&uuml;ssel
 *  Bestandteile der Laufzeitumgebung sein oder kann beim Programmstart in der
 *  Form <code>-Dschluessel=wert</code> gesetzt werden.<br>
 *  Wird in den System-Properties der Java-Laufzeitumgebung kein entsprechender
 *  Schl&uuml;ssel ermnittelt, wird alternativ <code>WERT-6; WERT-7</code> als
 *  Wert verwendet.<br>
 *  Durch die Kombination mit der Option <code>[+]</code> am Ende vom
 *  Schl&uuml;ssel, wird der Zeilenkommentar abgeschaltet und alle Zeichen
 *  f&uuml;r die Wertzuweisung verwendet. Die Eingabe eines Kommentars ist in
 *  dieser Zeile nicht m&ouml;glich.<br>
 *  <br>
 *      <dir>Zeile 006</dir>
 *  Die Wertzuweisung f&uuml;r den Schl&uuml;ssel <code>PARAM-E</code> erfolgt
 *  dynamisch. In den System-Properties der Java-Laufzeitumgebung wird dazu nach
 *  einem Wert f&uuml;r zum Schl&uuml;ssel <code>PARAM-E</code> gesucht, wobei
 *  die Gross- und Kleinschreibung ignoriert wird. Dazu muss der Schl&uuml;ssel
 *  Bestandteile der Laufzeitumgebung sein oder kann beim Programmstart in der
 *  Form <code>-Dschluessel=wert</code> gesetzt werden.<br>
 *  Wird in den System-Properties der Java-Laufzeitumgebung kein entsprechender
 *  Schl&uuml;ssel ermnittelt, wird dieser Schl&uuml;ssel ignoriert, da auch
 *  kein alternativer Wert angegeben wurde.<br>
 *  Kommentare werden in dieser Zeile unterst&uuml;tzt.<br>
 *  <br>
 *      <dir>Zeile 008 - 015</dir>
 *  Analog den Beispielen aus Zeile 001 - 006 wird für Sektionen, Schl&uuml;ssel
 *  und Werte die hexadezimale Schreibweise unterst&uuml;tzt.<br>
 *  <br>
 *  Initialize 5.0 20160808<br>
 *  Copyright (C) 2016 Seanox Software Solutions<br>
 *  Alle Rechte vorbehalten.
 *
 *  @author  Seanox Software Solutions
 *  @version 5.0 20161126
 */
public class Initialize implements Cloneable {

    /** Hashtable der Sektionen */
    private volatile LinkedHashMap entries;
    
    /** Hashtable der Ableitungen in den Sektionen (extends) */
    private volatile LinkedHashMap follower;
    
    /** Option zum automatischen Anlegen nicht existierender Sektionen */
    private volatile boolean smart;

    /** Konstruktor, richtet Initialize ein.*/
    public Initialize() {
        this(false);
    }
    
    /** 
     *  Konstruktor, richtet Initialize ein.
     *  @param smart automatisches Anlegen nicht existierender Sektionen
     */
    public Initialize(boolean smart) {
        
        this.entries  = new LinkedHashMap();
        this.follower = new LinkedHashMap();
        this.smart    = smart;
    }

    /**
     *  Dekodiert ggf. hexadezimale Werte in einen String.
     *  @param  string zu dekodierender String
     *  @return der dekodierte und getrimmte String
     */                   
    private static String decode(String string) {

        string = string == null ? "" : string.trim();
        if (string.length() > 0
                && string.length() % 2 == 0
                && string.matches("^0x[0-9A-Fa-f]+$")) {
            string = new String(new BigInteger(string.substring(2), 16).toByteArray()).trim();
        }
    
        return string.toUpperCase();
    }

    /**
     *  Ermittelt aus dem String die enthaltenen Sektionen.
     *  R&uuml;ckgabe die ermittelten Sektionen als Initialize.
     *  @param  string zu parsender String
     *  @return die ermittelten Sektionen als Initialize
     */
    public static Initialize parse(String string) {
        return Initialize.parse(string, false);
    }
    
    /**
     *  Ermittelt aus dem String die enthaltenen Sektionen.
     *  R&uuml;ckgabe die ermittelten Sektionen als Initialize.
     *  @param  string zu parsender String
     *  @param  smart  automatisches Anlegen nicht existierender Sektionen
     *  @return die ermittelten Sektionen als Initialize
     */
    public static Initialize parse(String string, boolean smart) {

        Initialize      initialize;
        StringBuffer    buffer;
        String          line;
        String          section;
        StringTokenizer tokenizer;
        Iterator        iterator;
        Vector          follower;
        
        String[]        strings;
        
        int             index;
        
        initialize = new Initialize(smart);

        if (string == null) return initialize;

        buffer = null;

        tokenizer = new StringTokenizer(string, "\r\n");
        while (tokenizer.hasMoreTokens()) {

            line = ((String)tokenizer.nextElement()).trim();
            
            if (line.startsWith("[")) {
                
                buffer = null;

                //die Zeile wird wie folgt verarbeitet:
                //  - der eindeutige/gueltige Name der Sektion wird ermittelt
                //  - der Kommentarteil wird entfernt
                //  - nachfolgende Sektionen sind nicht zulaessig und werden entfernt
                //  - ggf. existierende Ableitungen werden
                strings = line.replaceAll("^(?i)(?:\\[\\s*([^\\[\\]\\;]+)\\s*\\]\\s*(?:extends\\s+([^\\[\\]\\;]+))*)*.*$", "$1 \00 $2").split("\00");
                
                //die Sektion wird ggf. dekodiert und optimiert 
                section = Initialize.decode(strings[0]);
                
                //nur gueltige Sektionen werden geladen
                if (section.length() == 0) continue;

                //die Ableitungen werden eingerichtet
                follower = (Vector)initialize.follower.get(section);
                if (follower == null)
                    follower = new Vector();
                initialize.follower.put(section, follower);
                
                buffer = (StringBuffer)initialize.entries.getOrDefault(section, new StringBuffer());
                initialize.entries.put(section, buffer);
                
                //ggf. existierende Ableitungen werden registriert und geladen
                strings = strings[1].split("\\s+");
                for (index = 0; index < strings.length; index++) {
                    follower.add(strings[index]);
                    section = Initialize.decode(strings[index]);
                    if (initialize.entries.containsKey(section))
                        buffer.append("\r\n").append(initialize.entries.get(section));
                }
                
            } else if (buffer != null) {

                //Inhalt wird nur mit gueltiger Sektion verarbeitet
                buffer.append("\r\n").append(line);
            }
        }
        
        iterator = initialize.entries.keySet().iterator();
        while (iterator.hasNext()) {
            String entry = (String)iterator.next();
            initialize.entries.put(entry, Section.parse(((StringBuffer)initialize.entries.get(entry)).toString(), smart));
        }

        return initialize;
    }

    /**
     *  R&uuml;ckgabe aller Sektionen als Enumeration.
     *  @return alle Sektionen als Enumeration
     */
    public Enumeration elements() {
        return Collections.enumeration(this.entries.keySet());
    }

    /**
     *  R&uuml;ckgabe <code>true</code>, wenn die Sektion enthalten ist.
     *  @param  name Name der Sektion
     *  @return <code>true</code> wenn die Sektion enthalten ist
     */
    public boolean contains(String name) {

        name = name == null ? "" : name.trim().toUpperCase();
        return this.entries.containsKey(name);
    }

    /**
     *  R&uuml;ckgabe der angegebenen Section.
     *  @param  name Name der Section
     *  @return die ermittelte Section, sonst <code>null</code> 
     */
    public Section get(String name) {
        
        Section section;
        
        name = name == null ? "" : name.trim().toUpperCase();
        if (name.length() <= 0)
            return null;
        section = (Section)this.entries.get(name);
        if (section == null && this.smart) {
            section = new Section(true);
            this.entries.put(name, section);
        }
        
        return section;
    }

    /**
     *  Setzt die entsprechende Sektion mit dem angegebenen Inhalt.
     *  Bestehende Sektionen werden entfernt und neu gesetzt.
     *  @param  name    Name der Sektion
     *  @param  section Sektion
     *  @return ggf. zuvor zugeordnete Sektion, sonst <code>null</code>
     */
    public Section set(String name, Section section) {
        
        name = name == null ? "" : name.trim().toUpperCase();
        if (name.length() <= 0)
            return null;
        if (section == null)
            section = new Section(this.smart);
        this.entries.put(name, section);
        
        return section;
    }

    /**
     *  Entfernt die angegebene Sektion aus Initialize.
     *  @param  name Name der zu entfernenden Sektion
     *  @return ggf. zuvor zugeordnete Sektion, sonst <code>null</code>
     */
    public Section remove(String name) {

        name = name == null ? "" : name.trim().toUpperCase();
        return (Section)this.entries.remove(name);
    }

    /**
     *  F&uuml;hrt die &uuml;bergebenen Sektionen mit den bestehenden zusammen.
     *  Bereits vorhandene werden dabei aktualisiert, neue angelegt.
     *  @param  initialize zu &uuml;bernehmende Sektionen
     *  @return die aktuelle Instanz mit den zusammgef&uuml;hrten Sektionen
     */
    public Initialize merge(Initialize initialize) {
        
        Iterator iterator;
        String   entry;
        Section  section;
        
        if (initialize == null) return this;

        //die Sektionen werden zusammengefasst oder ggf. neu angelegt
        iterator = initialize.entries.keySet().iterator();
        while (iterator.hasNext()) {
        
            entry   = (String)iterator.next();
            section = initialize.get(entry);
            
            this.set(entry, section.merge(this.get(entry)));
        }
        
        return this;
    }

    /**
     *  R&uuml;ckgabe der Anzahl von Sektionen.
     *  @return die Anzahl von Sektionen
     */
    public int size() {
        return this.entries.size();
    }

    /** Setzt Initialize komplett zur&uuml;ck und verwirft alle Sektionen. */
    public void clear() {
        this.entries.clear();
    }

    /**
     *  R&uuml;ckgabe einer Kopie von Initialize.
     *  @return eine Kopie von Initialize
     */
    public Object clone() {

        Initialize initialize;
        Iterator   iterator;
        String     entry;

        //Initialize wird eingerichtet
        initialize = new Initialize(this.smart);

        //die Sektionen werden kopiert
        iterator = this.entries.keySet().iterator();
        while (iterator.hasNext()) {
        
            entry = (String)iterator.next();
            initialize.entries.put(entry, ((Section)this.entries.get(entry)).clone());
        }
        
        return initialize;
    }
}