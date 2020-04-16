/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 * Devwex, Advanced Server Development
 * Copyright (C) 2020 Seanox Software Solutions
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.seanox.devwex;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

/**
 * Section stellt eine Schnittstelle zu den in den Sektionen von Initialize
 * enthaltenen Werten zur Verf&uuml;gung.
 * 
 * <h3>Hinweis</h3>
 * F&uuml;r eine optimale Verarbeitung von INI-Dateien sollte immer die
 * Kombination von Initialize mit Section verwendet werden. So basiert die
 * nachfolgende Beschreibung auf der Kombination beider Komponenten.<br>
 * <br>
 * Das für die Konfiguration verwendete INI-Format ist eine kompatible
 * Erweiterung zum klassischen Format. Es ist ebenfalls zeilenbasiert und
 * verwendet Sektionen in denen Schl&uuml;ssel mit Werten abgelegt sind. Beim
 * Namen von Sektion und Schl&uuml;ssel wird die Gross- und Kleinschreibung
 * ignoriert. Gleichnamige Deklarationen f&uuml;hren zum &Uuml;berschreiben von
 * Sektionen und Schl&uuml;sseln.<br>
 * <br>
 * In der Erweiterung lassen sich u.a. Sektionen vererben. Dazu folgt einer
 * Sektion das Sch&uuml;sselwort {@code EXTENDS} und gefolgt von den Namen
 * referenzierter Sektionen. Damit &uuml;bernimmt die Sektion alle
 * Schl&uuml;ssel und Werte der referenzierten Sektionen und kann diese
 * erweitern oder &uuml;berschreiben.<br>
 * <br>
 * Das Zuweisen von Werten zu Schl&uuml;sseln in einer Sektion erfolgt mit dem
 * Gleichheitszeichen. Abweichend vom Orginalformat, kann die Zuweisung in der
 * Folgezeile ohne erneute Angabe des Schl&uuml;ssels durch die Verwendung vom
 * Pluszeichen fortgesetzt werden. Werte lassen sich zudem fest, variabel und
 * optional zuweisen.<br>
 * Durch die Option {@code [?]} am Ende eines Schl&uuml;ssels, wird nach einem
 * gleichnamigen Schl&uuml;ssel in den System-Properties der
 * Java-Laufzeitumgebung gesucht. Kann keiner ermittelt werden, wird der
 * optional angegebene Wert verwendet. Ohne Wert gilt ein solcher
 * Schl&uuml;ssel als nicht angegeben und wird ignoriert.<br>
 * <br>
 * Kommentare beginnen mit einem Semikolon, sind optional und lassen sich an
 * jeder beliebigen Stelle in einer Zeile verwenden. Die nachfolgenden Zeichen
 * sind somit kein Bestandteil von Sektion, Schl&uuml;ssel oder Wert.<br>
 * Mit der Option {@code [+]} am Ende eines Schl&uuml;ssels, kann die
 * Verwendung von Kommentaren f&uuml;r diesen Schl&uuml;ssel deaktiviert und das
 * Semikolon im Wert verwendet werden.<br>
 * <br>
 * Sektionen, Schl&uuml;ssel und Werte unterst&uuml;tzen auch eine hexadezimale
 * Schreibweise. Diese beginnt mit {@code 0x...}, gefolgt von der hexadezimalen
 * Zeichenfolge. Diese Schreibweise kann immer nur auf das komplette Element
 * angewandt werden. Die Kombination oder Unterbrechung ist nicht m&ouml;glich.
 * 
 * <h3>Beispiel</h3>
 * <pre>
 *   001 [SECTION] EXTENDS SECTION-A SECTION-B      ;Kommentar
 *   002   PARAM-A                 = WERT-1         ;Kommentar
 *   003   PARAM-B             [+] = WERT-2; WERT-3
 *   004                           + WERT-4; WERT-5
 *   005   PARAM-C          [?][+] = WERT-6; WERT-7
 *   006   PARAM-E          [?]                     ;Kommentar
 *   007
 *   008 [0x53454354494F4E2D41]
 *   009   PARAM-A                 = 0x574552542D31 ;Kommentar
 *   010   0x504152414D2D42        = WERT-2         ;Kommentar 
 *   011   0x504152414D2D43    [+] = 0x574552542D33
 *   012   PARAM-D                 = 0x574552542D34
 *   013                           + 0x574552542D35
 *   014   PARAM-E          [?][+] = 0x574552542D363B20574552542D37
 *   015   0x504152414D2D45 [?]                     ;Kommentar
 * </pre>
 * 
 * <h4>Zeile 1</h4>
 * Die Sektion mit dem Namen {@code SECTION} wird definiert. Das
 * Schl&uuml;sselwort {@code EXTENDS} verweist auf die Ableitung von den
 * Sektionen {@code SECTION-A} und {@code SECTION-B}. Somit basiert
 * {@code SECTION} auf den Schl&uuml;sseln und Werten der Sektionen
 * {@code SECTION-A} und {@code SECTION-B}. Ab dem Semikolon werden die
 * nachfolgenden Zeichen als Kommentar interpretiert.
 * 
 * <h4>Zeile 2</h4>
 * Dem Schl&uuml;ssel {@code PARAM-A} wird der Wert {@code WERT-1} zugewiesen.
 * Die nachfolgenden Zeichen werden ab dem Semikolon als Kommentar
 * interpretiert.
 * 
 * <h4>Zeile 3</h4>
 * Dem Schl&uuml;ssel {@code PARAM-B} wird {@code WERT-2; WERT-3} als Wert
 * zugewiesen. Durch die Option {@code [+]} am Ende vom Schl&uuml;ssel, wird der
 * Zeilenkommentar abgeschaltet und alle Zeichen f&uuml;r die Wertzuweisung
 * verwendet. Die Angabe eines Kommentars ist in dieser Zeile nicht
 * m&ouml;glich.
 * 
 * <h4>Zeile 4</h4>
 * Die Wertzuweisung von Zeile 3 wird fortgesetzt und der Wert
 * {@code WERT-4; WERT-5} dem bestehenden Wert vom Schl&uuml;ssel
 * {@code PARAM-B} hinzugef&uuml;gt. Die Option {@code [+]} aus Zeile 3 wird
 * ebenfalls in Zeile 4 &uuml;bernommen, womit auch hier der Zeilenkommentar
 * abgeschaltet ist und alle Zeichen als Wertzuweisung verwendet werden. Die
 * Eingabe eines Kommentars ist in dieser Zeile nicht m&ouml;glich. Weitere
 * vorangestellte Optionen sind nicht m&ouml;glich.
 *
 * <h4>Zeile 5</h4>
 * Die Wertzuweisung f&uuml;r den Schl&uuml;ssel {@code PARAM-C} ist dynamisch.
 * In den System-Properties der Java-Laufzeitumgebung wird dazu nach dem
 * gleichnamigen Schl&uuml;ssel {@code PARAM-C} gesucht, wobei die Gross- und
 * Kleinschreibung ignoriert wird. Der Schl&uuml;ssel muss dazu Bestandteile der
 * Laufzeitumgebung sein oder kann beim Programmstart in der Form
 * {@code -Dschluessel=wert} gesetzt werden.<br>
 * Enthalten die System-Properties der Java-Laufzeitumgebung keinen
 * entsprechenden Schl&uuml;ssel, wird alternativ {@code WERT-6; WERT-7} als
 * Wert verwendet.<br>
 * Durch die Kombination mit der Option {@code [+]} am Ende vom Schl&uuml;ssel,
 * wird der Zeilenkommentar abgeschaltet und alle Zeichen f&uuml;r die
 * Wertzuweisung verwendet. Die Angabe eines Kommentars ist in dieser Zeile
 * nicht m&ouml;glich.
 *
 * <h4>Zeile 6</h4>
 * Die Wertzuweisung f&uuml;r den Schl&uuml;ssel {@code PARAM-E} ist dynamisch.
 * In den System-Properties der Java-Laufzeitumgebung wird dazu nach dem
 * gleichnamigen Schl&uuml;ssel gesucht, wobei die Gross- und Kleinschreibung
 * ignoriert wird. Der Schl&uuml;ssel muss dazu Bestandteil der Laufzeitumgebung
 * sein oder kann beim Programmstart in der Form {@code -Dschluessel=wert}
 * gesetzt werden.<br>
 * Enthalten die System-Properties der Java-Laufzeitumgebung keinen
 * entsprechenden Schl&uuml;ssel, wird dieser Schl&uuml;ssel ignoriert, da auch
 * kein alternativer Wert angegeben wurde.<br>
 * Kommentare werden in dieser Zeile unterst&uuml;tzt.
 *
 * <h4>Zeile 8 - 15</h4>
 * Analog den Beispielen aus Zeile 1 - 6 wird f&uuml;r Sektionen, Schl&uuml;ssel
 * und Werte die hexadezimale Schreibweise verwendet.<br>
 * <br>
 * Section 5.0.1 20180109<br>
 * Copyright (C) 2018 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 5.0.1 20180109
 */
