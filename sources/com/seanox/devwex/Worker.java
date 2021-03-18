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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.net.ssl.SSLSocket;

/**
 * Worker, wartet auf eingehende HTTP-Anfrage, wertet diese aus, beantwortet
 * diese entsprechend der HTTP-Methode und protokolliert den Zugriff.<br>
 * <br>
 * Hinweis zum Thema Fehlerbehandlung - Die Verarbeitung der Requests soll so
 * tolerant wie m&ouml;glich erfolgen. Somit werden interne Fehler, wenn
 * m&ouml;glich, geschluckt und es erfolgt eine alternative aber sichere
 * Beantwortung. Kann der Request nicht mehr kontrolliert werden, erfolgt ein
 * kompletter Abbruch.
 * <br>
 * Worker 5.4.0 20201209<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 5.4.0 20201209
 */
class Worker implements Runnable {
  
    /** Server Context */
    private volatile String context;

    /** Socket vom Worker */
    private volatile Socket socket;

    /** Socket des Servers */
    private volatile ServerSocket mount;
    
    /** Server Konfiguration */
    private volatile Initialize initialize;

    /** Dateneingangsstrom */
    private volatile InputStream input;

    /** Datenausgangsstrom */
    private volatile OutputStream output;

    /** Zugriffsrechte des Servers */
    private volatile Section access;

    /** Umgebungsvariablen des Servers */
    private volatile Section environment;

    /** Felder des Request-Headers */
    private volatile Section fields;

    /** Filter des Servers */
    private volatile Section filters;

    /** Common Gateway Interfaces des Servers */
    private volatile Section interfaces;

    /** Konfiguration des Servers */
    private volatile Section options;

    /** virtuelle Verzeichnisse des Servers */
    private volatile Section references;
    
    /** Mediatypes des Servers */
    private volatile Section mediatypes;

    /** Statuscodes des Servers */
    private volatile Section statuscodes;

    /** Dokumentenverzeichnis des Servers */
    private volatile String docroot;

    /** Header des Requests */
    private volatile String header;

    /** Mediatype des Requests */
    private volatile String mediatype;

    /** Resource des Requests */
    private volatile String resource;

    /** Gateway Interface */
    private volatile String gateway;
    
    /** Systemverzeichnis des Servers */
    private volatile String sysroot;

    /** Datenflusskontrolle */
    private volatile boolean control;

    /** Blockgr&ouml;sse f&uuml;r Datenzugriffe */
    private volatile int blocksize;

    /** Statuscode des Response */
    private volatile int status;

    /** Interrupt f&uuml;r Systemprozesse im Millisekunden */
    private volatile long interrupt;

    /** Timeout beim ausgehenden Datentransfer in Millisekunden */
    private volatile long isolation;

    /** Timeout bei Datenleerlauf in Millisekunden */
    private volatile long timeout;

    /** Menge der &uuml;bertragenen Daten */
    private volatile long volume;

    /**
     * Konstruktor, richtet den Worker mit Socket ein.
     * @param context    Server Context
     * @param socket     Socket mit dem eingegangen Request
     * @param initialize Server Konfiguraiton
     */
    Worker(String context, ServerSocket socket, Initialize initialize) {
        
        context = context.replaceAll("(?i):[a-z]+$", "");

        this.context    = context;
        this.mount      = socket;
        this.initialize = initialize;
    }

    /**
     * Entfernt aus dem String Parameter und Optionen im Format {@code [...]}.
     * Bereinigt werden alle Angaben, ob voll- oder unvollst&auml;ndig, ab dem
     * ersten Vorkommen.
     * @param  string zu bereinigender String
     * @return der String ohne Parameter und Optionen
     */
    private static String cleanOptions(String string) {
        
        int cursor;

        cursor = string.indexOf('[');
        if (cursor >= 0)
            string = string.substring(0, cursor).trim();

        return string;
    }
    
    /**
     * Erstellt zum String einen hexadezimalen MD5-Hash.
     * @param  string zu dem der Hash erstellt werden soll
     * @return der erstellte hexadezimale MD5-Hash
     * @throws Exception
     *     Im Fall nicht erwarteter Fehler
     */
    private static String textHash(String string) throws Exception {
        
        MessageDigest digest;
        
        byte[]        bytes;
        byte[]        cache;
        byte[]        codec;
        
        int           count;
        int           length;
        int           loop;
        
        if (string == null)
            string = "";

        digest = MessageDigest.getInstance("md5");
        bytes  = digest.digest(string.getBytes());
        length = bytes.length;
        
        codec = ("0123456789abcdef").getBytes();
        
        cache = new byte[length *2];
        for (loop = 0, count = 0; loop < length; loop++) {
            cache[count++] = codec[(bytes[loop] >> 4) & 0xF];
            cache[count++] = codec[(bytes[loop] & 0xF)];
        }
        
        return new String(cache, 0, count);
    }
    
    /**
     * Maskiert im String die Steuerzeichen: BS, HT, LF, FF, CR, ', ",  \ und
     * alle Zeichen ausserhalb vom ASCII-Bereich 0x20-0x7F.
     * Die Maskierung erfolgt per Slash:
     * <ul>
     *   <li>Slash + ISO</li>
     *   <li>Slash + drei Bytes oktal (0x80-0xFF)</li>
     *   <li>Slash + vier Bytes hexadezimal (0x100-0xFFFF)</li>
     * </ul>
     * @param  string zu maskierender String
     * @return der String mit den ggf. maskierten Zeichen.
     */
    public static String textEscape(String string) {
        
        byte[] codex;
        byte[] codec;
        byte[] cache;

        int    code;
        int    count;
        int    cursor;
        int    length;
        int    loop;
        
        if (string == null)
            return null;   
        
        length = string.length();
        
        cache = new byte[length *6];
        
        codex = ("\b\t\n\f\r\"'\\btnfr\"'\\").getBytes();
        codec = ("0123456789ABCDEF").getBytes();
        
        for (loop = count = 0; loop < length; loop++) {
            
            code = string.charAt(loop);
            
            cursor = Arrays.binarySearch(codex, (byte)code);
            if (cursor >= 0 && cursor < 8) {
                cache[count++] = '\\';
                cache[count++] = codex[cursor +8];
            } else if (code > 0xFF) {
                cache[count++] = '\\';
                cache[count++] = 'u';
                cache[count++] = codec[(code >> 12) & 0xF];
                cache[count++] = codec[(code >>  8) & 0xF];
                cache[count++] = codec[(code >>  4) & 0xF];
                cache[count++] = codec[(code & 0xF)];                
            } else if (code < 0x20 || code > 0x7F) {
                cache[count++] = '\\';
                cache[count++] = 'x';
                cache[count++] = codec[(code >> 4) & 0xF];
                cache[count++] = codec[(code & 0xF)];                
            } else cache[count++] = (byte)code;
        }
        
        return new String(Arrays.copyOfRange(cache, 0, count));          
    }    
    
    /**
     * Dekodiert den String tollerant als URL und UTF-8.
     * Tollerant, da fehlerhafte kodierte Zeichenfolgen nicht direkt zum Fehler
     * f&uml;hren, sondern erhalten bleiben und die UTF-8 Kodierung optional
     * betrachtet wird.
     * @param  string zu dekodierender String
     * @return der dekodierte String
     */
    private static String textDecode(String string) {
        
        byte[]  bytes;
        
        boolean control;

        int     code;
        int     count;
        int     length;
        int     loop;
        int     digit;
        int     cursor;
        
        if (string == null)
            string = "";
        
        //Datenpuffer wird eingerichtet
        length = string.length();
        bytes  = new byte[length *2];
        
        for (loop = count = 0; loop < length; loop++) {
            
            //der ASCII Code wird ermittelt
            code = string.charAt(loop);

            if (code == 43)
                code = 32;

            //der Hexcode wird in das ASCII Zeichen umgesetzt
            if (code == 37) {
                loop += 2;
                try {code = Integer.parseInt(string.substring(loop -1, loop +1), 16);
                } catch (Throwable throwable) {
                    loop -= 2;
                }
            }

            bytes[count++] = (byte)code;
        }
        
        bytes  = Arrays.copyOfRange(bytes, 0, count);
        length = bytes.length;
        
        cursor = 0;
        digit  = 0;
        
        control = false; 
        
        for (loop = count = 0; loop < length; loop++) {
            
            //der ASCII Code wird ermittelt
            code = bytes[loop] & 0xFF;

            if (code >= 0xC0 && code <= 0xC3)
                control = true;

            //Decodierung der Bytes als UTF-8, das Muster 10xxxxxx
            //wird um die 6Bits erweitert
            if ((code & 0xC0) == 0x80) {

                digit = (digit << 0x06) | (code & 0x3F);

                if (--cursor == 0) {
                    bytes[count++] = (byte)digit;
                    control = false;
                }

            } else {

                digit  = 0;
                cursor = 0;

                //0xxxxxxx (7Bit/0Byte) werden direkt verwendet,
                if (((code & 0x80) == 0x00) || !control) {
                    bytes[count++] = (byte)code;
                    control = false;
                }
                
                //110xxxxx (5Bit/1Byte), 1110xxxx (4Bit/2Byte),
                //11110xxx (3Bit/3Byte), 111110xx (2Bit/4Byte),
                //1111110x (1Bit/5Byte)                
                if ((code & 0xE0) == 0xC0) {
                    cursor = 1;
                    digit  = code & 0x1F;
                } else if ((code & 0xF0) == 0xE0) {
                    cursor = 2;
                    digit  = code & 0x0F;
                } else if ((code & 0xF8) == 0xF0) {
                    cursor = 3;
                    digit  = code & 0x07;
                } else if ((code & 0xFC) == 0xF8) {
                    cursor = 4;
                    digit  = code & 0x03;
                } else if ((code & 0xFE) == 0xFC) {
                    cursor = 5;
                    digit  = code & 0x01;
                }
            }
        }
        
        return new String(bytes, 0, count);
    }
    
    /**
     * Formatiert das Datum im angebenden Format und in der angegebenen Zone.
     * R&uuml;ckgabe das formatierte Datum, im Fehlerfall ein leerer String.
     * @param  format Formatbeschreibung
     * @param  date   zu formatierendes Datum
     * @param  zone   Zeitzone, {@code null} Standardzone
     * @return das formatierte Datum als String, im Fehlerfall leerer String
     */
    private static String dateFormat(String format, Date date, String zone) {

        SimpleDateFormat pattern;

        //die Formatierung wird eingerichtet
        pattern = new SimpleDateFormat(format, Locale.US);
        
        //die Zeitzone wird gegebenenfalls fuer die Formatierung gesetzt
        if (zone != null) pattern.setTimeZone(TimeZone.getTimeZone(zone));

        //die Zeitangabe wird formatiert
        return pattern.format(date);
    }
    
    /**
     * Liest die Datei einer Datei.
     * @param  file zu lesende Datei
     * @return die gelesenen Daten, im Fehlerfall ein leeres Byte-Array
     */
    private static byte[] fileRead(File file) {
        
        try {return Files.readAllBytes(file.toPath());
        } catch (Throwable throwable) {
            return null;
        }
    }
    
    /**
     * Normalisiert den String eines Pfads und l&ouml;sst ggf. existierende
     * Pfad-Direktiven auf und &auml;ndert das Backslash in Slash.
     * @param  path zu normalisierender Pfad
     * @return der normalisierte Pfad
     */
    private static String fileNormalize(String path) {
        
        String string;
        String stream;
        
        int    cursor;

        //die Pfadangabe wird auf Slash umgestellt
        string = path.replace('\\', '/').trim();

        //mehrfache Slashs werden zusammengefasst
        while ((cursor = string.indexOf("//")) >= 0)
            string = string.substring(0, cursor).concat(string.substring(cursor +1));

        //der Path wird ggf. ausgeglichen /abc/./def/../ghi -> /abc/ghi
        //der Path wird um "/." ausgeglichen
        if (string.endsWith("/."))
            string = string.concat("/");

        while ((cursor = string.indexOf("/./")) >= 0)
            string = string.substring(0, cursor).concat(string.substring(cursor +2));

        //der String wird um "/.." ausgeglichen
        if (string.endsWith("/.."))
            string = string.concat("/");

        while ((cursor = string.indexOf("/../")) >= 0) {

            stream = string.substring(cursor +3);
            string = string.substring(0, cursor);

            cursor = string.lastIndexOf("/");
            cursor = Math.max(0, cursor);
            string = string.substring(0, cursor).concat(stream);
        }

        //mehrfache Slashs werden zusammengefasst
        while ((cursor = string.indexOf("//")) >= 0)
            string = string.substring(0, cursor).concat(string.substring(cursor +1));
        
        return string;
    }

