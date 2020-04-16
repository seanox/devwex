/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 * im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Generator, generiert Daten durch das Bef&uuml;llen von Platzhaltern (Tags) in
 * einer Vorlage (Model/Template). Dazu wird der Vorlage eine Werte-Liste mit
 * Schl&uuml;sseln &uuml;bergeben. Entsprechen die Schl&uuml;ssel den
 * Platzhaltern, wobei die Gross-/Kleinschreibung ignoriert wird, werden die
 * Platzhalter durch die Werte ersetzt.<br>
 * <br>
 * Der Generator arbeitet aus Byte-Level.<br>
 * Werte werden daher prim&auml;r als byte-Arrays erwartet. Anderen Datentypen
 * werden mittels {@code String.valueOf(value).getBytes()} konvertiert.<br>
 * <br>
 * Platzhalter lassen sich auch als Segmente verwenden.<br>
 * Segmente sind Teilstrukturen, die bis zu einer Tiefe von 65535 Ebenen
 * verschachtelt werden können. Diese Teilstrukturen lassen sich global oder per
 * Segment-Name dediziert/partiell verwenden und bef&uuml;llen.<br>
 * Die Platzhalter von Segmenten bleiben nach dem Bef&uuml;llen erhalten und
 * sind iterativ wiederverwendbar.<br>
 * Als Werte werden f&uuml;r Segmente die Datentypen {@link Collection} und
 * {@link Map} erwartet. Eine {@link Map} enth&auml;lt dann die Werte f&uuml;r
 * die Platzhalter innerhalb des Segments. Eine {@link Collection} f&uuml;r zu
 * einer Iteration &uuml;ber eine Menge von {@link Map} und ist vergleichbar mit
 * dem iterativen Aufruf der Methode {@link #set(String, Map)}.<br>
 * Beides, {@link Map} und {@link Collection}, erzeugt tiefe, komplexe ggf. sich
 * wiederholende rekursive Strukturen.
 *
 * <h3>Beschreibung der Syntax</h3>
 * Die Syntax der Platzhalter ignoriert die Gross- und Kleinschreibung und ist
 * auf folgende Zeichen begrenzt:
 *     <dir>{@code a-z A-Z 0-9 _-}</dir>
 *     
 * <h3>Struktur und Beschreibung der Platzhalter</h3>
 * <table>
 *   <tr>
 *     <td valign="top" nowrap="nowrap">
 *       {@code #[value]}
 *     </td>
 *     <td valign="top">
 *       Setzt an dieser Stelle den Wert f&uuml;r &lt;value&gt; ein und entfernt
 *       den Platzhalter.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td valign="top" nowrap="nowrap">
 *       {@code #[scope[[...]]]}
 *     </td>
 *     <td valign="top">
 *       Definiert ein Segment/Scope. Die Verschachtelung und Verwendung
 *       weiterer Segmente ist m&ouml;glich. Da die Platzhalter zum
 *       Einf&uuml;gen von Segmenten erhalten bleiben, k&ouml;nnen diese zum
 *       Aufbau von Listen verwendet werden.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td valign="top" nowrap="nowrap">
 *       {@code #[0x0A]}<br>
 *       {@code #[0x4578616D706C6521]}
 *     </td>
 *     <td valign="top">
 *       Maskiert ein oder mehr Zeichen. Die Umwandlung erfolgt erst mit
 *       {@link #extract(String, Map)}, {@link #extract(String)} bzw.
 *       {@link #extract()} zum Schluss der Generierung.
 *     </td>
 *   </tr>
 * </table>
 * 
 * <h3>Arbeitsweise</h3>
 * Das Model (Byte-Array) wird initial geparst.
 * Dabei werden alle Platzhalter auf syntaktische Richtigkeit gepr&uuml;ft.
 * Ggf. werden ung&uuml;ltige Platzhalter entfernt. Zudem werden die Scopes mit
 * den Segmenten (Teilvorlagen) ermittelt und durch einen einfachen Platzhalter
 * ersetzt. Nach dem Parsen entsteht ein finales Model mit optimierten
 * Platzhaltern und extrahierten Segmenten, was zur Laufzeit nicht ge&auml;ndert
 * werden kann.<br>
 * <br>
 * Zur Nutzung des Models stehen dann verschiedene M&ouml;glichkeiten zur
 * Verf&uuml;gung.<br>
 * <br>
 * Mit {@link #set(Map)} werden im Model die Platzhalter durch die
 * &uuml;bergeben Werte ersetzt. Platzhalter zudenen keine Werte existieren,
 * bleiben erhalten. Platzhalter die ein Segment/Scope repr&auml;sentieren
 * werden ebenfalls gesetzt, wenn in den Werten ein korrespondierder
 * Schl&uuml;ssel existiert. Bei Segmenten/Scopes bleibt der Platzhalter zur
 * erneuten Verwendung erhalten und folgt direkt dem eingef&uuml;gten Wert.<br>
 * <br>
 * Bei {@link #set(String, Map)} wird nur der angegeben Scope bef&uuml;llt.
 * Dazu wird eine Kopie vom Segment (Teilvorlage) erstellt und mit den
 * &uuml;bergebenen Werten bef&uuml;llt, alle Platzhalter darin werden entfernt
 * und der Inhalt wird als Wert vor dem Platzhalter eingef&uuml;gt. Somit bleibt
 * auch der Platzhalter von Segmenten/Scopes zur erneuten Verwendung
 * erhalten.<br>
 * <br>
 * Die Methoden {@link #extract(String)} und {@link #extract(String, Map)}
 * dienen der exklusiven Nutzung von Segmenten (Teilvorlagen), die partiell
 * bef&uuml;llt und aufbereitet werden. Beide Methoden erstellen finale
 * Ergebnisse, die dem Aufruf von {@link #set(Map)} in Kombination mit
 * {@link #extract()} entsprechen, sich dabei aber nur auf ein Segment
 * konzentrieren.<br>
 * <br>
 * Generator 5.2 20190422<br>
 * Copyright (C) 2019 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 5.2 20190422
 */
public class Generator {

    /** Segmente der Vorlage */
    private HashMap scopes;

    /** Model, Datenbuffer der Vorlage */
    private byte[] model;

    /** Konstruktor, erstellt einen leeren Generator. */
    private Generator() {
        this.scopes = new HashMap();
    }

    /**
     * Erstellt einen neuen Generator auf Basis der &uuml;bergebenen Vorlage.
     * @param  model Vorlage als Bytes
     * @return der Generator mit der als Bytes &uuml;bergebenen Vorlage
     */
    public static Generator parse(byte[] model) {
        
        Generator generator = new Generator();
        generator.model = generator.scan(model);
        return generator;
    }
    
    /**
     * Ermittelt ob an der angegebenen Position in einem Model(Fragmet) ein
     * gueltiger Platzhalter beginnt. In dem Fall wird die Laenge des kompletten
     * Platzhalters zurueckgegeben. Kann kein Platzhalter ermittelt werden, wird
     * die Laenge 0 zurueckgegeben. Liegen im Model keine weiteren Daten zur
     * Analyse vor (Datenende ist erreicht) wird ein negativer Wert
     * zurueckgegeben.
     * @param  model  Model(Fragmet)
     * @param  cursor Position
     * @return die Position des n&auml;chsten Platzhalters oder Segments, sonst
     *     ein negativer Wert
     */
    private static int scan(byte[] model, int cursor) {
        
        if (model == null
                || cursor >= model.length)
            return -1;        

        //Phase 0: Identifizierung eines Platzhalters
        //  - die unterstuetzen Formate: #[...], #[...[[...]]]
        //  - Hauptmerkmal sind die ersten zwei Zeichen
        //  - alle Platzhalter beginnen mit #[...
        if (cursor +1 >= model.length
                || model[cursor] != '#'
                || model[cursor +1] != '[')
            return 0;
            
        int offset = cursor;
        int deep   = 0;

        int[] stack = new int[65535];
        while (cursor < model.length) {

            //Der aktuelle Level wird ermittelt.
            int level = 0;
            if (deep > 0)
                level = stack[deep];

            //Phase 1: Erkennung vom Start eines Platzhalters
            //  - die unterstuetzen Formate: #[...], #[...[[...]]]
            //  - Hauptmerkmal sind die ersten zwei Zeichen
            //  - alle Platzhalter beginnen mit #[...
            //Ein Platzhalter kann nur beginnen, wenn noch kein Stack und damit
            //kein Platzhalter existiert oder wenn zuvor ein
            //Segment-Platzhalter ermittelt wurde. In beiden Faellen ist das
            //Level ungleich 1 und es startet ein weiterer Stack mit Level 1.
            if (cursor +1 < model.length
                    && model[cursor] == '#'
                    && model[cursor +1] == '['
                    && level != 1) {
                stack[++deep] = 1;
                cursor += 2;
                continue;
            }
            
            //Phase 1A: Qualifizierung eines Segment-Platzhalters
            //  - es wird der aktive Level 1 erwartet
            //  - es wird die Zeichenfolge [[ gefunden
            //Der aktuelle Stack wird auf Level 2 gesetzt.
            if (cursor +1 < model.length
                    && model[cursor] == '['
                    && model[cursor +1] == '['
                    && level == 1) {
                stack[deep] = 2;
                cursor += 2;
                continue;
            }

            //Phase 2: Erkennung vom Ende eines erkannten Platzhalters
            //Der Level muss 1 sein und es muss das Zeichen [ gefunden werden.
            //Dann wird der aktulle Stack entfernt, da die Suche hier
            //abgeschlossen ist.
            if (model[cursor] == ']'
                    && level == 1) {
                if (--deep  <= 0)
                    break;
                cursor += 1;
                continue;
            }

            //Phase 2A: Erkennung vom Ende eines erkannten Platzhalters
            //Der Level muss 1 sein und es muss das Zeichen [ gefunden werden.
            //Dann wird der aktulle Stack entfernt, da die Suche hier
            //abgeschlossen ist.
            if (cursor +2 < model.length
                    && model[cursor +0] == ']'
                    && model[cursor +1] == ']'
                    && model[cursor +2] == ']'
                    && level == 2) {
                cursor += 2;
                if (--deep <= 0)
                    break;
                cursor += 1;
                continue;
            }
            
            cursor++;
        }
        
        //Fall 1: Der Stack ist nicht leer
        //Somit wurde ein Platzhalter erkannt, der nicht abgeschlossen ist.
        //Der Scan ist hungrig und geht von einem unvollstaendigen Platzhalter
        //aus. Daher ist der Offset von Start-Position bis zum Ende vom Model.
        if (deep > 0)
            return model.length -offset;
        
        //Fall 2: Der Stack ist leer
        //Der Platzhalter wurde komplett ermittelt und der Offset entspricht
        //der Laenge des kompletten Platzhalter mit ggf. enthaltenen Segmenten.
        return cursor -offset +1;
    }

    /**
     * Analysiert das Model und bereitet es final vor.
     * Dazu werden alle Platzhalter auf syntaktische Richtigkeit gepr&uuml;ft.
     * Ggf. werden ung&uuml;ltige Platzhalter entfernt. Zudem werden die Scopes
     * mit den Segmenten (Teilvorlagen) ermittelt und durch einen einfachen
     * Platzhalter ersetzt. Nach dem Parsen entsteht ein finales Model mit
     * optimierten Platzhaltern und extrahierten Segmenten, welches zur Laufzeit
     * nicht ge&auml;ndert werden kann
     * @param  model Model
     * @return das final aufbereitete Model
     */
    private byte[] scan(byte[] model) {
        
        if (model == null)
            return new byte[0];
        
        int cursor = 0;
        while (true) {
            int offset = Generator.scan(model, cursor++);
            if (offset < 0)
                break;
            if (offset == 0)
                continue;
                
            cursor--;            

            byte[] patch = new byte[0];
            String fetch = new String(model, cursor, offset);
            if (fetch.matches("^(?si)#\\[[a-z]([\\w\\-]*\\w)*\\[\\[.*\\]\\]\\]$")) {
                
                //der Scope wird ermittelt aus: #[scope[[segment]]]
                String scope = fetch.substring(2);
                scope = scope.substring(0, scope.indexOf('['));
                scope = scope.toLowerCase();
                
                //das Segment wird aus dem Model extrahiert
                byte[] cache = new byte[offset -scope.length() -7];
                System.arraycopy(model, cursor +scope.length() +4, cache, 0, cache.length);
                
                //der Scope wird mit dem Segment registriert, wenn der Scope
                //noch nicht existiert
                if (!this.scopes.containsKey(scope))
                    this.scopes.put(scope, this.scan(cache));
                
                //als neuer Platzhalter wird nur der Scope verwendet
                patch = ("#[").concat(scope).concat("]").getBytes();
            } else if (fetch.matches("^(?i)#\\[[a-z]([\\w-]*\\w)*\\]$")) {
                patch = fetch.toLowerCase().getBytes();
            } else if (fetch.matches("^(?i)#\\[0x([0-9a-f]{2})+\\]$")) {
                cursor += fetch.length() +1;
                continue;
            }
            
            //das Model wird mit dem Patch neu aufgebaut
            byte[] cache = new byte[model.length -offset +patch.length];
            System.arraycopy(model, 0, cache, 0, cursor);
            System.arraycopy(patch, 0, cache, cursor, patch.length);
            System.arraycopy(model, cursor +offset, cache, cursor +patch.length, model.length -cursor -offset);
            model = cache;
            
            cursor += patch.length;
        }
        
        return model;
    }
    
    /**
     * Bef&uuml;llt das aktulle Model mit den &uuml;bergebenen Werten.
     * Optional kann das Bef&uuml;llen durch die Angabe eines Scopes auf ein
     * Segment begrenzt werden und/oder mit {@code clean} festgelegt werden, ob
     * der R&uuml;ckgabewert finalisiert und alle ausstehenden Platzhalter
     * entfernt bzw. aufgel&ouml;st werden.
     * @param  scope  Scope bzw. Segment
     * @param  values Werte
     * @param  clean  {@code true} zur finalen Bereinigung
     * @return das bef&uuml;llte Model(Fragment)
     */    
    private byte[] assemble(String scope, Map values, boolean clean) {
        
        Iterator iterator;
        String   label;
        String   fetch;
        
        byte[]   cache;
        byte[]   model;
        byte[]   patch;

        if (this.model == null)
            return new byte[0];

        //Normalisierung der Werte (Kleinschreibung + Glaetten der Schluessel)
        if (values == null)
            values = new HashMap();
        iterator = values.keySet().iterator();
        values = new HashMap(values);
        while (iterator.hasNext()) {
            label = (String)iterator.next();
            values.put(label.toLowerCase().trim(), values.get(label));
        }
        
        //Optional wird der Scope ermittelt.
        if (scope != null) {
            scope = scope.toLowerCase().trim();

            //Wird einer angegeben der nicht existiert, ist nichts zu tun.
            if (!this.scopes.containsKey(scope))
                return this.model;
            
            //Scopes werden unabhaengig aufbereitet und spaeter wie ein
            //einfacher aber exklusiver Platzhalter verarbeitet.
            patch = this.extract(scope, values);
            
            values.clear();
            values.put(scope, patch);
        }
        
        int cursor = 0;
        while (true) {
            int offset = Generator.scan(this.model, cursor++);
            if (offset < 0)
                break;
            if (offset == 0)
                continue;
                
            cursor--;

            patch = new byte[0];
            fetch = new String(this.model, cursor, offset);
            if (fetch.matches("^(?i)#\\[[a-z]([\\w-]*\\w)*\\]$")) {
                fetch = fetch.substring(2, fetch.length() -1);
                
                //die Platzhalter nicht uebermittelter Schluessel werden
                //ignoriert, mit 'clean' werden die Platzhalter geloescht
                if (!values.containsKey(fetch)
                        && !clean) {
                    cursor += fetch.length() +3 +1;
                    continue;
                }
                
                //der Patch wird ueber den Schluessel ermittelt
                Object object = values.get(fetch);

                //Ist der Schluessel ein Segment und der Wert ist eine Map mit
                //Werten, wird das Segment rekursive befuellt. Zum Schutz vor
                //unendlichen Rekursionen, wird der aktuelle Scope aus der
                //Werte-Liste entfernt. Bsp. #[A[[#[B[[#[A[[...]]]...]]]...]]]
                if (this.scopes.containsKey(fetch)
                        && object instanceof Map) {
                    patch = this.extract(fetch, (Map)object);
                } else if (this.scopes.containsKey(fetch)
                        && object instanceof Collection) {
                    //Collections erzeugt durch die tiefe, sich wiederholende
                    //rekursive Generierung komplexe Strukturen/Tabellen.
                    iterator = ((Collection)object).iterator();
                    while (iterator.hasNext()) {
                        object = iterator.next();
                        if (object instanceof Map) {
                            model = this.extract(fetch, (Map)object);
                        } else if (object instanceof byte[]) {
                            model = (byte[])object;
                        } else if (object != null) {
                            model = String.valueOf(object).getBytes();
                        } else continue;
                        cache = new byte[patch.length +model.length];
                        System.arraycopy(patch, 0, cache, 0, patch.length);
                        System.arraycopy(model, 0, cache, patch.length, model.length);
                        patch = cache; 
                    }
                } else if (object instanceof byte[]) {
                    patch = (byte[])object;
                } else if (object != null) {
                    patch = String.valueOf(object).getBytes();
                }
                
                if (!clean) {
                
                    //ggf. werden die # Zeichen kodiert, um die Platzhalter und
                    //Struktur im Model zu schuetzen
                    int index = 0;
                    while (index < patch.length) {
                        if (patch[index++] != '#')
                            continue;
                        cache = new byte[patch.length +6];
                        System.arraycopy(patch, 0, cache, 0, index);
                        System.arraycopy(("[0x23]").getBytes(), 0, cache, index, 6);
                        System.arraycopy(patch, index, cache, index +6, patch.length -index);
                        patch = cache;
                    }
                    
                    if (this.scopes.containsKey(fetch)) {
                        fetch = ("#[").concat(fetch).concat("]");
                        cache = new byte[patch.length +fetch.length()];
                        System.arraycopy(patch, 0, cache, 0, patch.length);
                        System.arraycopy(fetch.getBytes(), 0, cache, patch.length, fetch.length());
                        patch = cache;
                    }
                }
                
            } else if (fetch.matches("^(?i)#\\[0x([0-9a-f]{2})+\\]$")) {
                
                //Hexadezimale Platzhalter werden nur mit clean aufgeloest, da
                //diese ungewollte (Steuerzeichen)Zeichen enthalten koennen,
                //was das Rendern behindert.
                if (!clean) {
                    cursor += fetch.length() +1;
                    continue;            
                }
                
                //der hexadezimale Code wird in Bytes konvertiert
                fetch = fetch.substring(4, fetch.length() -1); 
                fetch = ("ff").concat(fetch);
                patch = new BigInteger(fetch, 16).toByteArray();
                patch = Arrays.copyOfRange(patch, 2, patch.length);                
            }
            
            //das Model wird mit dem Patch neu aufgebaut
            cache = new byte[this.model.length -offset +patch.length];
            System.arraycopy(this.model, 0, cache, 0, cursor);
            System.arraycopy(patch, 0, cache, cursor, patch.length);
            System.arraycopy(this.model, cursor +offset, cache, cursor +patch.length, this.model.length -cursor -offset);
            this.model = cache;
            
            cursor += patch.length;
        }
        
        return this.model;
    }

    /**
     * R&uuml;ckgabe aller Scopes der Segmente als Enumeration.
     * Freie Scopes (ohne Segment) sind nicht enthalten.
     * @return alle Scopes der Segmente als Enumeration
     */
    public Enumeration scopes() {
        return Collections.enumeration(this.scopes.keySet());
    }

    /**
     * R&uuml;ckgabe der aktuell bef&uuml;llten Vorlage.
     * @return die aktuell bef&uuml;llte Vorlage
     */
    public byte[] extract() {
        return this.assemble(null, null, true).clone();
    }
    
    /**
     * Extrahiert ein angegebenes Segment und setzt dort die Daten.
     * Die Daten der Vorlage werden davon nicht ber&uuml;hrt.
     * @param  scope Segment
     * @return das gef&uuml;llte Segment, kann dieses nicht ermittelt werden,
     *     wird ein leeres Byte-Array zur&uuml;ckgegeben
     */
    public byte[] extract(String scope) {
        return this.extract(scope, null);
    }
    
    /**
     * Extrahiert ein angegebenes Segment und setzt dort die Daten.
     * Die Daten der Vorlage werden davon nicht ber&uuml;hrt.
     * @param  scope  Segment
     * @param  values Werteliste
     * @return das gef&uuml;llte Segment, kann dieses nicht ermittelt werden,
     *     wird ein leeres Byte-Array zur&uuml;ckgegeben
     */
    public byte[] extract(String scope, Map values) {
        
        if (scope != null)
            scope = scope.toLowerCase().trim();
        if (scope == null
                || !scope.matches("^[a-z]([\\w-]*\\w)*$"))
            return new byte[0];
        
        //Intern wird fuer das Segmente (Teilmodel)ein Kopie vom Generator
        //erstellt und dadurch partiell befuellt.
        Generator generator = new Generator();
        generator.scopes = (HashMap)this.scopes.clone();
        generator.scopes.remove(scope);
        generator.model = (byte[])this.scopes.get(scope);
        if (generator.model == null)
            generator.model = new byte[0];
        return generator.assemble(null, values, true);
    }

    /**
     * Setzt die Daten f&uuml;r einen Scope oder ein Segment.
     * @param values Werte
     */
    public void set(Map values) {
        this.set(null, values);
    }

    /**
     * Setzt die Daten f&uuml;r einen Scope oder ein Segment.
     * @param scope  Scope bzw. Segment
     * @param values Werte
     */
    public void set(String scope, Map values) {

        if (scope != null)
            scope = scope.toLowerCase().trim();
        if (scope != null
                && !scope.matches("^[a-z]([\\w-]*\\w)*$"))
            return;
        this.model = this.assemble(scope, values, false);
    }
}