public class Section implements Cloneable {

    /** Map mit den Sch&uuml;sseln */
    private volatile LinkedHashMap entries;
    
    /** Option zur Aktivierung vom Smart-Modus */
    private volatile boolean smart;

    /** Konstruktor, richtet Section ein. */
    public Section() {
        this(false);
    }
    
    /** 
     * Konstruktor, richtet Section ein.
     * Mit der Option {@code smart} kann ein smartes Verhalten aktiviert werden.
     * Dadurch verhalten sich die Methoden {@link #get(String)} und
     * {@link #get(String, String)} bei nicht existierenden Schl&uuml;sseln so,
     * als l&auml;gen diese mit einem leeren Wert vor und liefert so nie
     * {@code null}. Die Methoden {@link #parse(String, boolean)} und
     * {@link #merge(Section)} &uml;bernehmen im Smart-Modus nur Schl&uuml;ssel
     * mit Wert die nicht leer sind. Andere Schl&uuml;ssel werden ignoriert. Die
     * Methode {@link #set(String, String)} reagiert im Smart-Modus zudem bei
     * einem leeren Werten wie {@link #remove(String)} und entfernt den
     * Schl&uuml;ssel.
     * @param smart aktiviert den smarten Modus
     */
    public Section(boolean smart) {
        
        this.entries = new LinkedHashMap();
        this.smart   = smart;
    }