    /**
     * L&ouml;scht die Ressource, handelt es sich um ein Verzeichnis, werden
     * alle Unterdateien und Unterverzeichnisse rekursive gel&ouml;scht.
     * R&uuml;ckgabe {@code true} im Fehlerfall {@code false}.
     * @param  resource zu l&ouml;schende Ressource
     * @return {@code true}, im Fehlerfall {@code false}
     */
    private static boolean fileDelete(File resource) {

        File[] files;

        int    loop;
        
        //bei Verzeichnissen wird die Dateiliste ermittelt rekursive geloescht
        if (resource.isDirectory()) {
            files = resource.listFiles();
            if (files == null)
                return true;
            for (loop = 0; loop < files.length; loop++)
                if (!Worker.fileDelete(files[loop]))
                    return false;
        }

        //der File oder das leere Verzeichnis wird geloescht, ist dies nicht
        //moeglich wird false zurueckgegeben
        return resource.delete();
    }

    /**
     * Pr&uuml;ft ob die Ressource dem {@code IF-(UN)MODIFIED-SINCE} entspricht.
     * R&uuml;ckgabe {@code false} wenn die Ressource in Datum und
     * Dateigr&ouml;sse entspricht, sonst {@code true}.
     * @param  file   Dateiobjekt
     * @param  string Information der Modifikation
     * @return {@code true} wenn Unterschiede in Datum oder Dateigr&ouml;sse
     *     ermittelt wurden
     */
    private static boolean fileIsModified(File file, String string) {

        SimpleDateFormat pattern;
        StringTokenizer  tokenizer;

        int              cursor;
        long             timing;

        if (string.length() <= 0)
            return true;

        //If-(Un)Modified-Since wird geprueft
        tokenizer = new StringTokenizer(string, ";");

        try {

            //die Formatierung wird eingerichtet
            pattern = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US);

            //die Zeitzone wird gegebenenfalls fuer die Formatierung gesetzt
            pattern.setTimeZone(TimeZone.getTimeZone("GMT"));

            timing = pattern.parse(tokenizer.nextToken()).getTime() /1000L;

            //IF-(UN)MODIFIED-SINCE:TIMEDATE wird ueberprueft
            if (timing != file.lastModified() /1000L)
                return true;

            while (tokenizer.hasMoreTokens()) {

                string = tokenizer.nextToken().trim();
                if (!string.toLowerCase().startsWith("length"))
                    continue;

                cursor = string.indexOf("=");
                if (cursor < 0)
                    continue;
                
                //IF-(UN)MODIFIED-SINCE:LENGTH wird ueberprueft
                return file.length() != Long.parseLong(string.substring(cursor +1).trim());
            }

        } catch (Throwable throwable) {
            return true;
        }

