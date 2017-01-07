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
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

/**
 *  Section stellt eine Schnittstelle zu den in den Sektionen von Initialize
 *  enthaltenen Werten zur Verf&uuml;gung.<br>
 *  <br>
 *      <dir>Hinweis</dir>
 *  F&uuml;r eine optimale Verarbeitung von INI-Dateien sollte immer die 
 *  Kombination von Initialize mit Section verwendet werden. So basiert die
 *  nachfolgende Beschreibung auf der Kombination beider Komponenten.<br>
 *  <br>
 *  Das verarbeitete INI-Format wurde zur klassischen Form erweitert. Die
 *  Unterteilung erfolgt auch hier in Sektionen, in denen zeilenweise
 *  Schl&uuml;ssel mit zugeh&ouml;rigen Werten abgelegt sind. Beim Namen von
 *  Sektion und Schl&uuml;ssel wird die Gross- und Kleinschreibung ignoriert.
 *  Gleichnamige Deklarationen f&uuml;hren zum &Uuml;berschreiben von Sektionen
 *  und Werten.<br>
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
 *      <dir>Zeile 001</dir>
 *  Die Sektion mit dem Namen <code>SECTION</code> wird definiert. Die Option
 *  <code>EXTENDS</code> verweist auf die Ableitung von den Sektionen
 *  <code>SECTION-A</code> und <code>SECTION-B</code>. Somit basiert die
 *  <code>SECTION</code> auf den Schl&uuml;sseln und Werten der Sektionen
 *  <code>SECTION-A</code> und <code>SECTION-B</code>. Ab dem Semikolon werden
 *  die nachfolgenden Zeichen als Kommentar interpretiert.<br>
 *  <br>
 *      <dir>Zeile 002</dir>
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
 *  Section 5.0 20161224<br>
 *  Copyright (C) 2016 Seanox Software Solutions<br>
 *  Alle Rechte vorbehalten.
 *
 *  @author  Seanox Software Solutions
 *  @version 5.0 20161224
 */
public class Section implements Cloneable {

    /** Hashtable mit den Sch&uuml;sseln */
    private volatile LinkedHashMap entries;
    
    /** Option zum automatische Anlegen nicht existierender Schl&uuml;ssel */
    private volatile boolean smart;

    /** Konstruktor, richtet Section ein. */
    public Section() {
        this(false);
    }
    
    /** 
     *  Konstruktor, richtet Section ein.
     *  Mit der Option <code>smart</code> kann ein smartes Verhalten aktiviert
     *  werden, bei dem nicht existierende Schl&uuml;ssel automatisch mit der
     *  ersten Anfrage angelegt werden. Die Methoden {@link #get(String)} und
     *  {@link #get(String, String)} liefern dann nie <code>null</code> sondern
     *  immer einen Wert, ggf. ist dieser dann leer.
     *  @param smart automatisches Anlegen nicht existierender Schl&uuml;ssel
     */
    public Section(boolean smart) {
        
        this.entries = new LinkedHashMap();
        this.smart   = smart;
    }

    /**
     *  Dekodiert ggf. hexadezimale Werte in einen String.
     *  @param  string zu dekodierender String
     *  @return der dekodierte und getrimmte String
     */                    
    private static String decode(String string) {

        string = string == null ? "" : string.trim();
        if (string.matches("^(?i)0x([0-9a-f]{2})+$"))
            return new String(new BigInteger(string.substring(2), 16).toByteArray()).trim();
        return string;
    }
    
    /**
     *  Erstellt ein Section-Objekt aus dem &uuml;bergebenen String.
     *  Das Parsen ignoriert ung&uuml;ltige Schl&uuml;ssel und Werte.
     *  @param  text zu parsende Section
     *  @return das erstellte Section-Objekt
     */
    public static Section parse(String text) {
        return Section.parse(text, false);
    }

