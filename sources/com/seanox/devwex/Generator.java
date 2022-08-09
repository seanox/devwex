/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * Devwex, Advanced Server Development
 * Copyright (C) 2022 Seanox Software Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
 * Generator bef&uuml;llt Platzhalter in einer Vorlage (Template/Model) mit den
 * Werten von Schl&uuml;ssel-Werte-Paaren aus einem Verzeichnis ({@link Map}),
 * wenn die Schl&uuml;ssel der Platzhalter-Kennung (Identifier) entsprechen,
 * wobei die Gross-/Kleinschreibung der Schl&uuml;ssel ignoriert wird.<br>
 * <br>
 * Bef&uuml;llt wird auf Byte-Level, das Werte als byte-Arrays nutzt. Andere
 * Datentypen werden automatisch per {@code String.valueOf(value).getBytes()}
 * konvertiert.<br>
 * <br>
 * Platzhalter repr&auml;sentieren Einzelwerte und Strukturen. Strukturen sind
 * dabei in sich verschachtelte Platzhalter mit einer Tiefe von bis zu 65535
 * Ebenen, die ein baumartig Verzeichnis mit Schl&uuml;ssel-Werte-Paaren nutzen.
 * Zudem bilden Strukturen Geltungsbereiche (Scopes), diese sind vergleichbar
 * mit Teilvorlagen, die an beliebiger Stelle durch einfache Platzhalter
 * einf&uuml;gen lassen und k&ouml;nnen auf Basis der Struktur-Kennung dediziert
 * bzw. partiell bef&uuml;llen und extrahieren werden. Da die Platzhalter von
 * Strukturen nach dem Bef&uuml;llen erhalten bleiben, lassen sich diese
 * iterativ wiederverwenden.<br>
 * <br>
 * Strukturen verwenden als Werte {@link Collection} und {@link Map}. Eine
 * {@link Map} enth&auml;lt dann die Werte f&uuml;r die Platzhalter innerhalb
 * der Struktur. Eine {@link Collection} f&uuml;hrt zur Iteration &uuml;ber
 * eine Menge von {@link Map}-Objekten, was vergleichbar mit dem iterativen
 * Aufruf der Methode {@link #set(String, Map)} ist. {@link Map} und
 * {@link Collection} erzeugen tiefe, komplexe und rekursive Strukturen.
 *
 * <h3>Beschreibung der Syntax</h3>
 * Die Syntax der Platzhalter ignoriert die Gross- und Kleinschreibung, muss mit
 * einem Buchstaben beginnen und ist auf folgende Zeichen begrenzt:
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
 *       Definiert eine Struktur (Scope). Die Verschachtelung und Verwendung
 *       weiterer Strukturen und Platzhalter ist m&ouml;glich. Da Platzhalter
 *       von Strukturen beim Einf&uuml;gen erhalten bleiben, &ouml;nnen diese
 *       zum Aufbau von Listen verwendet werden.
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
 * Das Model (Byte-Array) wird initial geparst, die Syntax aller Platzhalter
 * auf Richtigkeit gepr&uuml;ft und ggf. bei Fehlern entfernt. Die Strukturen
 * werden ermittelt und mit ihrer Kennung (Identifier) als Teilvorlage (Scope)
 * registriert und durch einfache Platzhalter ersetzt. Nach dem Parsen  entsteht
 * das finales Model mit optimierten Platzhaltern und extrahierten Strukturen,
 * das zur Laufzeit nicht ge&auml;ndert werden kann.<br>
 * <br>
 * Zur Nutzung des Models stehen dann verschiedene M&ouml;glichkeiten zur
 * Verf&uuml;gung.<br>
 * <br>
 * Mit {@link #set(Map)} werden im Model die Platzhalter durch die
 * &uuml;bergeben Werte ersetzt. Platzhalter zudenen keine Werte existieren,
 * bleiben erhalten. Platzhalter die eine Struktur repr&auml;sentieren
 * werden ebenfalls gesetzt, wenn in den Werten ein korrespondierder
 * Schl&uuml;ssel existiert. Deren Platzhalter bleibt zur erneuten Verwendung
 * direkt nach dem eingef&uuml;gten Wert erhalten.<br>
 * <br>
 * Bei {@link #set(String, Map)} wird nur die angegeben Struktur bef&uuml;llt.
 * Dazu wird eine Kopie der Struktur (Teilvorlage) erstellt und mit den
 * &uuml;bergebenen Werten bef&uuml;llt, alle Platzhalter darin werden entfernt
 * und der Inhalt wird als Wert vor dem Platzhalter eingef&uuml;gt. Somit bleibt
 * auch der Platzhalter von Strukturen zur erneuten Verwendung erhalten.<br>
 * <br>
 * Die Methoden {@link #extract(String)} und {@link #extract(String, Map)}
 * dienen der exklusiven Nutzung von Strukturen (Teilvorlagen), die partiell
 * bef&uuml;llt werden. Das Ergebniss beider Methoden entspricht dem Aufruf von
 * {@link #set(Map)} in Kombination mit {@link #extract()}.<br>
 * <br>
 * Generator 5.2.2 20220731<br>
 * Copyright (C) 2022 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 5.2.2 20220731
 */
public class Generator {

    /** Geltungsbereiche mit Strukturen der Vorlage */
    private HashMap scopes;

    /** Model der Vorlage */
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
     * Ermittelt, ob im &uuml;bergebenen Model an der angegebenen Position ein
     * g&uuml;ltiger Platzhalter beginnt. In dem Fall wird dessen L&auml;ge
     * ermittelt. Ist kein Platzhalter erkennbar, wird der R&uuml;ckgabewert 0
     * sein. Liegen im Model keine weiteren Daten zur Analyse vor (Datenende ist
     * erreicht) wird ein negativer Wert zur&uuml;ckgegeben.
     * @param  model  Model
     * @param  offset Position
     * @return die L&auml;ge eines ermittelten Platzhalters, sonst 0 oder ein
     *     negativer Wert, wenn das Datenende erreicht wurde
     */
    private static int scan(byte[] model, int offset) {
        
        if (model == null
                || offset >= model.length)
            return -1;        

        // Phase 0: Identifizierung eines Platzhalters
        //   - die unterstuetzen Formate: #[...], #[...[[...]]]
        //   - Hauptmerkmal sind die ersten zwei Zeichen
        //   - alle Platzhalter beginnen mit #[...
        if (offset +1 >= model.length
                || model[offset] != '#'
                || model[offset +1] != '[')
            return 0;
            
        int cursor = offset;
        int deep   = 0;

        int[] stack = new int[65535];
        while (offset < model.length) {

            // Der aktuelle Level wird ermittelt.
            int mode = 0;
            if (deep > 0)
                mode = stack[deep];

            // Phase 1-1: Erkennung vom Start eines Platzhalters
            //   - die unterstuetzen Formate: #[...], #[...[[...]]]
            //   - Hauptmerkmal sind die ersten zwei Zeichen
            //   - alle Platzhalter beginnen mit #[...
            // Ein Platzhalter kann nur beginnen, wenn noch kein Stack und damit
            // kein Platzhalter existiert oder wenn zuvor ein
            // Struktur-Platzhalter ermittelt wurde. In beiden Faellen ist das
            // Level ungleich 1 und es startet ein weiterer Stack mit Level 1.
            if (offset +1 < model.length
                    && model[offset] == '#'
                    && model[offset +1] == '['
                    && mode != 1) {
                if (deep < 65535)
                    stack[++deep] = 1;
                offset += 2;
                continue;
            }
            
            // Phase 1-2: Qualifizierung eines Struktur-Platzhalters
            //   - es wird der aktive Level 1 erwartet
            //   - es wird die Zeichenfolge [[ gefunden
            // Der aktuelle Stack wird auf Level 2 gesetzt.
            if (offset +1 < model.length
                    && model[offset] == '['
                    && model[offset +1] == '['
                    && mode == 1) {
                stack[deep] = 2;
                offset += 2;
                continue;
            }

            // Phase 2-1: Erkennung vom Ende eines erkannten Platzhalters
            // Der Level muss 1 sein und Zeichen ] gefunden werden.
            // Dann wird der aktulle Stack entfernt, da die Suche hier
            // abgeschlossen ist.
            if (model[offset] == ']'
                    && mode == 1) {
                if (--deep  <= 0)
                    break;
                offset += 1;
                continue;
            }

            // Phase 2-2: Erkennung vom Ende eines erkannten Platzhalters
            // Der Level muss 2 sein und die Zeichenfolge ]]] gefunden werden.
            // Dann wird der aktulle Stack entfernt, da die Suche hier
            // abgeschlossen ist.
            if (offset +2 < model.length
                    && model[offset +0] == ']'
                    && model[offset +1] == ']'
                    && model[offset +2] == ']'
                    && mode == 2) {
                offset += 2;
                if (--deep <= 0)
                    break;
                offset += 1;
                continue;
            }
            
            offset++;
        }
        
        // Fall 1: Der Stack ist nicht leer
        // Somit wurde ein Platzhalter erkannt, der nicht abgeschlossen ist.
        // Der Scan ist hungrig und geht von einem unvollstaendigen Platzhalter
        // aus. Daher ist der Offset von Start-Position bis zum Ende vom Model.
        if (deep > 0)
            return model.length -cursor;
        
        // Fall 2: Der Stack ist leer
        // Der Platzhalter wurde komplett ermittelt und der Offset entspricht
        // der PositionLaenge des kompletten Platzhalter mit ggf. enthaltenen Strukturen.
        return offset -cursor +1;
    }

    /**
     * Analysiert das Model und bereitet es final vor. Dazu wird Syntax aller
     * Platzhalter auf  Richtigkeit gepr&uuml;ft und ggf. fehlerhafte entfernt.
     * Zudem werden die Scopes mit den Segmenten (Teilvorlagen) ermittelt und
     * durch einen einfachen Platzhalter ersetzt. Nach dem Parsen entsteht ein
     * finales Model mit optimierten Platzhaltern und extrahierten Segmenten,
     * welches zur Laufzeit nicht ge&auml;ndert werden kann
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
            if (fetch.matches("^(?si)#\\[[a-z]([\\w\\-]*\\w)?\\[\\[.*\\]\\]\\]$")) {
                
                // der Scope wird ermittelt aus: #[scope[[segment]]]
                String scope = fetch.substring(2);
                scope = scope.substring(0, scope.indexOf('['));
                scope = scope.toLowerCase();
                
                // das Segment wird aus dem Model extrahiert
                byte[] cache = new byte[offset -scope.length() -7];
                System.arraycopy(model, cursor +scope.length() +4, cache, 0, cache.length);
                
                // der Scope wird mit dem Segment registriert, wenn der Scope
                // noch nicht existiert
                if (!this.scopes.containsKey(scope))
                    this.scopes.put(scope, this.scan(cache));
                
                // als neuer Platzhalter wird nur der Scope verwendet
                patch = ("#[").concat(scope).concat("]").getBytes();
            } else if (fetch.matches("^(?i)#\\[[a-z]([\\w-]*\\w)?\\]$")) {
                patch = fetch.toLowerCase().getBytes();
            } else if (fetch.matches("^(?i)#\\[0x([0-9A-F]{2})+\\]$")) {
                cursor += fetch.length() +1;
                continue;
            }
            
            // das Model wird mit dem Patch neu aufgebaut
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
     * @return das bef&uuml;llte Model(Segment)
     */    
    private byte[] assemble(String scope, Map values, boolean clean) {
        
        Iterator iterator;
        Object   object;
        String   label;

        byte[]   cache;
        byte[]   model;
        byte[]   patch;

        if (this.model == null)
            return new byte[0];

        // Normalisierung der Werte (Kleinschreibung + Glaetten der Schluessel)
        if (values == null)
            values = new HashMap();
        iterator = values.keySet().iterator();
        values = new HashMap(values);
        while (iterator.hasNext()) {
            label = (String)iterator.next();
            values.put(label.toLowerCase().trim(), values.get(label));
        }
        
        // Optional wird der Scope ermittelt.
        if (scope != null) {
            scope = scope.toLowerCase().trim();

            // Wird einer angegeben der nicht existiert, ist nichts zu tun.
            if (!this.scopes.containsKey(scope))
                return this.model;
            
            // Scopes werden unabhaengig aufbereitet und spaeter wie ein
            // einfacher aber exklusiver Platzhalter verarbeitet.
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
            scope = new String(this.model, cursor, offset);
            if (scope.matches("^(?i)#\\[[a-z]([\\w-]*\\w)?\\]$")) {
                scope = scope.substring(2, scope.length() -1);
                
                // die Platzhalter nicht uebermittelter Schluessel werden
                // ignoriert, mit 'clean' werden die Platzhalter geloescht
                if (!values.containsKey(scope)
                        && !clean) {
                    cursor += scope.length() +3 +1;
                    continue;
                }
                
                // der Patch wird ueber den Schluessel ermittelt
                object = values.get(scope);

                // Ist der Schluessel ein Segment und der Wert ist eine Map mit
                // Werten, wird das Segment rekursive befuellt. Zum Schutz vor
                // unendlichen Rekursionen, wird der aktuelle Scope aus der
                // Werte-Liste entfernt. Bsp. #[A[[#[B[[#[A[[...]]]...]]]...]]]
                if (this.scopes.containsKey(scope)
                        && object instanceof Map) {
                    patch = this.extract(scope, (Map)object);
                } else if (this.scopes.containsKey(scope)
                        && object instanceof Collection) {
                    // Collections erzeugt durch die tiefe, sich wiederholende
                    // rekursive Generierung komplexe Strukturen/Tabellen.
                    iterator = ((Collection)object).iterator();
                    while (iterator.hasNext()) {
                        object = iterator.next();
                        if (object instanceof Map) {
                            model = this.extract(scope, (Map)object);
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
                
                    // ggf. werden die # Zeichen kodiert, um die Platzhalter und
                    // Struktur im Model zu schuetzen
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
                    
                    if (this.scopes.containsKey(scope)) {
                        scope = ("#[").concat(scope).concat("]");
                        cache = new byte[patch.length +scope.length()];
                        System.arraycopy(patch, 0, cache, 0, patch.length);
                        System.arraycopy(scope.getBytes(), 0, cache, patch.length, scope.length());
                        patch = cache;
                    }
                }
                
            } else if (scope.matches("^(?i)#\\[0x([0-9A-F]{2})+\\]$")) {
                
                // Hexadezimale Platzhalter werden nur mit clean aufgeloest, da
                // diese ungewollte (Steuerzeichen)Zeichen enthalten koennen,
                // was das Rendern behindert.
                if (!clean) {
                    cursor += scope.length() +1;
                    continue;            
                }
                
                // der hexadezimale Code wird in Bytes konvertiert
                scope = scope.substring(4, scope.length() -1);
                scope = ("ff").concat(scope);
                patch = new BigInteger(scope, 16).toByteArray();
                patch = Arrays.copyOfRange(patch, 2, patch.length);                
            }
            
            // das Model wird mit dem Patch neu aufgebaut
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
                || !scope.matches("^[a-z]([\\w-]*\\w)?$"))
            return new byte[0];
        
        // Intern wird fuer das Segmente (Teilmodel)ein Kopie vom Generator
        // erstellt und dadurch partiell befuellt.
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
                && !scope.matches("^[a-z]([\\w-]*\\w)?$"))
            return;
        this.model = this.assemble(scope, values, false);
    }
}