        return false;
    }
    
    /**
     * Erstellt zu einem abstrakter File das physische File-Objekt.
     * @param  file abstrakter File 
     * @return das physische File-Objekt, sonst {@code null}
     */
    private static File fileCanonical(File file) {
     
        try {return file.getCanonicalFile();
        } catch (Throwable throwable) {
            return null;
        }
    }

    /**
     * Ruft eine Modul-Methode auf.
     * Wenn erforderlich wird das Modul zuvor geladen und initialisiert.
     * Module werden global geladen und initialisiert. Ist ein Modul beim Aufruf
     * der Methode noch nicht geladen, erfolgt dieses ohne Angabe von Parametern
     * mit der ersten Anforderung.<br>
     * Soll ein Modul mit Parametern initalisiert werden, muss das Modul in der
     * Sektion {@code INITIALIZE} deklariert oder &uuml;ber den
     * Context-ClassLoader vom Devwex-Module-SDK geladen werden.
     * R&uuml;ckgabe {@code true} wenn die Ressource geladen und die Methode
     * erfolgreich aufgerufen wurde.
     * @param  resource Resource
     * @param  invoke   Methode
     * @return {@code true}, im Fehlerfall {@code false}
     */
    private boolean invoke(String resource, String invoke) throws Exception {

        Object object;
        Method method;
        String string;

        if (invoke == null
                || invoke.trim().length() <= 0)
            return false;
        
        object = Service.load(Service.load(resource), null);
        if (object == null)
            return false;

        string = this.environment.get("module_opts");
        if (string.length() <= 0)
            string = null;
        
        //die Methode fuer den Modul-Einsprung wird ermittelt
        method = object.getClass().getMethod(invoke, Object.class, String.class);
        
        //die Methode fuer den Modul-Einsprung wird aufgerufen
        method.invoke(object, this, string);
        
        return true;
    }

    /**
     * Ermittelt f&uuml;r das angegebene virtuelle Verzeichnis den realen Pfad.
     * Wurde der Pfad nicht als virtuelles Verzeichnis eingerichtet, wird ein
     * leerer String zur&uuml;ckgegeben.
     * @param  path Pfad des virtuellen Verzeichnis
     * @return der reale Pfad oder ein leerer String
     */
    private String locate(String path) {

        Enumeration enumeration;
        File        source;
        String      access;
        String      alias;
        String      locale;
        String      location;
        String      options;
        String      reference;
        String      result;
        String      rules;
        String      shadow;
        
        String      buffer;
        String      string;
        String      target;

        boolean     absolute;
        boolean     connect;
        boolean     forbidden;
        boolean     module;
        boolean     redirect;
        boolean     virtual;

        int         cursor;

        //der Pfad wird getrimmt, in Kleinbuchstaben umformatiert, die
        //Backslashs gegen Slashs getauscht
        path = path.replace('\\', '/').trim();

        //fuer die Ermittlung wird der Pfad mit / abgeschlossen um auch
        //Referenzen wie z.B. /a mit /a/ ermitteln zu koennen
        locale = path.toLowerCase();

        if (!locale.endsWith("/"))
            locale = locale.concat("/");

        //initiale Einrichtung der Variablen
        access    = "";
        location  = "";
        options   = "";
        reference = "";
        result    = "";
        shadow    = "";

        absolute  = false;
        forbidden = false;
        module    = false;
        redirect  = false;

        source    = null;

        //die Liste der Referenzen wird ermittelt
        enumeration = this.references.elements();

        //die virtuellen Verzeichnisse werden ermittelt
        while (enumeration.hasMoreElements()) {

            //die Regel wird ermittelt
            rules = ((String)enumeration.nextElement()).toLowerCase();
            rules = this.references.get(rules);

            //Alias, Ziel und Optionen werden ermittelt
            cursor = rules.indexOf('>');
            if (cursor < 0) {
                cursor = rules.replace(']', '[').indexOf('[');
                if (cursor < 0)
                    continue;
                alias = rules.substring(0, cursor).trim();
                rules = rules.substring(cursor).trim();
            } else {
                alias = rules.substring(0, cursor).trim();
                rules = rules.substring(cursor +2).trim();
            }

            target = Worker.cleanOptions(rules);
            alias  = Worker.fileNormalize(alias);
            
            //die Optionen werden ermittelt
            string  = rules.toUpperCase();
            virtual = string.contains("[A]") || string.contains("[M]");
            connect = string.contains("[M]") || string.contains("[R]");
            
            //ungueltige bzw. unvollstaendige Regeln werden ignoriert
            //  - ohne Alias
            //  - Redirect / Modul ohne Ziel
            //  - Alias ohne Ziel und Optionen
            if (alias.length() <= 0
                    || (connect && target.length() <= 0)
                    || (rules.length() <= 0 && target.length() <= 0))
                continue;
            
            //ggf. wird der Alias um Slash erweitert, damit spaeter DOCROOT
            //und Alias einen plausiblen Pfad ergeben
            if (!alias.startsWith("/"))
                alias = ("/").concat(alias);            
            
            //ggf. wird der Alias als Target uebernommen, wenn kein Target
            //angegeben wurde, z.B. wenn fuer einen realen Pfad nur Optionen
            //festgelegt werden
            if (target.length() <= 0)
                target = this.docroot.concat(alias);

            //das Ziel wird mit / abgeschlossen um auch Referenzen zwischen /a
            //und /a/ ermitteln zu koennen
            buffer = alias;
            if (!virtual && !buffer.endsWith("/") && buffer.length() > 0)
                buffer = buffer.concat("/");
            
            //die qualifizierteste laengste Refrerenz wird ermittelt
            if (locale.startsWith(buffer.toLowerCase())
                    && buffer.length() > 0 && reference.length() <= buffer.length()) {

                //optional wird die Sperrung des Verzeichnis ermittelt
                forbidden = string.contains("[C]");

                if (!connect) {

                    //die Zieldatei wird eingerichtet
                    //der absolute Pfad wird ermittelt
                    //Verzeichnisse werden ggf. mit Slash beendet
                    source = Worker.fileCanonical(new File(target));
                    if (source != null) {
                        target = source.getPath().replace('\\', '/');
                        if (source.isDirectory() && !target.endsWith("/"))
                            target = target.concat("/");
                    }
                }

                if (source != null || virtual || connect) {

                    location  = target;
                    options   = rules;
                    reference = alias;

                    module    = string.contains("[M]");
                    absolute  = string.contains("[A]") && !module;
                    redirect  = string.contains("[R]") && !module;
                }
            }

            //der Zielpfad wird mit / abgeschlossen um auch Referenzen
            //zwischen /a und /a/ ermitteln zu koennen
            buffer = alias;
            if (!absolute
                    && !module
                    && !buffer.endsWith("/")
                    && buffer.length() > 0)
                buffer = buffer.concat("/");

            //die ACC-Eintraege zur Authentifizierung werden zusammengesammelt
            if (string.contains("[ACC:")
                    && locale.startsWith(buffer.toLowerCase())
                    && buffer.length() > 0
                    && shadow.length() <= buffer.length()) {
                shadow = buffer;
                access = rules.replaceAll("(?i)(\\[(((acc|realm):[^\\[\\]]*?)|d)\\])", "\00$1\01");
                access = access.replaceAll("[^\01]+(\00|$)", "");
                access = access.replaceAll("[\00\01]+", " ");
            }
        }

        //die Referenz wird alternativ ermittelt
        if (reference.length() <= 0) reference = path;

        //konnte eine Referenz ermittelt werden wird diese geparst
        if (reference.length() > 0) {

            //die Location wird ermittelt und gesetzt
            result = absolute || module ? location : location.concat(path.substring(Math.min(path.length(), reference.length())));

            //die Option [A] und [M] wird fuer absolute Referenzen geprueft
            if (absolute || module) {

                string = path.substring(0, Math.min(path.length(), reference.length()));

                this.environment.set("script_name", string);
                this.environment.set("path_context", string);
                this.environment.set("path_info", path.substring(string.length()));
            }

            //die Moduldefinition wird als Umgebungsvariablen gesetzt
            if (module) result = options;
        }
        
        if (result.length() <= 0)
            result = this.docroot.concat(this.environment.get("path_url"));

        if (!module && absolute)
            result = result.concat("[A]");
        if (!module && forbidden)
            result = result.concat("[C]");
        if (!module && redirect) 
            result = result.concat("[R]");

        result = result.concat(access);
                
        return result;
    }
    
    /**
     * &Uuml;berpr&uuml;ft die Zugriffbrechtigungen f&uuml;r eine Referenz und
     * setzt ggf. den entsprechenden Status.
     * @param  reference Referenz
     * @throws Exception
     *     Im Fall nicht erwarteter Fehler
     */
    private void authorize(String reference) throws Exception {
        
        Section         section;
        String          access;
        String          realm;
        String          shadow;
        String          buffer;
        String          string;
        String          target;
        StringTokenizer tokenizer;
        
        boolean         control;
        boolean         digest;
        
        string = reference.toLowerCase();
        if (!string.contains("[acc:") || this.status >= 500)
            return;
        
        //optional wird die Bereichskennung ermittelt
        realm = reference.replaceAll("^(.*(\\[\\s*(?i)realm:([^\\[\\]]*?)\\s*\\]).*)|.*$", "$3");
        realm = realm.replace("\"", "\\\"");
        this.fields.set("auth_realm", realm);
        
        digest = string.contains("[d]");
        
        //der Authentication-Type wird gesetzt
        this.fields.set("auth_type", digest ? "Digest" : "Basic");
        
        //die Werte der ACC-Optionen werden ermittelt
        string = string.replaceAll("\\[acc:([^\\[\\]]*?)\\]", "\00$1\01");
        string = string.replaceAll("((((^|\01).*?)\00)|(\01.*$))|(^.*$)", " ").trim();
        
        control   = false;
        tokenizer = new StringTokenizer(string);
        access    = "";

        //die ACC-Eintraege (Gruppen) werden aufgeloest
        //mit der Option [ACC:NONE] wird die Authorisation aufgehoben
        while (tokenizer.hasMoreTokens()) {

            string = tokenizer.nextToken();
            if (string.equals("none"))
                return;
            control = true;
            access  = access.concat(" ").concat(this.access.get(string));
        }
        
        access = access.trim();
        if (access.length() <= 0) {
            if (control)
                this.status = 401;
            return;
        }
        
        //die Autorisierung wird ermittelt
        string = this.fields.get("http_authorization");

        if (string.toLowerCase().startsWith("digest ")
                && digest) {
            
            string = string.replaceAll("(\\w+)\\s*=\\s*(?:(?:\"(.*?)\")|([^,]*))", "\00$1=$2$3\n");
            string = string.replaceAll("[^\n]+\00", "");
            
            section = Section.parse(string, true);
            
            target = section.get("response");
            shadow = section.get("username");

            buffer = shadow.concat(":").concat(realm).concat(":");
            string = Worker.textHash(this.environment.get("request_method").concat(":").concat(section.get("uri")));
            string = (":").concat(section.get("nonce")).concat(":").concat(section.get("nc")).concat(":").concat(section.get("cnonce")).concat(":").concat(section.get("qop")).concat(":").concat(string);

            tokenizer = new StringTokenizer(access);
            
            while (tokenizer.hasMoreTokens()) {

                access = tokenizer.nextToken();

                if (shadow.equals(access))
                    access = access.substring(shadow.length());
                else if (access.startsWith(shadow.concat(":")))
                    access = access.substring(shadow.length() +1);
                else continue;
                
                access = Worker.textHash(buffer.concat(access));
                access = Worker.textHash(access.concat(string));
                if (target.equals(access)) {
                    this.fields.set("auth_user", shadow);
                    return;
                }
            }
            
        } else if (string.toLowerCase().startsWith("basic ")
                && !digest) {
            
            try {string = new String(Base64.getDecoder().decode(string.substring(6).getBytes())).trim();
            } catch (Throwable throwable) {
                string = "";
            }
            
            access = (" ").concat(access).concat(" ");
            if (string.length() > 0
                    && access.contains((" ").concat(string).concat(" "))) {
                string = string.substring(0, Math.max(0, string.indexOf(':'))).trim();
                this.fields.set("auth_user", string);
                return;
            }
        }
        
        this.status = 401;
    }

    /**
     * &Uuml;berpr&uuml;ft die Filter und wendet diese bei Bedarf an.
     * Filter haben keinen direkten R&uuml;ckgabewert, sie beieinflussen u.a.
     * Server-Status und Datenflusskontrolle.
     */
    private String filter() throws Exception {
        
        Enumeration     enumeration;
        File            file;
        String          valueA;
        String          valueB;
        String          method;
        String          buffer;
        String          string;
        String          location;
        StringTokenizer rules;
        StringTokenizer words;

        boolean         control;
        
        int             cursor;
        int             status;
        
        location = this.resource;

        if (this.status == 400 || this.status >= 500)
            return location;
        
        //FILTER die Filter werden in den Vektor geladen
        //die Steuerung erfolgt ueber REFERENCE, SCRIPT_URI und CODE
        enumeration = this.filters.elements();
        while (enumeration.hasMoreElements()) {

            string = this.filters.get((String)enumeration.nextElement());
            cursor = string.indexOf('>');
            buffer = cursor >= 0 ? string.substring(cursor +1).trim() : "";
            if (cursor >= 0)
                string = string.substring(0, cursor);
            
            //Die Auswertung der Filter erfolgt nach dem Auschlussprinzip.
            //Dazu werden alle Regeln einer Zeile in einer Schleife einzeln
            //geprueft und die Schleife mit der ersten nicht zutreffenden Regel
            //verlassen. Somit wird das Ende der Schleife nur erreicht, wenn
            //keine der Bedingungen versagt hat und damit alle zutreffen.
            
            rules = new StringTokenizer(string, "[+]");
            while (rules.hasMoreTokens()) {

                //die Filterbeschreibung wird ermittelt
                string = rules.nextToken().toLowerCase().trim();

                words = new StringTokenizer(string);
                
                //Methode und Bedingung muessen gesetzt sein
                //mit Tolleranz fuer [+] beim Konkatenieren leerer Bedingungen
                if (words.countTokens() < 2)
                    continue;

                //die Methode wird ermittelt
                string = words.nextToken();

                //die Methode wird auf Erfuellung geprueft
                if (string.length() > 0 && !string.equals("all")
                        && !string.equals(this.environment.get("request_method").toLowerCase()))
                    break;
                
                //die Bedingung wird ermittelt
                string = words.nextToken();
                
                //die Pseudobedingung ALWAYS spricht immer an
                if (!string.equals("always")) {
                    
                    //die Filterkontrolle wird gesetzt, hierbei sind nur IS und
                    //NOT zulaessig, andere Werte sind nicht zulaessig
                    if (!string.equals("is") && !string.equals("not"))
                        break;

                    control = string.equals("is");
                    
                    //Methode und Bedingung muessen gesetzt sein
                    if (words.countTokens() < 2)
                        break;

                    //Funktion und Parameter werden ermittelt
                    method = words.nextToken();
                    string = words.nextToken();
                    
                    //der zu pruefende Wert wird ermittelt
                    string = this.environment.get(string);
                    valueA = string.toLowerCase();
                    valueB = Worker.textDecode(valueA);
                    
                    //der zu pruefende Wert/Ausdruck wird ermittelt
                    string = words.hasMoreTokens() ? words.nextToken() : "";
                    
                    if ((method.equals("starts")
                            && control != (valueA.startsWith(string)
                                || valueB.startsWith(string)))
                        || (method.equals("contains")
                                && control != (valueA.contains(string)
                                        || valueB.contains(string)))
                        || (method.equals("equals")
                                && control != (valueA.equals(string)
                                        || valueB.equals(string)))
                        || (method.equals("ends")
                                && control != (valueA.endsWith(string)
                                        || valueB.endsWith(string)))
                        || (method.equals("match")
                                && control != (valueA.matches(string)
                                        || valueB.matches(string)))
                        || (method.equals("empty")
                                && control != (valueA.length() <= 0)))
                        break;
                
                    if (rules.hasMoreTokens())
                        continue;
                }
                    
                //die Anweisung wird ermittelt
                string = Worker.cleanOptions(buffer);
                
                //wurde ein Modul definiert, wird es optional im Hintergrund
                //als Filter- oder Process-Modul ausgefuehrt, die Verarbeitung
                //endet erst, wenn das Modul die Datenflusskontrolle veraendert
                if (buffer.toUpperCase().contains("[M]") && string.length() > 0) {
                    control = this.control;
                    status  = this.status;
                    this.environment.set("module_opts", buffer);
                    this.invoke(string, "filter");
                    if (this.control != control
                            || this.status != status)
                        return this.resource;
                    continue;
                }
                
                //bei einer Weiterleitung (Redirect) wird STATUS 302 gesetzt   
                if (buffer.toUpperCase().contains("[R]") && string.length() > 0) {
                    this.environment.set("script_uri", string);
                    this.status = 302;
                    return location;
                }
                
                //Verweise auf Dateien oder Verzeichnisse werden diese als
                //Location zurueckgegeben
                if (string.length() > 0) {
                    file = Worker.fileCanonical(new File(buffer));
                    if (file != null && file.exists())
                        return file.getPath();
                }
                
                //sprechen alle Bedingungen an und es gibt keine spezielle
                //Anweisung, wird der STATUS 403 gesetzt
                this.status = 403;

                return location;
            }
        }
        
        return location;
    }
    
    /**
     * Initialisiert die Connection, liest den Request, analysiert diesen und
     * richtet die Connection in der Laufzeitumgebung entsprechen ein.
     * @throws Exception
     *     Im Fall nicht erwarteter Fehler
     */
    private void initiate() throws Exception {

        ByteArrayOutputStream buffer;
        Enumeration           enumeration;
        File                  file;
        String                entry;
        String                method;
        String                shadow;
        String                string;
        StringTokenizer       tokenizer;
        Section               section;

        boolean               connect;
        boolean               secure;
        boolean               virtual;
        
        int                   count;
        int                   cursor;
        int                   digit;
        int                   offset;

        //der Datenpuffer wird zum Auslesen vom Header eingerichtet
        buffer = new ByteArrayOutputStream(65535);
        
        if ((this.socket instanceof SSLSocket))
            try {this.fields.set("auth_cert", ((SSLSocket)this.socket).getSession().getPeerPrincipal().getName());
            } catch (Throwable throwable) {

                //keine Fehlerbehandlung erforderlich
            }
        
        try {

            //das SO-Timeout wird fuer den ServerSocket gesetzt
            this.socket.setSoTimeout((int)this.timeout);

            //die Datenstroeme werden eingerichtet
            this.output = this.socket.getOutputStream();
            this.input  = this.socket.getInputStream();

            //der Inputstream wird gepuffert
            this.input = new BufferedInputStream(this.input, this.blocksize);
            
            count = cursor = offset = 0;
            
            //der Header vom Requests wird gelesen, tritt beim Zugriff auf die
            //Datenstroeme ein Fehler auf, wird STATUS 400 gesetzt
            while (true) {

                if ((digit = this.input.read()) >= 0) {

                    //der Request wird auf kompletten Header geprueft
                    cursor = (digit == ((cursor % 2) == 0 ? 13 : 10)) ? cursor +1 : 0;

                    if (cursor > 0 && count > 0 && offset > 0 && buffer.size() > 0) {

                        string = new String(buffer.toByteArray(), offset, buffer.size() -offset);

                        offset = string.indexOf(':');
                        shadow = string.substring(offset < 0 ? string.length() : offset +1).trim();
                        string = string.substring(0, offset < 0 ? string.length() : offset).trim();

                        //entsprechend RFC 3875 (CGI/1.1-Spezifikation) werden
                        //alle Felder vom HTTP-Header als HTTP-Parameter zur
                        //Verfuegung gestellt, dazu wird das Zeichen - durch _
                        //ersetzt und allen Parametern das Praefix "http_"
                        //vorangestellt
                        
                        string = ("http_").concat(string.replace('-', '_'));
                        if (!this.fields.contains(string)
                                && string.length() > 0
                                && shadow.length() > 0)
                            this.fields.set(string, shadow);

                        offset = buffer.size();
                    }

                    //die Feldlaenge wird berechnet
                    count = cursor > 0 ? 0 : count +1;

                    if (count == 1) offset = buffer.size();

                    //die Zeile eines Felds vom Header muss sich mit 8-Bit
                    //addressieren lassen (fehlende Regelung im RFC 1945/2616)
                    if (count > 32768) this.status = 413;

                    //die Daten werden gespeichert
                    buffer.write(digit);

                    //der Header des Request wird auf 65535 Bytes begrenzt
                    if (buffer.size() >= 65535
                            && cursor < 4) {
                        this.status = 413;
                        break;
                    }
                    
                    //der Request wird auf kompletter Header geprueft
                    if (cursor == 4)
                        break;

                } else {

                    //der Datenstrom wird auf Ende geprueft
                    if (digit >= 0)
                        Thread.sleep(this.interrupt);
                    else break;
                } 
            }

        } catch (Throwable throwable) {
            
            this.status = 400;
            if (throwable instanceof SocketTimeoutException)
                this.status = 408;
        }
        
        //der Header wird vorrangig fuer die Schnittstellen gesetzt
        this.header = buffer.toString().trim();

        //die zusaetzlichen Header-Felder werden vorrangig fuer Filter
        //ermittelt und sind nicht Bestandteil vom (X)CGI, dafuer werden nur
        //die relevanten Parameter wie Methode, Pfade und Query uebernommen

        //die erste Zeile wird ermittelt
        tokenizer = new StringTokenizer(this.header, "\r\n");

        string = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";

        this.fields.set("req_line", string);

        //die Methode vom Request wird ermittelt
        offset = string.indexOf(' ');
        shadow = string.substring(0, offset < 0 ? string.length() : offset);
        string = string.substring(offset < 0 ? string.length() : offset +1);

        this.fields.set("req_method", shadow);

        //ohne HTTP-Methode ist die Anfrage ungueltig, somit STATUS 400
        if (this.status == 0
                && shadow.length() <= 0)
            this.status = 400;

        //Protokoll und Version vom Request werden ermittelt aber ignoriert
        offset = string.lastIndexOf(' ');
        string = string.substring(0, offset < 0 ? string.length() : offset);

        //Pfad und Query vom Request werden ermittelt
        offset = string.indexOf(' ');
        string = string.substring(0, offset < 0 ? string.length() : offset);

        this.fields.set("req_path", string);

        //Pfad und Query vom Request werden ermittelt
        offset = string.indexOf('?');
        shadow = string.substring(offset < 0 ? string.length() : offset +1);
        string = string.substring(0, offset < 0 ? string.length() : offset);

        this.fields.set("req_query", shadow);
        this.fields.set("req_uri", string);

        //der Pfad wird dekodiert
        shadow = Worker.textDecode(string);
        
        //der Pfad wird ausgeglichen /abc/./def/../ghi/ -> /abc/ghi
        string = Worker.fileNormalize(shadow);
        if (shadow.endsWith("/") && !string.endsWith("/"))
            string = string.concat("/");

        this.fields.set("req_path", string);

        //ist der Request nicht korrekt wird STATUS 400 gesetzt
        //enthaelt der Request keinen Header wird STATUS 400 gesetzt
        //enthaelt der Request kein gueltige Pfadangabe wird STATUS 400 gesetzt
        if (this.status == 0
                && (!string.startsWith("/")
                        || this.header.length() <= 0))
            this.status = 400;
        
        //der Host wird ohne Port ermittelt und verwendet
        string = this.fields.get("http_host");
        offset = string.indexOf(':');
        string = string.substring(0, offset < 0 ? string.length() : offset);

        while (string.endsWith("."))
            string = string.substring(0, string.length() -1);

        //wurde im Request kein Host angegeben, wird die aktuelle Adresse verwendet
        if (string.length() <= 0)
            string = this.socket.getLocalAddress().getHostAddress();

        this.fields.set("http_host", string);

        //der Host wird zur Virtualisierung ermittelt
        if (string.length() > 0) {

            string  = ("virtual:").concat(string);
            section = this.initialize.get(string.concat(":ini"));
            shadow  = section.get("server").toLowerCase();

            //die Optionen werden mit allen Vererbungen ermittelt bzw. erweitert
            //wenn ein virtueller Host fuer den Server existiert
            if ((" ").concat(shadow).concat(" ").contains((" ").concat(this.context.toLowerCase()).concat(" "))
                    || shadow.length() <= 0) {
                this.options.merge(section);
                this.references.merge(this.initialize.get(string.concat(":ref")));
                this.access.merge(this.initialize.get(string.concat(":acc")));
                this.filters.merge(this.initialize.get(string.concat(":flt")));
                this.environment.merge(this.initialize.get(string.concat(":env")));
                this.interfaces.merge(this.initialize.get(string.concat(":cgi")));
            }
            
            //die zu verwendende Blockgroesse wird ermittelt
            try {this.blocksize = Integer.parseInt(this.options.get("blocksize"));
            } catch (Throwable throwable) {
                this.blocksize = 65535;
            }

            if (this.blocksize <= 0)
                this.blocksize = 65535;

            string = this.options.get("timeout");

            this.isolation = string.toUpperCase().contains("[S]") ? -1 : 0;

            //das Timeout der Connection wird ermittelt
            try {this.timeout = Long.parseLong(Worker.cleanOptions(string));
            } catch (Throwable throwable) {
                this.timeout = 0;
            }
        }

        //das aktuelle Arbeitsverzeichnis wird ermittelt
        file   = Worker.fileCanonical(new File("."));
        string = file != null ? file.getPath().replace('\\', '/') : ".";
        if (string.endsWith("/"))
            string = string.substring(0, string.length() -1);

        //das Systemverzeichnis wird ermittelt
        file = Worker.fileCanonical(new File(this.options.get("sysroot")));
        this.sysroot = file != null ? file.getPath().replace('\\', '/') : string;
        if (this.sysroot.endsWith("/"))
            this.sysroot = this.sysroot.substring(0, this.sysroot.length() -1);
        if (this.options.get("sysroot").length() <= 0)
            this.sysroot = string;

        //das Dokumentenverzeichnis wird ermittelt
        file = Worker.fileCanonical(new File(this.options.get("docroot")));
        this.docroot = file != null ? file.getPath().replace('\\', '/') : string;
        if (this.docroot.endsWith("/"))
            this.docroot = this.docroot.substring(0, this.docroot.length() -1);
        if (this.options.get("docroot").length() <= 0)
            this.docroot = string;

        //die serverseitig festen Umgebungsvariablen werden gesetzt
        this.environment.set("server_port", String.valueOf(this.socket.getLocalPort()));
        this.environment.set("server_protocol", "HTTP/1.0");
        this.environment.set("server_software", "Seanox-Devwex/#[ant:release-version] #[ant:release-date]");

        this.environment.set("document_root", this.docroot);

        //die Requestspezifischen Umgebungsvariablen werden gesetzt
        this.environment.set("content_length", this.fields.get("http_content_length"));
        this.environment.set("content_type", this.fields.get("http_content_type"));
        this.environment.set("query_string", this.fields.get("req_query"));
        this.environment.set("request", this.fields.get("req_line"));
        this.environment.set("request_method", this.fields.get("req_method"));
        this.environment.set("remote_addr", this.socket.getInetAddress().getHostAddress());
        this.environment.set("remote_port", String.valueOf(this.socket.getPort()));

        //die Unique-Id wird aus dem HashCode des Sockets, den Millisekunden
        //sowie der verwendeten Portnummer ermittelt, die Laenge ist variabel
        string = Long.toString(Math.abs(this.socket.hashCode()), 36);
        string = string.concat(Long.toString(((Math.abs(System.currentTimeMillis()) *100000) +this.socket.getPort()), 36));

        //die eindeutige Request-Id wird gesetzt
        this.environment.set("unique_id", string.toUpperCase());

        //der Path wird ermittelt
        shadow = this.fields.get("req_path");

        //die Umgebungsvariabeln werden entsprechend der Ressource gesetzt
        this.environment.set("path_url", shadow);
        this.environment.set("script_name", shadow);
        
        this.environment.set("script_url", this.fields.get("req_uri"));

        this.environment.set("path_context", "");
        this.environment.set("path_info", "");

        //REFERRENCE - Zur Ressource werden ggf. der virtuellen Pfad im Unix
        //Fileformat (Slash) bzw. der reale Pfad, sowie optionale Parameter,
        //Optionen und Verweise auf eine Authentication ermittelt.
        this.resource = this.locate(shadow);

        //Option [A] fuer eine absolute Referenz wird ermittelt
        string = this.resource.toUpperCase();

        digit  = string.contains("[A]") ?  1 : 0;
        digit |= string.contains("[M]") ?  2 : 0;
        digit |= string.contains("[R]") ?  4 : 0;
        digit |= string.contains("[C]") ?  8 : 0;
        digit |= string.contains("[X]") ? 16 : 0;
        
        virtual = (digit & 1) != 0;
        connect = (digit & (2|4)) != 0;

        //HINWEIS - Auch Module koennen die Option [R] besitzen, diese wird dann
        //aber ignoriert, da die Weiterleitung zu Modulen nur ueber deren
        //virtuellen Pfad erfolgt

        if ((this.status == 0 || this.status == 404) && (digit & 8) != 0) this.status = 403;
        if ((this.status == 0 || this.status == 404) && (digit & 2) != 0) this.status = 0;
        if ((this.status == 0 || this.status == 404) && (digit & (2|4)) == 4) {
            this.environment.set("script_uri", Worker.cleanOptions(this.resource));
            this.status = 302;
        }
        
        //ggf. wird die Zugriffsbrechtigung geprueft
        //unterstuetzt werden Basic- und Digest-Authentication
        this.authorize(this.resource);
        
        //die Request-Parameter (nur mit dem Praefix http_ und auth_) werden in
        //die Umgebungsvariablen uebernommen
        enumeration = this.fields.elements();
        while (enumeration.hasMoreElements()) {
            string = (String)enumeration.nextElement();
            if (!string.startsWith("HTTP_")
                    && !string.startsWith("AUTH_") )
                continue;
            this.environment.set(string, this.fields.get(string));
        }
        
        this.environment.set("remote_user", this.fields.get("auth_user"));
        
        if (!connect)
            this.resource = Worker.cleanOptions(this.resource);

        if (this.resource.length() <= 0)
            this.resource = this.docroot.concat(this.environment.get("path_url"));

        //die Ressource wird als File eingerichtet
        file = new File(this.resource);
        
        //die Ressource wird syntaktisch geprueft, nicht real existierende
        //Ressourcen sowie Abweichungen im  kanonisch Pfad werden mit Status
        //404 quitiert (das schliesst die Bugs im Windows-Dateisystem / / und
        //Punkt vorm Slash ein), die Gross- und Kleinschreibung wird in Windows
        //ignoriert
        if (!connect
                && this.status == 0) {
            File canonical = Worker.fileCanonical(file);
            if (!file.equals(canonical))
                this.status = 404;
            if (canonical != null)
                file = canonical;
            if (file.isDirectory()
                    && file.list() == null)
                this.status = 404;
            this.resource = file.getPath();
        }
        
        if (file.isFile() && shadow.endsWith("/"))
            shadow = shadow.substring(0, shadow.length() -1);

        if (file.isDirectory() && !shadow.endsWith("/"))
            shadow = shadow.concat("/");

        //der HOST oder VIRTUAL HOST wird ermittelt
        entry = this.fields.get("http_host");
        if (entry.length() <= 0)
            entry = this.socket.getLocalAddress().getHostAddress();

        //die OPTION IDENTITY wird geprueft
        if (this.options.get("identity").toLowerCase().equals("on"))
            this.environment.set("server_name", entry);

        //aus dem Schema wird die Verwendung vom Secure-Layer ermittelt
        secure = this.mount instanceof javax.net.ssl.SSLServerSocket;
        
        //die Location wird zusammengestellt
        string = this.environment.get("server_port");
        string = (!string.equals("80") && !secure || !string.equals("443") && secure) && string.length() > 0 ? (":").concat(string) : "";
        string = (secure ? "https" : "http").concat("://").concat(entry).concat(string);

        //die URI vom Skript wird komplementiert
        if (this.status != 302)
            this.environment.set("script_uri", string.concat(this.fields.get("req_path")));

        //bei abweichendem Path wird die Location als Redirect eingerichtet
        if (this.status == 0 && !this.environment.get("path_url").equals(shadow) && !virtual && !connect) {
            this.environment.set("script_uri", string.concat(shadow));
            this.status = 302;
        }

        //die aktuelle Referenz wird ermittelt
        string = this.environment.get("script_uri");
        string = Worker.fileNormalize(string);

        //bezieht sich die Referenz auf ein Verzeichnis und der URI endet
        //aber nicht auf "/" wird STATUS 302 gesetzt
        if (file.isDirectory() && !string.endsWith("/")) {
            this.environment.set("script_uri", string.concat("/"));
            if (this.status == 0)
                this.status = 302;
        }
        
        //DEFAULT, beim Aufruf von Verzeichnissen wird nach einer alternativ
        //anzuzeigenden Datei gesucht, die intern wie ein Verweis verwendet wird
        if (file.isDirectory() && this.status == 0) {

            //das Verzeichnis wird mit Slash abgeschlossen
            if (!this.resource.endsWith("/"))
                this.resource = this.resource.concat("/");

            //die Defaultdateien werden ermittelt
            tokenizer = new StringTokenizer(this.options.get("default").replace('\\', '/'));

            while (tokenizer.hasMoreTokens()) {

                string = tokenizer.nextToken();
                if (string.length() <= 0
                        || string.indexOf('/') >= 0
                        || !new File(this.resource.concat(string)).isFile())
                    continue;

                this.resource = this.resource.concat(string);

                shadow = this.environment.get("path_context");
                if (shadow.length() <= 0)
                    shadow = this.environment.get("path_url");
                if (!shadow.endsWith("/"))
                    shadow = shadow.concat("/");

                this.environment.set("script_name", shadow.concat(string));

                break;
            }
        }
        
        string = Worker.cleanOptions(this.resource);

        this.environment.set("script_filename", string);
        this.environment.set("path_translated", string);

        //der Query String wird ermittelt
        string = this.environment.get("query_string");

        //der Query String wird die Request URI aufbereite
        if (string.length() > 0) string = ("?").concat(string);

        //die Request URI wird gesetzt
        this.environment.set("request_uri", this.fields.get("req_uri").concat(string));

        //die aktuelle HTTP-Methode wird ermittelt
        string = this.environment.get("request_method").toLowerCase();
        
        //METHODS, die zulaessigen Methoden werden ermittelt        
        shadow = (" ").concat(this.options.get("methods").toLowerCase()).concat(" ");

        //die aktuelle Methode wird in der Liste der zulaessigen gesucht, ist
        //nicht enthalten, wird STATUS 405 gesetzt, ausgenommen sind Module
        //mit der Option [X], da an diese alle Methoden weitergereicht werden
        if (((digit & (2 | 16)) != (2 | 16))
                && !shadow.contains((" ").concat(string).concat(" "))
                && this.status <= 0)
            this.status = 405;
        
        this.resource = this.filter();

        string = Worker.cleanOptions(this.resource);

        this.environment.set("script_filename", string);
        this.environment.set("path_translated", string);

        //handelt es sich bei der Ressource um ein Modul wird keine Zuweisung
        //fuer das (X)CGI und den Mediatype vorgenommen
        if (connect || this.resource.toUpperCase().contains("[M]")) {

            this.mediatype = this.options.get("mediatype");
            this.gateway   = this.resource;

            return;
        }
        
        if (this.status == 302) 
            return;

        //die Dateierweiterung wird ermittelt
        cursor = this.resource.lastIndexOf(".");
        entry  = cursor >= 0 ? this.resource.substring(cursor +1) : this.resource;

        //CGI - zur Dateierweiterung wird ggf. eine Anwendung ermittelt
        this.gateway = this.interfaces.get(entry);

        if (this.gateway.length() > 0) {

            cursor = this.gateway.indexOf('>');

            //die zulaessigen Methoden werden ermittelt
            method = this.gateway.substring(0, Math.max(0, cursor)).toLowerCase().trim();

            //die eigentliche Anwendung ermittelt
            if (cursor >= 0) this.gateway = this.gateway.substring(cursor +2).trim();

            //die Variable GATEWAY-INTERFACE wird fuer das (X)CGI gesetzt
            if (this.gateway.length() > 0) {

                this.environment.set("gateway_interface", "CGI/1.1");

                //die Methode wird geprueft ob diese fuer das CGI zugelassen ist
                //ist die Methode nicht zugelassen wird STATUS 405 gesetzt
                string = (" ").concat(this.environment.get("request_method")).concat(" ").toLowerCase();
                shadow = (" ").concat(method).concat(" ");

                if (method.length() > 0
                        && !shadow.contains(string)
                        && !shadow.contains(" all ")
                        && this.status < 500
                        && this.status != 302)
                    this.status = 405;
            }
        }
        
        //der Mediatype wird ermittelt
        this.mediatype = this.mediatypes.get(entry);

        //kann dieser nicht festgelegt werden wird der Standardeintrag aus den
        //Server Basisoptionen eingetragen
        if (this.mediatype.length() <= 0) this.mediatype = this.options.get("mediatype");

        //die vom Client unterstuetzten Mediatypes werden ermittelt
        shadow = this.fields.get("http_accept");
        
        if (shadow.length() > 0) {

            //es wird geprueft ob der Client den Mediatype unterstuetzt,
            //ist dies nicht der Fall, wird STATUS 406 gesetzt
            tokenizer = new StringTokenizer(shadow.toLowerCase().replace(';', ','), ",");
            
            while (this.status == 0) {

                if (tokenizer.hasMoreTokens()) {

                    string = tokenizer.nextToken().trim();
                    if (string.equals(this.mediatype) || string.equals("*/*") || string.equals("*"))
                        break;
                    cursor = this.mediatype.indexOf("/");
                    if (cursor >= 0 && (string.equals(this.mediatype.substring(0, cursor +1).concat("*"))
                            || string.equals(("*").concat(this.mediatype.substring(cursor)))))
                        break;
                    
                } else this.status = 406;
            }
        }
    }
    
    /**
     * Erstellt den Basis-Header f&uuml;r einen Response und erweitert diesen um
     * die optional &uuml;bergebenen Parameter.
     * @param  status HTTP-Status
     * @param  header optionale Liste mit Parameter
     * @return der erstellte Response-Header
     */
    private String header(int status, String[] header) {
        
        String string;
        
        string = String.valueOf(status);
        string = ("HTTP/1.0 ").concat(string).concat(" ").concat(Worker.cleanOptions(this.statuscodes.get(string))).trim();
        if (this.options.get("identity").toLowerCase().equals("on"))
            string = string.concat("\r\nServer: Seanox-Devwex/#[ant:release-version] #[ant:release-date]");
        string = string.concat("\r\n").concat(Worker.dateFormat("'Date: 'E, dd MMM yyyy HH:mm:ss z", new Date(), "GMT"));
        string = string.concat("\r\n").concat(String.join("\r\n", header));

        return string.trim();
    }
    
    /**
     * Erstellt ein Array mit den Umgebungsvariablen.
     * Umgebungsvariablen ohne Wert werden nicht &uuml;bernommen.
     * @return die Umgebungsvariablen als Array
     */
    private String[] getEnvironment() {
        
        Enumeration     enumeration;
        List            list;
        StringTokenizer tokenizer;
        String          label;
        String          value;

        int             index;
        
        list = new ArrayList();
        
        //die Umgebungsvariablen werden ermittelt und uebernommen
        enumeration = this.environment.elements();
        while (enumeration.hasMoreElements()) {
            
            label = (String)enumeration.nextElement();
            value = this.environment.get(label);
            if (value.length() <= 0)
                continue;
            value = label.concat("=").concat(value);
            if (list.contains(value))
                continue;
            list.add(value);
        }
        
        //die Zeilen vom Header werden ermittelt
        tokenizer = new StringTokenizer(this.header, "\r\n");
        
        //die erste Zeile mit dem Request wird verworfen
        if (tokenizer.hasMoreTokens())
            tokenizer.nextToken();
        
        while (tokenizer.hasMoreTokens()) {

            value = tokenizer.nextToken();
            index = value.indexOf(':');
            if (index <= 0)
                continue;
            label = value.substring(0, index).trim();
            value = value.substring(index +1).trim();
            if (label.length() <= 0
                    || value.length() <= 0)
                continue;
            label = ("http_").concat(label.replace('-', '_'));
            value = label.toUpperCase().concat("=").concat(value);
            if (list.contains(value))
                continue;
            list.add(value);            
        }
        
        return (String[])list.toArray(new String[0]);
    }
    
    private void doGateway() throws Exception {
        
        InputStream  error;
        InputStream  input;
        OutputStream output;
        Process      process;
        String       shadow;
        String       header;
        String       string;
        
        String[]     environment;

        byte[]       bytes;

        int          cursor;
        int          length;
        int          offset;

        long         duration;
        long         size;

        if (this.gateway.toUpperCase().contains("[M]")) {
            
            //die Moduldefinition wird als Umgebungsvariablen gesetzt
            this.environment.set("module_opts", this.gateway);
            
            this.invoke(Worker.cleanOptions(this.gateway), "service");
        
            return;
        }
        
        string = this.gateway;
        
        shadow = this.environment.get("script_filename");
        cursor = shadow.replace('\\', '/').lastIndexOf("/") +1;
        string = string.replace("[d]", "[D]");
        string = string.replace("[D]", shadow.substring(0, cursor));
        
        string = string.replace("[c]", "[C]");
        string = string.replace("[C]", shadow.replace((File.separator.equals("/")) ? '\\' : '/', File.separatorChar));

        shadow = shadow.substring(cursor);
        cursor = shadow.lastIndexOf(".");
        string = string.replace("[n]", "[N]");
        string = string.replace("[N]", shadow.substring(0, cursor < 0 ? shadow.length() : cursor));
        
        string = Worker.cleanOptions(string);
        
        //die maximale Prozesslaufzeit wird ermittelt
        try {duration = Long.parseLong(this.options.get("duration"));
        } catch (Throwable throwable) {
            duration = 0;
        }

        //der zeitliche Verfall des Prozess wird ermittelt
        if (duration > 0)
            duration += System.currentTimeMillis();
        
        //der Datenpuffer wird entsprechen der BLOCKSIZE eingerichtet
        bytes = new byte[this.blocksize];

        //initiale Einrichtung der Variablen
        environment = this.getEnvironment();

        //der Prozess wird gestartet, Daten werden soweit ausgelesen
        //bis der Prozess beendet wird oder ein Fehler auftritt
        process = Runtime.getRuntime().exec(string.trim(), environment);

        try {
            
            input  = process.getInputStream();
            output = process.getOutputStream();

            //der XCGI-Header wird aus den CGI-Umgebungsvariablen
            //erstellt und vergleichbar dem Header ins StdIO geschrieben
            if (this.gateway.toUpperCase().contains("[X]"))
                output.write(String.join("\r\n", environment).trim().concat("\r\n\r\n").getBytes());

            //die Laenge des Contents wird ermittelt
            try {length = Integer.parseInt(this.fields.get("http_content_length"));
            } catch (Throwable throwable) {
                length = 0;
            }
            
            while (length > 0) {

                //die Daten werden aus dem Datenstrom gelesen
                size = this.input.read(bytes);
                if (size < 0)
                    break;

                //die an das CGI zu uebermittelnde Datenmenge wird auf die
                //Menge von CONTENT-LENGHT begrenzt
                if (size > length)
                    size = length;
                
                //die Daten werden an das CGI uebergeben
                output.write(bytes, 0, (int)size);

                //das verbleibende Datenvolumen wird berechnet
                length -= size;
                
                //die maximale Prozesslaufzeit wird geprueft
                if (duration > 0
                        && duration < System.currentTimeMillis()) {
                    this.status = 504;
                    break;
                }

                Thread.sleep(this.interrupt);
            }        
            
            //der Datenstrom wird geschlossen
            if (process.isAlive())
                output.close();
            
            //der Prozess wird zwangsweise beendet um auch die Prozesse
            //abzubrechen deren Verarbeitung fehlerhaft verlief
            if (this.status != 200)
                return;
            
            //der Datenpuffer fuer den  wird eingerichtet
            header = new String();
            
            //Responsedaten werden bis zum Ende der Anwendung gelesen
            //dazu wird initial die Datenflusskontrolle gesetzt
            while (true) {
                
                //das Beenden der Connection wird geprueft
                try {this.socket.getSoTimeout();
                } catch (Throwable throwable) {
                    this.status = 503;
                    break;
                }
                
                if (input.available() > 0) {
                    
                    //die Daten werden aus dem StdIO gelesen
                    length = input.read(bytes);
                    offset = 0;
                    
                    //fuer die Analyse vom Header wird nur die erste Zeile
                    //vom CGI-Output ausgewertet, der Header selbst wird
                    //vom Server nicht geprueft oder manipuliert, lediglich
                    //die erste Zeile wird HTTP-konform aufbereitet                        
                    if (this.control && header != null) {
                        
                        header = header.concat(new String(bytes, 0, length));
                        cursor = header.indexOf("\r\n\r\n");
                        if (cursor >= 0) {
                            offset = length -(header.length() -cursor -4); 
                            header = header.substring(0, cursor);
                        } else offset = header.length();
                        
                        //der Buffer zur Header-Analyse ist auf 65535 Bytes
                        //begrenzt, Ueberschreiten fuehrt zum STATUS 502                            
                        if (header.length() > 65535) {
                            
                            this.status = 502;
                            break;
                            
                        } else if (cursor >= 0) {
                            
                            header = header.trim();
                            cursor = header.indexOf("\r\n");
                            string = cursor >= 0 ? header.substring(0, cursor).trim() : header;
                            shadow = string.toUpperCase();
                            
                            if (shadow.startsWith("HTTP/")) {

                                if (shadow.matches("^HTTP/STATUS(\\s.*)*$"))
                                    header = null;
                                    
                                try {this.status = Math.abs(Integer.parseInt(string.replaceAll("^([^\\s]+)\\s*([^\\s]+)*\\s*(.*?)\\s*$", "$2")));
                                } catch (Throwable throwable) {
                                    //keine Fehlerbehandlung erforderlich
                                } 

                                //ggf. werden die Statuscodes mit eigenen bzw.
                                //unbekannten Codes und Text temporaer
                                //angereichert, mit dem Ende der Connection wird
                                //die Liste verworfen
                                string = string.replaceAll("^([^\\s]+)\\s*([^\\s]+)*\\s*(.*?)\\s*$", "$3");
                                if (string.length() > 0
                                        && !this.statuscodes.contains(String.valueOf(this.status)))
                                    this.statuscodes.set(String.valueOf(this.status), string);
                            }
                            
                            //beginnt der Response mit HTTP/STATUS, wird der
                            //Datenstrom ausgelesen, nicht aber an den Client
                            //weitergeleitet, die Beantwortung vom Request
                            //uebernimmt in diesem Fall der Server

                            if (header != null) {

                                header = header.replaceAll("(?si)^ *HTTP */ *[^\r\n]*([\r\n]+|$)", "");
                                header = this.header(this.status, header.split("[\r\n]+"));
                                this.control = false;
                            }                                
                        }
                    }

                    //die Daten werden nur in den Output geschrieben, wenn die
                    //Ausgabekontrolle gesetzt wurde und der Response nicht mit
                    //HTTP/STATUS beginnt
                    if (!this.control) {
                        
                        //der Zeitpunkt wird registriert, um auf blockierte
                        //Datenstroeme reagieren zu koennen
                        if (this.isolation != 0)
                            this.isolation = System.currentTimeMillis();
                        
                        if (header != null)
                            this.output.write(header.concat("\r\n\r\n").getBytes());
                        header = null;

                        this.output.write(bytes, offset, length -offset);

                        if (this.isolation != 0)
                            this.isolation = -1;
                        
                        this.volume += length -offset;
                    }
                }

                //die maximale Prozesslaufzeit wird geprueft
                if (duration > 0
                        && duration < System.currentTimeMillis()) {
                    this.status = 504;
                    break;
                 }
                
                //der Datenstrom wird auf vorliegende Daten
                //und der Prozess wird auf sein Ende geprueft
                if (input.available() <= 0
                        && !process.isAlive())
                    break;
                
                Thread.sleep(this.interrupt);
            }
            
        } finally {
            
            try {
                string = new String();
                error  = process.getErrorStream();
                while (error.available() > 0) {
                    length = error.read(bytes);
                    string = string.concat(new String(bytes, 0, length));
                }
                string = string.trim();
                if (!string.isEmpty())
                    Service.print(("GATEWAY ").concat(string));
                
            } finally {
                
                //der Prozess wird zwangsweise beendet um auch die Prozesse
                //abzubrechen deren Verarbeitung fehlerhaft verlief
                try {process.destroy();
                } catch (Throwable throwable) {
                    //keine Fehlerbehandlung erforderlich
                }
            }
        }
    }
    
    /**
     * Erstellt vom angeforderten Verzeichnisse auf Basis vom Template
     * {@code index.html} eine navigierbare HTML-Seite.
     * @param  directory Verzeichnis des Dateisystems
     * @param  query     Option der Sortierung
     * @return das Verzeichnisse als navigierbares HTML
     */
    private byte[] createDirectoryIndex(File directory, String query) {
        
        Enumeration     enumeration;
        File            file;
        Generator       generator;
        Hashtable       data;
        Hashtable       values;
        List            list;
        List            storage;
        StringTokenizer tokenizer;
        String          entry;
        String          path;
        String          string;

        File[]          files; 
        String[]        entries;

        int[]           assign;

        boolean         control;
        boolean         reverse;

        int             cursor;
        int             digit;
        int             loop;
        
        values = new Hashtable();
        
        //der Header wird mit den Umgebungsvariablen zusammengefasst,
        //die serverseitig gesetzten haben dabei die hoehere Prioritaet
        enumeration = this.environment.elements();
        while (enumeration.hasMoreElements()) {
            entry = (String)enumeration.nextElement();
            if (entry.toLowerCase().equals("path")
                    || entry.toLowerCase().equals("file"))
                continue;
            values.put(entry, this.environment.get(entry));
        }

        //die Zuordung der Felder fuer die Sortierung wird definiert
        if (query.length() <= 0)
            query = "n";
        query = query.substring(0, 1);
        digit = query.charAt(0);
        
        reverse = digit >= 'A' && digit <= 'Z';
        
        query = query.toLowerCase(); 
        digit = query.charAt(0);
         
        //case, name, date, size, type
        assign = new int[] {0, 1, 2, 3, 4};
        
        //die Sortierung wird durch die Query festgelegt und erfolgt nach
        //Case, Query und Name, die Eintraege sind als Array abgelegt um eine
        //einfache und flexible Zuordnung der Sortierreihenfolge zu erreichen
        //0 - case, 1 - name, 3 - date, 4 - size, 5 - type
        if (digit == 'd') {

            //case, date, name, size, type
            assign = new int[] {0, 2, 1, 3, 4};

        } else if (digit == 's') {

            //case, size, name, date, type
            assign = new int[] {0, 3, 1, 2, 4};

        } else if (digit == 't') {

            //case, type, name, date, size
            assign = new int[] {0, 4, 1, 2, 3};

        } else query = "n";
        
        //das Standard-Template fuer den INDEX wird ermittelt und geladen
        file = new File(this.sysroot.concat("/index.html"));
        
        generator = Generator.parse(Worker.fileRead(file));
        
        string = this.environment.get("path_url");
        if (!string.endsWith("/"))
            string = string.concat("/");
        values.put("path_url", string);
        
        //der Pfad wird fragmentiert, Verzeichnissstruktur koennen so als
        //einzelne Links abgebildet werden
        tokenizer = new StringTokenizer(string, "/");
        for (path = ""; tokenizer.hasMoreTokens();) {

            entry = tokenizer.nextToken();
            path  = path.concat("/").concat(entry);

            values.put("path", path);
            values.put("name", entry);
            
            generator.set("location", values);
        }

        //die Dateiliste wird ermittelt
        files = directory.listFiles();
        if (files == null)
            files = new File[0];
        storage = new ArrayList(Arrays.asList(files));

        //mit der Option [S] werden versteckte Dateien nicht angezeigt
        control = this.options.get("index").toUpperCase().contains("[S]");
        
        entries = new String[5];
        
        //die Dateiinformationen werden zusammengestellt
        for (loop = 0; loop < storage.size(); loop++) {

            //die physiche Datei wird ermittlet
            file = (File)storage.get(loop);
            
            //die Eintraege werden als Array abgelegt um eine einfache und
            //flexible Zuordnung der Sortierreihenfolge zu erreichen
            //0 - base, 1 - name, 2 - date, 3 - size, 4 - type

            //der Basistyp wird ermittelt
            entries[0] = file.isDirectory() ? "directory" : "file";
            
            //der Name wird ermittelt
            entries[1] = file.getName();
            
            //der Zeitpunkt der letzten Aenderung wird ermittelt
            entries[2] = String.format("%tF %<tT", new Object[] {new Date(file.lastModified())});

            //die Groesse wird ermittelt, nicht aber bei Verzeichnissen
            string = file.isDirectory() ? "-" : String.valueOf(file.length());
            
            //die Groesse wird an der ersten Stelle mit dem Character erweitert
            //welches sich aus der Laenge der Groesse ergibt um diese nach
            //numerischer Groesse zu sortieren
            entries[3] = String.valueOf((char)string.length()).concat(string);

            //der Dateityp wird ermittlet, nicht aber bei Verzeichnissen
            string = entries[1];
            cursor = string.lastIndexOf(".");
            string = cursor >= 0 ? string.substring(cursor +1) : "";

            entries[4] = file.isDirectory() ? "-" : string.toLowerCase().trim();
            
            //Dateien und Verzeichnisse der Option "versteckt" werden markiert
            string = String.join("\00 ", new String[] {entries[assign[0]], entries[assign[1]],
                    entries[assign[2]], entries[assign[3]], entries[assign[4]]});
            if (control && file.isHidden())
                string = "";
            storage.set(loop, string);
        }

        //die Dateiliste wird sortiert
        Collections.sort(storage, String.CASE_INSENSITIVE_ORDER);
        if (reverse)
            Collections.reverse(storage);
        
        list = new ArrayList();
        
        //die Dateiinformationen werden zusammengestellt
        for (loop = 0; loop < storage.size(); loop++) {
            
            //nicht anerkannte Dateien werden unterdrueckt
            tokenizer = new StringTokenizer((String)storage.get(loop), "\00");
            if (tokenizer.countTokens() <= 0)
                continue;
            
            data = new Hashtable(values);
            list.add(data);
            
            //die Eintraege werden als Array abgelegt um eine einfache und
            //flexible Zuordnung der Sortierreihenfolge zu erreichen
            //0 - base, 1 - name, 2 - date, 3 - size, 4 - type

            entries[assign[0]] = tokenizer.nextToken();
            entries[assign[1]] = tokenizer.nextToken().substring(1);
            entries[assign[2]] = tokenizer.nextToken().substring(1);
            entries[assign[3]] = tokenizer.nextToken().substring(1);
            entries[assign[4]] = tokenizer.nextToken().substring(1);
            
            data.put("case", entries[0]);
            data.put("name", entries[1]);
            data.put("date", entries[2]);
            data.put("size", entries[3].substring(1));
            data.put("type", entries[4]);
            
            string = entries[4];
            if (!string.equals("-")) {
                string = this.mediatypes.get(string);
                if (string.length() <= 0)
                    string = this.options.get("mediatype");
            } else string = "";

            data.put("mime", string);
        }
        
        query = query.concat(reverse ? "d" : "a");
        if (list.isEmpty())
            query = query.concat(" x");
        values.put("sort", query);

        values.put("file", list);
        generator.set(values);

        return generator.extract();
    }

    private void doGet() throws Exception {
        
        File            file;
        InputStream     input;
        List            header;
        StringTokenizer tokenizer;
        String          method;
        String          string;

        byte[]          bytes;

        long            size;
        long            offset;
        long            limit;
        
        header = new ArrayList();
        
        //die Methode wird ermittelt
        method = this.fields.get("req_method").toLowerCase();
        
        //die Ressource wird eingerichtet
        file = new File(this.resource);
        
        if (file.isDirectory()) {

            //die Option INDEX ON wird ueberprueft
            //bei Verzeichnissen und der INDEX OFF wird STATUS 403 gesetzt
            if (Worker.cleanOptions(this.options.get("index")).toLowerCase().equals("on")) {

                header.add(("Content-Type: ").concat(this.mediatypes.get("html")));

                bytes = new byte[0]; 

                //die Verzeichnisstruktur wird bei METHOD:GET generiert
                if (method.equals("get")) {
                    
                    bytes = this.createDirectoryIndex(file, this.environment.get("query_string"));
                    
                    header.add(("Content-Length: ").concat(String.valueOf(bytes.length)));
                }

                //der Header wird fuer die Ausgabe zusammengestellt
                string = this.header(this.status, (String[])header.toArray(new String[0])).concat("\r\n\r\n");

                //die Connection wird als verwendet gekennzeichnet
                this.control = false;
                
                //der Zeitpunkt wird registriert, um auf blockierte
                //Datenstroeme reagieren zu koennen
                if (this.isolation != 0)
                    this.isolation = System.currentTimeMillis();

                this.output.write(string.getBytes());
                this.output.write(bytes);

                if (this.isolation != 0)
                    this.isolation = -1;
                
                this.volume += bytes.length;
                
                return;
            } 
            
            this.status = 403;
            
            return;
        }       

        //wenn vom Client uebermittelt, wird IF-(UN)MODIFIED-SINCE geprueft
        //und bei (un)gueltiger Angabe STATUS 304/412 gesetzt
        if (!Worker.fileIsModified(file, this.fields.get("http_if_modified_since"))) {
            this.status = 304;
            return;
        }
        
        if (this.fields.contains("http_if_unmodified_since")
                && Worker.fileIsModified(file, this.fields.get("http_if_unmodified_since"))) {
            this.status = 412;
            return;
        }

        offset = 0;
        size   = file.length();
        limit  = size;

        //ggf. werden der partielle Datenbereich RANGE ermittelt
        if (this.fields.contains("http_range")
                && size > 0) {
            
            //mogeliche Header fuer den Range:
            //  Range: ...; bytes=500-999 ...
            //         ...; bytes=-999 ...
            //         ...; bytes=500- ...

            string = this.fields.get("http_range").replace(';', '\n');
            string = Section.parse(string).get("bytes");
            if (string.matches("^(\\d+)*\\s*-\\s*(\\d+)*$")) {
                tokenizer = new StringTokenizer(string, "-");
                try {
                    offset = Long.parseLong(tokenizer.nextToken().trim());
                    if (tokenizer.hasMoreTokens()) {
                        limit = Long.parseLong(tokenizer.nextToken().trim());
                    } else if (string.startsWith("-")) {
                        if (offset > 0) {
                            limit  = size -1;
                            offset = Math.max(0, size -offset);
                        } else limit = -1;
                    }
                    limit = Math.min(limit, size -1);
                    if (offset >= size) {
                        this.status = 416;
                        return;
                    }
                    if (offset <= limit) {
                        this.status = 206;
                        limit++;
                    } else {
                        offset = 0;
                        limit  = size;
                    }
                } catch (Throwable throwable) {
                    
                    offset = 0;
                    size   = file.length();
                    limit  = size;                            
                }
            }
        }
        
        //der Header wird zusammengestellt
        header.add(("Last-Modified: ").concat(Worker.dateFormat("E, dd MMM yyyy HH:mm:ss z", new Date(file.lastModified()), "GMT")));
        header.add(("Content-Length: ").concat(String.valueOf(limit -offset)));
        header.add(("Accept-Ranges: bytes"));
        
        //wenn verfuegbar wird der Content-Type gesetzt
        if (this.mediatype.length() > 0)
            header.add(("Content-Type: ").concat(this.mediatype));

        //ggf. wird der partielle Datenbereich gesetzt
        if (this.status == 206)
            header.add(("Content-Range: bytes ").concat(String.valueOf(offset)).concat("-").concat(String.valueOf(limit -1)).concat("/").concat(String.valueOf(size)));
        
        //der Header wird fuer die Ausgabe zusammengestellt
        string = this.header(this.status, (String[])header.toArray(new String[0])).concat("\r\n\r\n");
        
        //die Connection wird als verwendet gekennzeichnet
        this.control = false;

        //der Zeitpunkt wird registriert, um auf blockierte Datenstroeme
        //reagieren zu koennen
        if (this.isolation != 0)
            this.isolation = System.currentTimeMillis();

        this.output.write(string.getBytes());

        if (this.isolation != 0)
            this.isolation = -1;
        
        if (method.equals("get")) {

            //das ByteArray wird mit dem BLOCKSIZE eingerichtet
            bytes = new byte[this.blocksize];
            
            input = null;
            
            try {

                //der Datenstrom wird eingerichtet
                input = new FileInputStream(this.resource);

                //ggf. wird der partielle Datenbereich beruecksichtigt
                input.skip(offset);

                //der Datenstrom wird ausgelesen
                while ((offset +this.volume < limit)
                        && ((size = input.read(bytes)) >= 0)) {

                    if (this.status == 206 && (offset +this.volume +size > limit))
                        size = limit -(offset +this.volume);

                    //der Zeitpunkt wird registriert, um auf blockierte
                    //Datenstroeme reagieren zu koennen
                    if (this.isolation != 0)
                        this.isolation = System.currentTimeMillis();

                    this.output.write(bytes, 0, Math.max(0, (int)size));

                    if (this.isolation != 0)
                        this.isolation = -1;

                    this.volume += size;
                }

            } finally {

                //der Datenstrom wird geschlossen
                try {input.close();
                } catch (Throwable throwable) {
                    //keine Fehlerbehandlung erforderlich
                }
            }
        }
    }

    private void doPut() throws Exception {
        
        File         file;
        OutputStream output;

        byte[]       bytes;
        
        long         length;
        long         size;
        
        //die Ressource wird eingerichtet
        file = new File(this.resource);
        
        //ohne CONTENT-LENGTH werden Verzeichnisse, sonst Dateien angelegt
        if (!this.fields.contains("http_content_length")) {

            //die Verzeichnisstruktur wird angelegt
            //bei Fehlern wird STATUS 424 gesetzt
            if (!(file.isDirectory() || file.mkdirs()))
                this.status = 424;
            
            //bei erfolgreicher Methode wird STATUS 201 gesetzt
            if (this.status == 200
                    || this.status == 404) {
                this.fields.set("req_location", this.environment.get("script_uri"));
                this.status = 201;
            }
            
            return;
        } 
        
        //die Laenge des Contents wird ermittelt
        try {length = Long.parseLong(this.fields.get("http_content_length"));
        } catch (Throwable throwable) {
            length = -1;
        }

        //die Methode setzt eine gueltige Dateilaenge voraus, ohne gueltige
        //Dateilaenge wird STATUS 411 gesetzt            
        if (length < 0) {
            this.status = 411;
            return;
        }

        //ggf. bestehende Dateien werden entfernt
        file.delete();

        //das Datenpuffer wird eingerichtet
        bytes = new byte[this.blocksize];
        
        output = null;
        
        try {
            
            //der Datenstrom wird eingerichtet
            output = new FileOutputStream(this.resource);

            while (length > 0) {

                //die Daten werden aus dem Datenstrom gelesen
                if ((size = this.input.read(bytes)) < 0)
                    break;

                //die zu schreibende Datenmenge wird ermittelt
                //CONTENT-LENGHT wird dabei nicht ueberschritten
                size = Math.min(size, length);
                
                //die Daten werden geschrieben
                output.write(bytes, 0, (int)size);

                //das verbleibende Datenvolumen wird berechnet
                length -= size;

                Thread.sleep(this.interrupt);
            }
            
            //kann die Datei nicht oder durch Timeout nur unvollstaendig
            //angelegt werden wird STATUS 424 gesetzt
            if (length > 0)
                this.status = 424;

        } finally {

            //der Datenstrom wird geschlossen
            try {output.close();
            } catch (Throwable throwable) {
                //keine Fehlerbehandlung erforderlich
            }
        }
        
        //bei erfolgreicher Methode wird STATUS 201 gesetzt
        if (this.status == 200
                || this.status == 404) {
            this.fields.set("req_location", this.environment.get("script_uri"));
            this.status = 201;
        }
    }
    
    private void doDelete() {
        
        //die angeforderte Ressource wird komplett geloescht,
        //tritt dabei ein Fehler auf wird STATUS 424 gesetzt
        if (Worker.fileDelete(new File(this.resource)))
            return;
        
        this.status = 424;
    }

    private void doStatus() throws Exception {
        
        Enumeration enumeration;
        Generator   generator;
        Hashtable   elements;
        List        header;
        String      method;
        String      string;
        String      shadow;
        
        byte[]      bytes;
        
        header = new ArrayList();
        
        //die Methode wird ermittelt
        method = this.fields.get("req_method").toLowerCase();
        
        if (method.equals("options")
                && (this.status == 302 || this.status == 404))
            this.status = 200;

        if (this.status == 302) {

            //die LOCATION wird fuer die Weiterleitung ermittelt
            string = this.environment.get("query_string");
            if (string.length() > 0)
                string = ("?").concat(string);
            string = this.environment.get("script_uri").concat(string);

            this.fields.set("req_location", string);

        } else {
            
            string = String.valueOf(this.status);

            //das Template fuer den Server Status wird ermittelt
            this.resource = this.sysroot.concat("/status-").concat(string).concat(".html");
            if (!new File(this.resource).exists())
                this.resource = this.sysroot.concat("/status-").concat(string.substring(0, 1)).concat("xx.html");
            if (!new File(this.resource).exists())
                this.resource = this.sysroot.concat("/status.html");
        }

        //der Typ zur Autorisierung wird wenn vorhanden ermittelt
        shadow = this.fields.get("auth_type");

        if (this.status == 401 && shadow.length() > 0) {
            
            string = (" realm=\"").concat(this.fields.get("auth_realm")).concat("\"");

            if (shadow.equals("Digest")) {
                   
                string = ("Digest").concat(string);
                string = string.concat(", qop=\"auth\"");
                shadow = this.environment.get("unique_id");
                string = string.concat(", nonce=\"").concat(shadow).concat("\"");
                shadow = Worker.textHash(shadow.concat(String.valueOf(shadow.hashCode())));
                string = string.concat(", opaque=\"").concat(shadow).concat("\"");
                string = string.concat(", algorithm=\"MD5\"");
                
            } else string = ("Basic").concat(string);
            
            header.add(("WWW-Authenticate: ").concat(string));
        }

        if (this.fields.contains("req_location"))
            header.add(("Location: ").concat(this.fields.get("req_location")));
        
        //der Generator wird eingerichtet, dabei werden fuer die STATUS
        //Klasse 1xx, den STATUS 302 (Redirection), 200 (Success) sowie die
        //METHOD:HEAD/METHOD:OPTIONS keine Templates verwendet
        string = String.valueOf(this.status);
        string = this.statuscodes.get(string);
        generator = Generator.parse(method.equals("head")
                || method.equals("options")
                || string.toUpperCase().contains("[H]") ? null : Worker.fileRead(new File(this.resource)));
        
        //der Header wird mit den Umgebungsvariablen zusammengefasst,
        //die serverseitig gesetzten haben dabei die hoehere Prioritaet
        elements = new Hashtable();

        enumeration = this.fields.elements();
        while (enumeration.hasMoreElements()) {
            string = (String)enumeration.nextElement();
            elements.put(string, this.fields.get(string));
        }
        
        enumeration = this.environment.elements();
        while (enumeration.hasMoreElements()) {
            string = (String)enumeration.nextElement();
            elements.put(string, this.environment.get(string));
        }
        
        string = String.valueOf(this.status);
        elements.put("HTTP_STATUS", string);
        elements.put("HTTP_STATUS_TEXT", Worker.cleanOptions(this.statuscodes.get(string)));
        
        //die allgemeinen Elemente werden gefuellt
        generator.set(elements);

        bytes = generator.extract();

        //bei Bedarf wird im Header CONTENT-TYPE / CONTENT-LENGTH  gesetzt
        if (bytes.length > 0) {
            header.add(("Content-Type: ").concat(this.mediatypes.get("html")));
            header.add(("Content-Length: ").concat(String.valueOf(bytes.length)));
        }
        
        //die verfuegbaren Methoden werden ermittelt und zusammengestellt
        string = String.join(", ", this.options.get("methods").split("\\s+"));
        if (string.length() > 0)
            header.add(("Allow: ").concat(string));
        
        //der Header wird fuer die Ausgabe zusammengestellt
        string = this.header(this.status, (String[])header.toArray(new String[0])).concat("\r\n\r\n");

        //die Connection wird als verwendet gekennzeichnet
        this.control = false;         

        //der Zeitpunkt wird registriert, um auf blockierte Datenstroeme
        //reagieren zu koennen
        if (this.isolation != 0)
            this.isolation = System.currentTimeMillis();

        //der Response wird ausgegeben
        if (this.output != null) {
            this.output.write(string.getBytes());
            this.output.write(bytes);
        }
        
        //das Datenvolumen wird uebernommen
        this.volume += bytes.length;  
            
        if (this.isolation != 0)
            this.isolation = -1;
    }

    /**
     * Nimmt den Request an und organisiert die Verarbeitung und Beantwortung.
     * @throws Exception
     *     Im Fall nicht erwarteter Fehler 
     */
    private void service() throws Exception {

        File   file;
        String method;
        
        try {
            
            //die Connection wird initialisiert, um den Serverprozess nicht zu
            //behindern wird die eigentliche Initialisierung der Connection erst mit
            //laufendem Thread als asynchroner Prozess vorgenommen
            try {this.initiate();
            } catch (Exception exception) {
                this.status = 500;
                throw exception;
            }             
            
            //die Ressource wird eingerichtet
            file = new File(this.resource);

            //die Ressource muss auf Modul, Datei oder Verzeichnis verweisen
            if (this.status == 0) this.status = file.isDirectory() || file.isFile() || this.resource.toUpperCase().contains("[M]") ? 200 : 404;            
            
            if (this.control) {

                //PROCESS/(X)CGI/MODULE - wird bei gueltiger Angabe ausgefuehrt
                if (this.status == 200 && this.gateway.length() > 0) {

                    try {this.doGateway();
                    } catch (Exception exception) {
                        this.status = 502;
                        throw exception;
                    }
                    
                } else {

                    //die Methode wird ermittelt
                    method = this.fields.get("req_method").toLowerCase();

                    //vom Server werden die Methoden OPTIONS, HEAD, GET, PUT, DELETE
                    //unterstuetzt bei anderen Methoden wird STATUS 501 gesetzt
                    if (this.status == 200
                            && !method.equals("options") && !method.equals("head") && !method.equals("get")
                            && !method.equals("put") && !method.equals("delete"))
                        this.status = 501;
                    
                    //METHOD:PUT - wird bei gueltiger Angabe ausgefuehrt
                    if (this.status == 200 && method.equals("head"))
                        try {this.doGet();
                        } catch (Exception exception) {
                            this.status = 500;
                            throw exception;
                        }                    

                    if (this.status == 200 && method.equals("get"))
                        try {this.doGet();
                        } catch (Exception exception) {
                            this.status = 500;
                            throw exception;
                        }                    

                    //METHOD:PUT - wird bei gueltiger Angabe ausgefuehrt
                    if ((this.status == 200 || this.status == 404) && method.equals("put"))
                        try {this.doPut();
                        } catch (Exception exception) {
                            this.status = 424;
                            throw exception;
                        }                    
                    
                    //METHOD:DELETE - wird bei gueltiger Angabe ausgefuehrt
                    if (this.status == 200 && method.equals("delete"))
                        try {this.doDelete();
                        } catch (Exception exception) {
                            this.status = 424;
                            throw exception;
                        }    
                }
            }            
            
        } finally {

            //der Zeitpunkt evtl. blockierender Datenstroeme wird
            //zurueckgesetzt
            if (this.isolation != 0)
                this.isolation = -1;               
            
            //STATUS/ERROR/METHOD:OPTIONS - wird ggf. ausgefuehrt
            if (this.control)
                try {this.doStatus();            
                } catch (IOException exception) {
                }
        }
    }
   
    /** Protokollierte den Zugriff im Protokollmedium. */
    private void register() throws Exception {

        Enumeration  source;
        Generator    generator;
        Hashtable    values;
        OutputStream stream;
        String       format;
        String       output;
        String       string;
        
        //der Client wird ermittelt
        string = this.environment.get("remote_addr");
        try {string = InetAddress.getByName(string).getHostName();
        } catch (Throwable throwable) {
            //keine Fehlerbehandlung erforderlich
        }
        this.environment.set("remote_host", string);
        this.environment.set("response_length", String.valueOf(this.volume));
        this.environment.set("response_status", String.valueOf(this.status == 0 ? 500 : this.status));

        synchronized (Worker.class) {
            
            //das Format vom ACCESSLOG wird ermittelt
            format = this.options.get("accesslog");
            
            //Konvertierung der String-Formater-Syntax in Generator-Syntax
            format = format.replaceAll("#", "#[0x23]");
            format = format.replaceAll("%%", "#[0x25]");
            format = format.replaceAll("%\\[", "#[");
            format = format.replaceAll("%t", "%1\\$t");
 
            //die Zeitsymbole werden aufgeloest
            format = String.format(Locale.US, format, new Object[] {new Date()});

            //Format und Pfad werden am Zeichen > getrennt
            if (format.contains(">")) {
                output = format.split(">")[1].trim();
                format = format.split(">")[0].trim();
            } else output = "";
            
            //ohne Format und/oder mit OFF wird kein Access-Log erstellt
            if (format.length() <= 0
                    || format.toLowerCase().equals("off"))
                return;

            values = new Hashtable();
            source = this.environment.elements();
            while (source.hasMoreElements()) {
                string = (String)source.nextElement();
                values.put(string, Worker.textEscape(this.environment.get(string)));
            }            
            
            generator = Generator.parse(format.getBytes());
            generator.set(values);
            format = new String(generator.extract());
            format = format.replaceAll("(?<=\\s)(''|\"\")((?=\\s)|$)", "-");
            format = format.replaceAll("(\\s)((?=\\s)|$)", "$1-"); 
            format = format.concat(System.lineSeparator());
            
            generator = Generator.parse(output.getBytes());
            generator.set(values);
            output = new String(generator.extract());
            output = output.replaceAll("(?<=\\s)(''|\"\")((?=\\s)|$)", "-");
            output = output.replaceAll("(\\s)((?=\\s)|$)", "$1-").trim(); 
            
            //wurde kein ACCESSLOG definiert wird in den StdIO,
            //sonst in die entsprechende Datei geschrieben
            if (output.length() > 0) {
                stream = new FileOutputStream(output, true);
                try {stream.write(format.getBytes());
                } finally {
                    stream.close();
                }
            } else Service.print(format, false);            
        }
    }
    
    /**
     * Merkt den Worker zum Schliessen vor, wenn diese in der naechsten Zeit
     * nicht mehr verwendet wird. Der Zeitpunkt zum Bereinigen betr&auml;gt
     * 250ms Leerlaufzeit nach der letzen Nutzung. Die Zeit wird &uuml;ber das
     * SoTimeout vom ServerSocket definiert.
     */
    void isolate() {

        if (this.socket == null
                && this.mount != null)
            this.mount = null;
    }

    /**
     * R&uuml;ckgabe {@code true}, wenn der Worker aktiv zur Verf&uuml;gung
     * steht und somit weiterhin Requests entgegen nehmen kann.
     * @return {@code true}, wenn der Worker aktiv verf&uuml;bar ist
     */
    boolean available() {

        //der Socket wird auf Blockaden geprueft und ggf. geschlossen
        if (this.mount != null
                && this.isolation > 0
                && this.isolation < System.currentTimeMillis() -this.timeout)
            this.destroy();

        return this.mount != null && this.socket == null;
    }

    /** Beendet den Worker durch das Schliessen der Datenstr&ouml;me. */
    void destroy() {

        //der ServerSocket wird zurueckgesetzt
        this.mount = null;

        if (this.isolation != 0)
            this.isolation = -1;

        //der Socket wird geschlossen
        try {this.socket.close();
        } catch (Throwable throwable) {
            //keine Fehlerbehandlung erforderlich
        }
    }

    /**
     * Stellt den Einsprung in den Thread zur Verf&uuml;gung und initialisiert
     * den Worker. Um den Serverprozess nicht zu behindern wird die eigentliche
     * Initialisierung vom Worker erst mit dem laufenden Thread als asynchroner
     * Prozess vorgenommen.
     */
    @Override
    public void run() {

        ServerSocket socket;
        String       string;

        //der ServerSocket wird vorgehalten
        socket = this.mount;

        while (this.mount != null) {

            //initiale Einrichtung der Variablen
            this.status = 0;
            this.volume = 0;

            this.control = true;

            this.docroot   = "";
            this.gateway   = "";
            this.resource  = "";
            this.mediatype = "";
            this.sysroot   = "";

            //die Felder vom Header werde eingerichtet
            this.fields = new Section(true);

            //die Konfigurationen wird geladen
            this.access      = (Section)this.initialize.get(this.context.concat(":acc")).clone();
            this.environment = (Section)this.initialize.get(this.context.concat(":env")).clone();
            this.filters     = (Section)this.initialize.get(this.context.concat(":flt")).clone();
            this.interfaces  = (Section)this.initialize.get(this.context.concat(":cgi")).clone();
            this.options     = (Section)this.initialize.get(this.context.concat(":ini")).clone();
            this.references  = (Section)this.initialize.get(this.context.concat(":ref")).clone();
            this.statuscodes = (Section)this.initialize.get("statuscodes").clone();
            this.mediatypes  = (Section)this.initialize.get("mediatypes").clone();

            //die zu verwendende Blockgroesse wird ermittelt
            try {this.blocksize = Integer.parseInt(this.options.get("blocksize"));
            } catch (Throwable throwable) {
                this.blocksize = 65535;
            }

            if (this.blocksize <= 0)
                this.blocksize = 65535;

            string = this.options.get("timeout");

            this.isolation = string.toUpperCase().contains("[S]") ? -1 : 0;

            //das Timeout der Connecton wird ermittelt
            try {this.timeout = Long.parseLong(Worker.cleanOptions(string));
            } catch (Throwable throwable) {
                this.timeout = 0;
            }

            //die maximale Prozesslaufzeit wird gegebenfalls korrigiert
            if (this.timeout < 0)
                this.timeout = 0;

            //der Interrupt wird ermittelt
            try {this.interrupt = Long.parseLong(this.options.get("interrupt"));
            } catch (Throwable throwable) {
                this.interrupt = 10;
            }

            if (this.interrupt < 0)
                this.interrupt = 10;

            try {this.socket = socket.accept();
            } catch (InterruptedIOException exception) {
                continue;
            } catch (IOException exception) {
                break;
            }

            //der Request wird verarbeitet
            try {this.service();
            } catch (Throwable throwable) {
                Service.print(throwable);
            }
            
            //die Connection wird beendet
            try {this.destroy();
            } catch (Throwable throwable) {
                //keine Fehlerbehandlung erforderlich
            }

            //HINWEIS - Beim Schliessen wird der ServerSocket verworfen, um
            //die Connection von Aussen beenden zu koennen, intern wird
            //diese daher nach dem Beenden neu gesetzt

            //der Zugriff wird registriert
            try {this.register();
            } catch (Throwable throwable) {
                Service.print(throwable);
            }

            //der Socket wird alternativ geschlossen
            try {this.socket.close();
            } catch (Throwable throwable) {
                //keine Fehlerbehandlung erforderlich
            }

            //durch das Zuruecksetzen wird die Connection ggf. reaktivert
            this.mount = socket;

            //der Socket wird verworfen
            this.socket = null;
        }
    }
}