    /**
     *  Erstellt ein Section-Objekt aus dem &uuml;bergebenen Text.
     *  Das Parsen ignoriert ung&uuml;ltige Schl&uuml;ssel und Werte.
     *  Mit der Option <code>smart</code> kann ein smartes Verhalten aktiviert
     *  werden, bei dem nicht existierende Schl&uuml;ssel automatisch mit der
     *  ersten Anfrage angelegt werden. Die Methoden {@link #get(String)} und
     *  {@link #get(String, String)} liefern dann nie <code>null</code> sondern
     *  immer einen Wert, ggf. ist dieser dann leer.
     *  @param  text  zu parsende Sektion
     *  @param  smart automatisches Anlegen nicht existierender Schl&uuml;ssel
     *  @return das erstellte Section-Objekt
     */
    public static Section parse(String text, boolean smart) {
        
        Enumeration     enumeration;
        Section         section;
        StringBuffer    buffer;
        StringTokenizer tokenizer;
        String          line;
        String          entry;
        String          label;
        String          value;
        
        int             option;

        section = new Section(smart);

        if (text == null) return section;
        
        buffer = null;
        option = 0;

        tokenizer = new StringTokenizer(text, "\r\n");
        while (tokenizer.hasMoreTokens()) {
            
            //die naechste Zeile wird ermittelt
            line = tokenizer.nextToken().trim();
            
            if (!line.startsWith("+")) {
                
                option = 0;
                if (line.matches("^[^;=]+\\[\\s*\\+\\s*\\].*$"))
                    option |= 1;
                if (line.matches("^[^;=]+\\[\\s*\\?\\s*\\].*$"))
                    option |= 2;
                
                //der Kommentarteil wird ggf. entfernt
                if ((option & 1) == 0 && line.contains(";"))
                    line = line.substring(0, line.indexOf(';')).trim();
                
                buffer = null;

                //der Schluessel wird ermittelt, ggf. dekodiert und optimiert 
                label = line.replaceAll("^([^;=]*?)((?:\\[\\s*.{0,1}\\s*\\]\\s*)*)(?:\\s*=\\s*(.+?)\\s*)*$", "$1");
                label = Section.decode(label).toUpperCase();
                
                //nur gueltige Schluessel werden geladen
                if (label.isEmpty())
                    continue;
                
                if ((option & 2) != 0) {
                    
                    //der Wert wird direkt in Systemproperties gesucht
                    value = System.getProperty(label);
                    
                    //die System-Properties werden unabhaengig von der
                    //Gross- / Kleinschreibung nach dem Schuessel durchsucht
                    enumeration = Collections.enumeration(System.getProperties().keySet());
                    while (value == null && enumeration.hasMoreElements()) {
                        entry = (String)enumeration.nextElement();
                        if (!label.equalsIgnoreCase(entry.trim()))
                            continue;
                        value = System.getProperty(entry, "").trim();
                        break;
                    }
                    
                    //die System-Umgebungsvariablen werden unabhaengig von der
                    //Gross- / Kleinschreibung nach dem Schuessel durchsucht
                    enumeration = Collections.enumeration(System.getenv().keySet());
                    while (value == null && enumeration.hasMoreElements()) {
                        entry = (String)enumeration.nextElement();
                        if (!label.equalsIgnoreCase(entry.trim()))
                            continue;
                        value = System.getenv(entry);
                        value = value == null ? "" : value.trim();
                        break;
                    }
                    
                    if (value != null) {
                        section.entries.put(label, new StringBuffer(value));
                        continue;
                    }
                }
                
                //der Wert wird ermittelt, ggf. dekodiert und optimiert
                value = line.trim();
                value = value.replaceAll("^([^;=]*?)((?:\\[\\s*.{0,1}\\s*\\]\\s*)*)(?:\\s*=\\s*(.+?)\\s*)*$", "$3");
                value = Section.decode(value);
                
                buffer = new StringBuffer(value);
                section.entries.put(label, buffer);
                
            } else if (buffer != null) {
                
                //Inhalt wird nur mit gueltigem Schluessel verarbeitet

                //der Kommentarteil wird ggf. entfernt
                if ((option & 1) == 0 && line.contains(";"))
                    line = line.substring(0, Math.max(0, line.indexOf(';'))).trim();

                line = line.substring(1).trim();
                if (!line.isEmpty())
                    line = Section.decode(line);
                if (!line.isEmpty())
                    buffer.append(" ").append(Section.decode(line));                
            }
        }
        
        enumeration = Collections.enumeration(section.entries.keySet());
        while (enumeration.hasMoreElements()) {
            entry = (String)enumeration.nextElement();
            section.entries.put(entry, ((StringBuffer)section.entries.get(entry)).toString());
        }

        return section;
    }

    /**
     *  R&uuml;ckgabe aller Sch&uuml;ssel als Enumeration.
     *  @return alle Sch&uuml;ssel als Enumeration
     */
    public synchronized Enumeration elements() {
        return Collections.enumeration(this.entries.keySet());
    }
    
    /**
     *  R&uuml;ckgabe <code>true</code> wenn der Sch&uuml;ssel ist.
     *  @param  key Name des Sch&uuml;ssels
     *  @return <code>true</code> wenn der Sch&uuml;ssel enthalten ist
     */
    public synchronized boolean contains(String key) {

        if (key != null)
            key = key.toUpperCase().trim();
        if (key == null || key.isEmpty())
            return false;
        return this.entries.containsKey(key);
    }