    /**
     * Dekodiert ggf. hexadezimale Werte in einen String.
     * @param  string zu dekodierender String
     * @return der dekodierte und getrimmte String
     */                    
    private static String decode(String string) {

        string = string == null ? "" : string.trim();
        if (string.matches("^(?i)0x([0-9a-f]{2})+$"))
            return new String(new BigInteger(string.substring(2), 16).toByteArray()).trim();
        return string;
    }
    
    /**
     * Erstellt ein Section-Objekt aus dem &uuml;bergebenen String.
     * Das Parsen ignoriert ung&uuml;ltige Schl&uuml;ssel und Werte.
     * @param  text zu parsende Section
     * @return das erstellte Section-Objekt
     */
    public static Section parse(String text) {
        return Section.parse(text, false);
    }

    /**
     * Erstellt ein Section-Objekt aus dem &uuml;bergebenen Text.
     * Das Parsen ignoriert ung&uuml;ltige Schl&uuml;ssel und Werte.
     * Mit der Option {@code smart} kann ein smartes Verhalten aktiviert werden.
     * Dadurch &uml;bernehmen {@link #parse(String, boolean)} und
     * {@link #merge(Section)} nur Schl&uuml;ssel mit Werten die nicht leer
     * sind. Schl&uuml;ssel mit leeren Werten werden ignoriert. Die Methoden 
     * {@link #get(String)} und {@link #get(String, String)} reagieren bei nicht
     * existierenden Schl&uuml;sseln so, als l&auml;gen diese mit einem leeren
     * Wert vor und liefert so nie {@code null}. Die Methode
     * {@link #set(String, String)} reagiert im Smart-Modus zudem bei leeren
     * Werten wie {@link #remove(String)} und entfernt den Schl&uuml;ssel.
     * @param  text  zu parsende Sektion
     * @param  smart aktiviert den smarten Modus
     * @return das erstellte Section-Objekt
     */
    public static Section parse(String text, boolean smart) {
        
        Enumeration     enumeration;
        LinkedHashMap   entries;
        Section         section;
        StringBuffer    buffer;
        StringTokenizer tokenizer;
        String          line;
        String          entry;
        String          label;
        String          value;
        
        int             option;

        section = new Section(smart);

        if (text == null)
            return section;
        
        entries = new LinkedHashMap();
        buffer  = null;
        option  = 0;

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
                label = line.replaceAll("^([^;=]+?)?((?:\\s*\\[\\s*.{0,1}\\s*\\])+)?(?:\\s*=\\s*(.*))?\\s*$", "$1");
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
                        entries.put(label, new StringBuffer(value));
                        continue;
                    }
                }
                
                //der Wert wird ermittelt, ggf. dekodiert und optimiert
                value = line.trim();
                value = value.replaceAll("^([^;=]+?)?((?:\\s*\\[\\s*.{0,1}\\s*\\])+)?(?:\\s*=\\s*(.*))?\\s*$", "$3");
                value = Section.decode(value);
                
                buffer = new StringBuffer(value);
                entries.put(label, buffer);
                
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
        
        enumeration = Collections.enumeration(entries.keySet());
        while (enumeration.hasMoreElements()) {
            entry = (String)enumeration.nextElement();
            value = ((StringBuffer)entries.get(entry)).toString().trim();
            if (!smart || !value.isEmpty())
                section.entries.put(entry, value);
        }
        
        return section;
    }

    /**
     * R&uuml;ckgabe aller Sch&uuml;ssel als Enumeration.
     * @return alle Sch&uuml;ssel als Enumeration
     */
    public synchronized Enumeration elements() {
        return Collections.enumeration(this.entries.keySet());
    }
    
    /**
     * R&uuml;ckgabe {@code true} wenn der Sch&uuml;ssel ist.
     * @param  key Name des Sch&uuml;ssels
     * @return {@code true} wenn der Sch&uuml;ssel enthalten ist
     */
    public synchronized boolean contains(String key) {

        if (key != null)
            key = key.toUpperCase().trim();
        if (key == null || key.isEmpty())
            return false;
        return this.entries.containsKey(key);
    }

    /**
     * R&uuml;ckgabe vom Wert des Sch&uuml;ssels. Ist dieser nicht enthalten
     * bzw. kann nicht ermittelt werden, liefert die Methode den alternativen
     * Wert, sonst {@code null}, bzw. im Smart-Modus einen leeren Wert. Da im
     * Smart-Modus leere Schl&uuml;ssel automatisch bereinigt werden, wird der
     * erstellte Schl&uuml;ssel mit seinem leeren Standardwert nicht in der
     * Section eingetragen.
     * @param  key       Name des Sch&uuml;ssels
     * @param  alternate alternativer Wert, bei unbekanntem Sch&uuml;ssel
     * @return der Wert des Sch&uuml;ssels, sonst {@code null} bzw. den
     *     alternativen Wert oder im Smart-Modus einen leeren
     */
    public synchronized String get(String key) {
        return this.get(key, null);
    }

    /**
     * R&uuml;ckgabe vom Wert des Sch&uuml;ssels. Ist dieser nicht enthalten
     * bzw. kann nicht ermittelt werden, liefert die Methode den alternativen
     * Wert, sonst {@code null}, bzw. im Smart-Modus einen leeren Wert. Da im
     * Smart-Modus leere Schl&uuml;ssel automatisch bereinigt werden, wird der
     * erstellte Schl&uuml;ssel mit seinem leeren Standardwert nicht in der
     * Section eingetragen.
     * @param  key       Name des Sch&uuml;ssels
     * @param  alternate alternativer Wert, bei unbekanntem Sch&uuml;ssel
     * @return der Wert des Sch&uuml;ssels, sonst {@code null} bzw. den
     *     alternativen Wert oder im Smart-Modus einen leeren
     */
    public synchronized String get(String key, String alternate) {

        String value;
        
        if (key != null)
            key = key.toUpperCase().trim();
        if (key == null
                || key.isEmpty())
            value = null;
        else value = (String)this.entries.get(key);
        
        if (value == null)
            value = alternate;
        if (value == null
                && this.smart)
            value = "";
        
        return value;
    }

    /**
     * Setzt den Sch&uuml;ssel mit dem entsprechenden Wert. Sch&uuml;ssel und
     * Werte werden dabei gel&auml;ttet. Leere Sch&uuml;ssel sind nicht
     * zul&auml;ssig und f&uuml;hren zur {@link IllegalArgumentException}. Die
     * Methode reagiert im Smart-Modus zudem bei einem leeren Werten wie
     * {@link #remove(String)} und entfernt den Schl&uuml;ssel.
     * @param  key   Name des Sch&uuml;ssels
     * @param  value Wert des Sch&uuml;ssels
     * @return ggf. zuvor zugeordneter Wert, sonst {@code null}
     */
     public synchronized String set(String key, String value) {
         
         if (key != null)
             key = key.toUpperCase().trim();
         if (key == null || key.isEmpty())
             throw new IllegalArgumentException();
         value = value == null ? "" : value.trim();
         if (this.smart && value.isEmpty())
             return (String)this.entries.remove(key);
         return (String)this.entries.put(key, value);
    }

     /**
      * Entfernt den angegebenen Sch&uuml;ssel.
      * @param  key Name des zu entfernenden Sch&uuml;ssels
      * @return ggf. zuvor zugeordneter Wert, sonst {@code null}
      */
     public synchronized String remove(String key) {

         if (key != null)
             key = key.toUpperCase().trim();
         if (key == null || key.isEmpty())
             return null;
         return (String)this.entries.remove(key);
     }
     
     /**
      * F&uuml;hrt die Sch&uuml;ssel dieser und der &uuml;bergebenen Sektion
      * zusammen. Bereits vorhandene Eintr&auml;ge werden &uuml;berschrieben,
      * neue werden hinzugef&uuml;gt. Leere Sch&uuml;ssel sind dabei nicht
      * zul&auml;ssig und f&uuml;hren zur {@link IllegalArgumentException}.
      * @param  section zu &uuml;bernehmende Sektion
      * @return die aktuelle Instanz mit den zusammgef&uuml;hrten Sektionen
      */
     public synchronized Section merge(Section section) {
         
         Enumeration enumeration;
         String      entry;
         String      value;
         
         if (section == null)
             return this;
         
         //die Sektionen werden zusammengefasst oder ggf. neu angelegt
         enumeration = Collections.enumeration(section.entries.keySet());
         while (enumeration.hasMoreElements()) {
             entry = (String)enumeration.nextElement();
             value = section.get(entry);
             if (!this.smart || !value.isEmpty())             
                 this.set(entry, value);
         }
         
         return this;
    }
     
    /**
     * R&uuml;ckgabe der Anzahl von Eintr&auml;gen.
     * @return die Anzahl der Eintr&auml;ge
     */
    public synchronized int size() {
        return this.entries.size();
    }

    /** Setzt Section komplett zur&uuml;ck. */
    public synchronized void clear() {
        this.entries.clear();
    }

    /**
     * R&uuml;ckgabe einer Kopie von Section.
     * @return eine Kopie von Section
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