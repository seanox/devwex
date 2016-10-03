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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *  Generator, generiert Daten durch das Bef&uuml;llen von Platzhaltern (Tags).
 *  Platzhalter lassen sich f&uuml;r einzelne Werte oder komplexe Segmente
 *  definieren. Das Bef&uuml;llen erfolgt dann direkt oder nur f&uuml;r
 *  angegebene Namensr&auml;ume (Scopes) und basiert auf Wertetabellen.
 *  Es k&oouml;nnen der komplette Inhalt oder auch nur einzelne Segmente
 *  bef&uuml;llt und herausgel&ouml;st werden.<br>
 *  <dir>
 *    <b>Beschreibung der Syntax</b>
 *  </dir>
 *  <table>
 *    <tr>
 *      <td valign="top" nowrap="nowrap">
 *        <code>#[value]</code>
 *      </td>
 *      <td valign="top">
 *        Setzt an dieser Stelle den Wert f&uuml;r &lt;value&gt; ein und
 *        entfernt den Platzhalter.
 *      </td>
 *    </tr>
 *    <tr>
 *      <td valign="top" nowrap="nowrap">
 *        <code>#[namespace:value]</code>
 *      </td>
 *      <td valign="top">
 *        Setzt an dieser Stelle den Wert f&uuml;r &lt;value&gt; nur in
 *        Verbindung mit dem korrespondierenden Namensraum (Scope) ein und
 *        entfernt den Platzhalter.
 *      </td>
 *    </tr>
 *    <tr>
 *      <td valign="top" nowrap="nowrap">
 *        <code>#[segment:[[...]]]</code>
 *      </td>
 *      <td valign="top">
 *        Definiert ein Segment. Die Verschachtelung und Verwendung weiterer
 *        Segmente ist m&ouml;glich. Da die Platzhalter zum Einf&uuml;gen von
 *        Segmenten erhalten bleiben, k&ouml;nnen diese zum Aufbau von Listen
 *        verwendet werden.
 *      </td>
 *    </tr>
 *    <tr>
 *      <td valign="top" nowrap="nowrap">
 *        <code>#[0x0A]</code>
 *      </td>
 *      <td valign="top">
 *        Maskiert ein Zeichen. Die Umwandlung erfolgt erst mit dem Aufruf der
 *        {@link #extract(String, Hashtable)}, {@link #extract(String)} bzw.
 *        {@link #extract()} Method zum Schluss der Generierung.
 *      </td>
 *    </tr>
 *  </table>
 *  <br>
 *  Generator 5.0 20160804<br>
 *  Copyright (C) 2016 Seanox Software Solutions<br>
 *  Alle Rechte vorbehalten.
 *
 *  @author  Seanox Software Solutions
 *  @version 5.0 20160804
 */
public class Generator {

    /** Segmente der Vorlage */
    private volatile Hashtable segments;

    /** Datenbuffer der Vorlage */
    private volatile byte[] data;

    /**
     *  Konstruktor, richtet den Generator initial ein.
     *  @param data Vorlage als Bytes
     */
    private Generator(byte[] data) {

        this.segments = new Hashtable();
        this.data     = this.prepare(data, null, null, 0);
    }

    /**
     *  Erstellt einen neuen Generator auf Basis der &uuml;bergebenen Vorlage.
     *  @param  data Vorlage
     *  @return der Generator mit der als Bytes &uuml;bergebenen Vorlage
     */
    public static Generator parse(byte[] data) {

        return new Generator(data == null ? new byte[0] : (byte[])data.clone());
    }
    
    /**
     *  Ermittelt ab der angegebenen Position in einem Datenfragmet die Position
     *  des n&auml;chsten Platzhalters oder Segments. Kann keine Position
     *  ermittelt werden, liefert die Methode einen negativen Wert.
     *  @param  bytes  Datenfragmet
     *  @param  cursor Position
     *  @return die Position des n&auml;chsten Platzhalters oder Segments, sonst
     *          ein negativer Wert
     */
    private static int scan(byte[] bytes, int cursor) {
        
        int digit;
        int offset;
        int size;
        
        offset = cursor;

        //Phase 1: Identifizierung eines Platzhalters
        //  - die unterstuetzen Formate: #[...], #[...:[[...]]]
        //  - Hauptmerkmal sind die ersten zwei Zeichen
        //  - alle Platzhalter beginnen mit #[...
        if (cursor +1 > bytes.length
                || bytes[cursor] != '#' || bytes[++cursor] != '[')
            return -1;
        
        //Phase 2: Begrenzung des Platzhalters
        //  - zulaessig sind nur folgende Zeichen: a-z A-Z 0-9 .:_-
        //    abweichende Zeichen werden als Platzhalter ignoriert
        //  - indirekt werden auch [ und ] unterstuetzt, was aber nur zur
        //    internen Klassifizierung und Begrenzung verwendet wird
        while (++cursor < bytes.length) {
            digit = bytes[cursor];
            if (digit < 'a' && digit > 'z'
                && digit < 'A' && digit > 'Z'
                && digit < '0' && digit > '9'
                && digit != '.' && digit != ':' && digit != '_' && digit != '-'
                && digit != '[' && digit != ']')
                return -1;
            
            //nur gueltige Segemente werden gefunden, sonst ignoriert
            //  - das vorangehende Zeichen muss ein Doppelpunkt sein
            //  - das folgende zwei Zeichen muss eine oeffende eckige Klammern
            //    sein, da diese das Segment einschliesst
            //  - die nachfolgende Datenmenge muss fuer die drei schliessenden
            //    Klammern ausreichen
            if (digit == '[' && (bytes[cursor -1] != ':'
                    || cursor +4 >= bytes.length
                    || bytes[++cursor] != '['))
                return -1;
            
            //gueltige Segemente werden analysiert
            //  - Segment koennen weitere Segmente einschliessen, daher muss
            //    nach weiteren eingeschlossenen Segmenten und nach dem Schluss
            //    des aktuellen Segments gesucht werden
            //  - zur Suche nach den Segmenten wird die Methode rekursive
            //    verwendet
            //  - die Bewertung der gefundenen Platzhalter erfolgt ueber die
            //    letzten frei Zeichen, dabei muss es sich um drei schliessenden
            //    eckige Klammern handeln, sonst ist es kein Segment 
            //  - werden weitere Segmente gefunden, wird der Cursor an das Ende
            //    des gefundenen Segments gesetzt, was das mehrfache Lesen der
            //    Daten minimiert
            if (digit == '[') {
                while (++cursor < bytes.length) {
                    if (cursor +2 >= bytes.length)
                        return -1;
                    //TODO: test inner segements (verschatelte segemente)
                    size = Generator.scan(bytes, cursor);
                    if (size < 0) {
                        if (bytes[cursor] == ']'
                                && bytes[++cursor] == ']'
                                && bytes[++cursor] == ']')
                            return cursor -offset +1;
                    } else cursor += size -1;
                }
            }
            
            //gueltige Platzhalter werden gefunden
            //  - das folgende Zeichen muss eine oeffende eckige Klammer sein,
            //    da diese den Schluessel vom Platzhalter einschliesst
            //  - fuer den Schluessel sind nur folgende Zeichen zulaessig:
            //    a-z A-Z 0-9 .:_-
            //    abweichende Zeichen werden als Platzhalter ignoriert
            //  - dem Schluessel muss die schliessende eckige Klammer folgen,
            //    da diese den Schluessel vom Platzhalter einschliesst
            if (digit == ']')
                return cursor -offset +1;
        }
        
        return -1;
    }
    
    /**
     *  R&uuml;ckgabe aller Scopes der Segmente als Enumeration.
     *  Freie Scopes (ohne Segment) sind nicht enthalten.
     *  @return alle Scopes der Segmente als Enumeration
     */
    public Enumeration scopes() {
        return this.segments.keys();
    }

    /**
     *  R&uuml;ckgabe der aktuell bef&uuml;llten Vorlage.
     *  @return die aktuell bef&uuml;llte Vorlage
     */
    public byte[] extract() {
        return this.prepare(this.data, null, null, 2);
    }
    
    /**
     *  Extrahiert ein angegebenes Segment und setzt dort die Daten.
     *  Die Daten der Vorlage werden davon nicht ber&uuml;hrt.
     *  @param  scope Segment
     *  @return das gef&uuml;llte Segment, kann dieses nicht ermittelt werden,
     *          wird ein leeres Byte-Array zur&uuml;ckgegeben
     */
    public byte[] extract(String scope) {
        
        byte[] bytes = (byte[])this.segments.get(scope);
        if (bytes == null)
            bytes = new byte[0];
        return this.prepare(bytes, null, null, 2);
    }
    
    /**
     *  Extrahiert ein angegebenes Segment und setzt dort die Daten.
     *  Die Daten der Vorlage werden davon nicht ber&uuml;hrt.
     *  @param  scope  Segment
     *  @param  values Werteliste
     *  @return das gef&uuml;llte Segment, kann dieses nicht ermittelt werden,
     *          wird ein leeres Byte-Array zur&uuml;ckgegeben
     */
    public byte[] extract(String scope, Hashtable values) {
        
        byte[] bytes = (byte[])this.segments.get(scope);
        if (bytes == null)
            bytes = new byte[0];
        bytes = this.prepare(bytes, scope, values, 1);
        return this.prepare(bytes, null, null, 2);
    }

    /**
     *  Bereitet in der &uuml;bergeben Vorlage alle Plazthalter zum angegebenen
     *  Scope bzw. Segment vor bzw. auf oder bereinigt diese.
     *  @param  bytes  Vorlage
     *  @param  scope  Scope bzw. Segment
     *  @param  values Werte
     *  @param  level  Verarbeitungsschritt
     *  @return die ge&auml;nderten Daten als ByteArray
     */
    private byte[] prepare(byte[] bytes, String scope, Hashtable values, int level) {

        Enumeration enumeration;
        String      filter;
        String      label;
        Object      object;
        
        int         cursor;
        int         offset;
        int         shift;
        int         index;
        
        byte[]      cache;
        byte[]      value;
        
        //die Werte werden ggf. normalisiert (Kleinschreibung und gelaettet)
        if (values == null || level != 1)
            values = new Hashtable();
        values = new Hashtable(values);
        enumeration = new Hashtable(values).keys();
        while (enumeration.hasMoreElements()) {
            label = (String)enumeration.nextElement();
            values.put(label.toLowerCase().trim(), values.get(label));
        }
        
        filter = scope != null ? scope.toLowerCase().trim() : "";
        
        for (cursor = 0; cursor < bytes.length; cursor++) {
            
            offset = Generator.scan(bytes, cursor);
            if (offset < 0)
                continue;
            
            shift = 0;
            value = null;
            
            //der Platzhalter wird als Ausschnitt ermittelt, einleitende
            //und beendende Zeichen werden abgeschnitten, womit Scope,
            //Trennung und Schluessel verbleiben
            cache = Arrays.copyOfRange(bytes, cursor +2, cursor +offset -1);
            scope = new String(cache).toLowerCase();
            index = scope.indexOf(':'); 
            
            if (scope.matches("^0x[0-9A-Fa-f]+$")) {

                //Platzhalter mit maskierten Daten werden nur beim Release
                //aufgeloest und sonst ignoriert
                if (level > 1)
                    value = new BigInteger(scope.substring(2), 16).toByteArray();
                
            } else if (level > 1) {

                //ab Level Release werden alle Platzhalter bereinigt
                //neue Scopes werden in diesem Level ignoriert
                value = new byte[0];
                
            } else if (cache[cache.length -1] == ']') {
                
                //es werden immer die letzten drei Bytes betrachtet
                //eine schliessende Klammer an dieser Stelle kann nur auf 
                //ein Segment hinweisen

                //Scope und Daten vom Segment werden ermittelt
                scope = scope.substring(0, Math.max(0, index)).trim();
                cache = Arrays.copyOfRange(cache, index +3, cache.length -2);

                //rekursiv werden weitere innere Segmente gesucht
                cache = this.prepare(cache, null, null, 0);

                //das Segmente wird registriert
                //evtl. innere namensgleiche Segemente werden ignoriert
                this.segments.put(scope, cache);

                //Segmente werden an der Stelle wo diese verwendet werden durch
                //einen Platzhalter abgebildet
                value = ("#[").concat(scope).concat("]").getBytes();
                
                //mit dem negativem Offset wird der Cursor vor den Platzhalter
                //positioniert, damit der normale Prepare-Prozess greift, wenn
                //ein Segement erst nach dem Parsen ermittelt ermittelt wird
                shift = -value.length; 
                
            } else if (level < 1) {

                //im Level Initialize werden alle Platzhalter ignoriert
                
            } else {
                
                if (this.segments.containsKey(scope)) {

                    //Segmente werden nur mit gueltigem Scope/Filer verarbeitet,
                    //ohne Scope/Filer werden diese ignoriert
                    if (scope.equals(filter)) {

                        //der Inhalt von Segmenten laesst sich ueber
                        //gleichnamige Werte ueberschreiben, das Verhalten mit
                        //dem Fortfuehren vom Platzhalter bleibt dabei erhalten

                        if (values.containsKey(scope)) {
                            
                            //der Wert wird ueber den Schluessel ermittelt
                            object = values.get(scope);
                            if (object instanceof byte[])
                                value = (byte[])object;
                            else if (object instanceof String)
                                value = ((String)object).getBytes();
                            else if (object != null)
                                value = String.valueOf(object).getBytes();
                            
                        } else {
                            
                            //das Segement wird rekursiv aufgeloest
                            value = this.prepare((byte[])this.segments.get(scope), scope, values, 1);
                        }
                        
                        //der Platzhalter bleibt erhalten, was durch die
                        //Verschiebung vom Cursor erreicht wird, die Laenge
                        //vom Value kann hier ignoriert werden, da diese am
                        //Ende beruecksichtig wird
                        shift  = offset +1;
                        offset = 0;
                    }
                    
                } else {

                    //Schluessel und Scope werden ermittelt
                    label = scope.substring(index +1).trim();
                    scope = scope.substring(0, Math.max(0, index)).trim();
                    
                    //nur bei uebereinstimmenden Scope wird der Platzhalter
                    //beruecksichtigt bzw. verarbeitet
                    if (scope.equals(filter) || scope.length() == 0) {

                        //der Wert wird ueber den Schluessel ermittelt
                        object = values.get(label);
                        if (object instanceof byte[])
                            value = (byte[])object;
                        else if (object instanceof String)
                            value = ((String)object).getBytes();
                        else if (object != null)
                            value = String.valueOf(object).getBytes();
                    }
                }
            }
            
            if (value != null) {
            
                //die Daten werden eingefuegt
                cache = new byte[bytes.length -offset +value.length];
                System.arraycopy(bytes, 0, cache, 0, cursor);
                System.arraycopy(value, 0, cache, cursor, value.length);
                System.arraycopy(bytes, cursor +offset, cache, cursor +value.length, bytes.length -cursor -offset);
                bytes = cache;
            }
            
            //der neue Cursor wird berechnet
            cursor += value == null ? offset -1 : value.length -1;
            cursor += shift;
        }
        
        return bytes;
    }

    /**
     *  Setzt die Daten f&uuml;r einen Scope oder ein Segment.
     *  @param values Werte
     */
    public void set(Hashtable values) {
        this.data = this.prepare(this.data, null, values, 1);
    }

    /**
     *  Setzt die Daten f&uuml;r einen Scope oder ein Segment.
     *  @param scope  Scope bzw. Segment
     *  @param values Werte
     */
    public void set(String scope, Hashtable values) {
        this.data = this.prepare(this.data, scope, values, 1);
    }
}