    /**
     *  R&uuml;ckgabe des Wert zum Sch&uuml;ssel. Ist dieser nicht enthalten
     *  bzw. kann nicht ermittelt werden, liefert die Methode <code>null</code>
     *  und im Smart-Modus einen leeren Wert.
     *  mit einem leerer Wert erstellt. 
     *  @param  key Name des Sch&uuml;ssels
     *  @return der Wert des Sch&uuml;ssels, sonst <code>null</code> und im
     *          Smart-Modus einen leeren Wert
     */
    public synchronized String get(String key) {
        return this.get(key, null);
    }

    /**
     *  R&uuml;ckgabe des Wert zum Schl&uuml;ssel. Ist dieser nicht enthalten
     *  bzw. kann nicht ermittelt werden, liefert die Methode den alternativen
     *  Wert, sonst <code>null</code>. Im Smart-Modus wird ein nicht
     *  existierender Schl&uuml;ssel mit einem leeren oder dem alternativen
     *  Wert angelegt. Die Methode gibt dann einen leeren bzw. den alternativen
     *  Wert zur&uuml;ck.
     *  @param  key       Name des Sch&uuml;ssels
     *  @param  alternate alternativer Wert, bei unbekanntem Sch&uuml;ssel
     *  @return der Wert des Sch&uuml;ssels, sonst <code>null</code> und im
     *          Smart-Modus einen leeren bzw. den alternativen Wert
     */
    public synchronized String get(String key, String alternate) {

        String value;
        
        if (key != null)
            key = key.toUpperCase().trim();
        if (key == null || key.isEmpty())
            return alternate;
        
        value = (String)this.entries.get(key);
        if (value == null) {
            value = alternate != null ? alternate : this.smart ? "" : null;
            if (value != null)
                this.entries.put(key, value);
        }
        
        return value;
    }

    /**
     *  Setzt den Sch&uuml;ssel mit dem entsprechenden Wert. Sch&uuml;ssel und
     *  Werte werden dabei gel&auml;ttet. Leere Sch&uuml;ssel sind nicht
     *  zul&auml;ssig und f&uuml;hren zur {@link IllegalArgumentException}.
     *  @param  key   Name des Sch&uuml;ssels
     *  @param  value Wert des Sch&uuml;ssels
     *  @return ggf. zuvor zugeordneter Wert, sonst <code>null</code>
     */
     public synchronized String set(String key, String value) {
         
         if (key != null)
             key = key.toUpperCase().trim();
         if (key == null || key.isEmpty())
             throw new IllegalArgumentException();
         return (String)this.entries.put(key, value == null ? "" : value.trim());
    }

     /**
      *  Entfernt den angegebenen Sch&uuml;ssel.
      *  @param  key Name des zu entfernenden Sch&uuml;ssels
      *  @return ggf. zuvor zugeordneter Wert, sonst <code>null</code>
      */
     public synchronized String remove(String key) {

         if (key != null)
             key = key.toUpperCase().trim();
         if (key == null || key.isEmpty())
             return null;
         return (String)this.entries.remove(key);
     }
     
     /**
      *  F&uuml;hrt die Sch&uuml;ssel dieser und der &uuml;bergebenen Sektion
      *  zusammen. Bereits vorhandene Eintr&auml;ge werden &uuml;berschrieben,
      *  neue werden hinzugef&uuml;gt. Leere Sch&uuml;ssel sind dabei nicht
      *  zul&auml;ssig und f&uuml;hren zur {@link IllegalArgumentException}.
      *  @param  section zu &uuml;bernehmende Sektion
      *  @return die aktuelle Instanz mit den zusammgef&uuml;hrten Sektionen
      */
     public synchronized Section merge(Section section) {
         
         Enumeration enumeration;
         String      entry;
         
         if (section == null)
             return this;

         //die Sektionen werden zusammengefasst oder ggf. neu angelegt
         enumeration = Collections.enumeration(section.entries.keySet());
         while (enumeration.hasMoreElements()) {
             entry = (String)enumeration.nextElement();
             this.set(entry, section.get(entry));
         }
         
         return this;
    }
     
    /**
     *  R&uuml;ckgabe der Anzahl von Eintr&auml;gen.
     *  @return die Anzahl der Eintr&auml;ge
     */
    public synchronized int size() {
        return this.entries.size();
    }

    /** Setzt Section komplett zur&uuml;ck. */
    public synchronized void clear() {
        this.entries.clear();
    }

    /**
     *  R&uuml;ckgabe einer Kopie von Section.
     *  @return eine Kopie von Section
     */
    public synchronized Object clone() {

        Section section;

        //Section wird eingerichtet
        section = new Section(this.smart);

        //die Schuessel werden als Kopie uebernommen
        section.entries = (LinkedHashMap)this.entries.clone();

        return section;
    }
}