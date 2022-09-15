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
import java.math.BigInteger;
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
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.net.ssl.SSLSocket;

/**
 * Worker, waits for incoming HTTP request, evaluates it, responds to it
 * according to the HTTP method and logs the access.<br>
 * <br>
 * Note on error handling - The processing of requests should be as tolerant as
 * possible. Thus, internal errors are swallowed if possible and the request is
 * safely responded with an error status. If the request can no longer be
 * controlled, it is completely aborted.
 *
 * @author  Seanox Software Solutions
 * @version 5.5.0 20220912
 */
class Worker implements Runnable {
  
    /** Server Context */
    private final String context;

    /** Server configuration */
    private final Initialize initialize;
    
    /** Accepted socket of the worker */
    private volatile Socket accept;

    /** Socket of the server */
    private volatile ServerSocket socket;
    
    /** Data input stream of the accepted socket */
    private volatile InputStream input;

    /** Data output stream of the accepted socket */
    private volatile OutputStream output;

    /** Access rights of the server */
    private volatile Section access;

    /** Environment variables of the server */
    private volatile Section environment;

    /** Fields of the request header */
    private volatile Section fields;

    /** Filters of the server */
    private volatile Section filters;

    /** Common Gateway Interfaces of the server */
    private volatile Section interfaces;

    /** Section with the connection configuration from the server */
    private volatile Section options;

    /** Section with the configuration of references from the server */
    private volatile Section references;
    
    /** Section with the mediatypes configuration from the server */
    private volatile Section mediatypes;

    /** Section with the status codes configuration from the server */
    private volatile Section statuscodes;

    /** Document directory of the server */
    private volatile String docroot;

    /** Header of the request */
    private volatile String header;

    /** Mediatype of the request */
    private volatile String mediatype;

    /** Resource of the request */
    private volatile String resource;

    /** Gateway interface of the request */
    private volatile String gateway;
    
    /** System directory of the server */
    private volatile String sysroot;

    /** Data flow control */
    private volatile boolean control;

    /** Block size for data access */
    private volatile int blocksize;

    /** Status code of the response */
    private volatile int status;

    /** Interrupt for system processes in milliseconds */
    private volatile long interrupt;

    /** Timeout during outgoing data transfer in milliseconds */
    private volatile long isolation;

    /** Timeout on data idle in milliseconds */
    private volatile long timeout;

    /** Amount of transmitted data */
    private volatile long volume;

    /**
     * Constructor, establishes the worker with socket and configuration.
     * @param context    Server context
     * @param socket     Socket with the accepted request
     * @param initialize Server configuraiton
     */
    Worker(String context, ServerSocket socket, Initialize initialize) {
        this.context    = context.replaceAll("(?i):[a-z]+$", "");
        this.socket     = socket;
        this.initialize = initialize;
    }

    /**
     * Removes parameters and options in the string in the format {@code [...]}.
     * All patterns, even incomplete ones, are cleaned from the first occurrence.
     * @param  string string to be cleaned
     * @return the string without parameters and options
     */
    private static String cleanOptions(String string) {
        int cursor = string.indexOf('[');
        if (cursor >= 0)
            string = string.substring(0, cursor).trim();
        return string;
    }

    /**
     * Creates a hexadecimal MD5 hash for the string.
     * @param  string for which the hash is to be created
     * @return the created hexadecimal MD5 hash
     * @throws Exception
     *     In case of unexpected errors
     */
    private static String textHash(String string)
            throws Exception {
        
        if (string == null)
            string = "";

        MessageDigest digest = MessageDigest.getInstance("md5");
        byte[] bytes  = digest.digest(string.getBytes());
        string = new BigInteger(1, bytes).toString(16);
        while (string.length() < 32)
            string = ("0").concat(string);
        return string;
    }
    
    /**
     * Escapes the control characters in the string: BS, HT, LF, FF, CR, ', ", \
     * and characters outside the ASCII range 0x20-0x7F with a slash sequence:
     * <ul>
     *   <li>Slash + ISO</li>
     *   <li>Slash + three bytes octal (0x80-0xFF)</li>
     *   <li>Slash + four bytes hexadecimal (0x100-0xFFFF)</li>
     * </ul>
     * @param  string string to be escaped
     * @return the string with escaped characters
     */
    private static String textEscape(String string) {
        
        if (string == null)
            return null;   

        int length = string.length();

        byte[] codex = ("\b\t\n\f\r\"'\\btnfr\"'\\").getBytes();
        byte[] codec = ("0123456789ABCDEF").getBytes();
        byte[] cache = new byte[length *6];
        
        int count = 0;
        for (int loop = 0; loop < length; loop++) {
            
            int code = string.charAt(loop);
            
            int cursor = Arrays.binarySearch(codex, (byte)code);
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
        
        return new String(cache, 0, count);
    }    
    
    /**
     * Decodes URL and UTF-8 optionally encoded parts in a string. Non-compliant
     * sequences do cause an error, instead they remain as unknown encoding.
     * @param  string String to be decoded
     * @return the decoded string
     */
    private static String textDecode(String string) {
        
        if (string == null)
            string = "";
        
        // Part 1: URL decoding
        // Non-compliant sequences do cause an error, instead they remain as
        // unknown encoding
        
        int length = string.length();
        
        byte[] bytes = new byte[length *2];
        
        int count = 0;
        for (int loop = 0; loop < length; loop++) {
            
            // ASCII code is determined
            int code = string.charAt(loop);

            // Plus sign is converted to space.
            if (code == 43)
                code = 32;

            // Hexadecimal sequences are converted to ASCII character
            if (code == 37) {
                loop += 2;
                try {code = Integer.parseInt(string.substring(loop -1, loop +1), 16);
                } catch (Throwable throwable) {
                    loop -= 2;
                }
            }

            bytes[count++] = (byte)code;
        }
        
        // Part 2: UTF-8 decoding
        // Non-compliant sequences do cause an error, instead they remain as
        // unknown encoding
        
        bytes  = Arrays.copyOfRange(bytes, 0, count);
        length = bytes.length;
        
        int cursor = 0;
        int digit  = 0;
        
        boolean control = false; 
        
        for (int loop = count = 0; loop < length; loop++) {
            
            // ASCII code is determined
            int code = bytes[loop] & 0xFF;

            if (code >= 0xC0 && code <= 0xC3)
                control = true;

            // decoding of the bytes as UTF-8.
            // pattern 10xxxxxx is extended by the 6Bits
            if ((code & 0xC0) == 0x80) {

                digit = (digit << 0x06) | (code & 0x3F);

                if (--cursor == 0) {
                    bytes[count++] = (byte)digit;
                    control = false;
                }

            } else {

                digit  = 0;
                cursor = 0;

                // 0xxxxxxx (7Bit/0Byte) are used directly
                if (((code & 0x80) == 0x00) || !control) {
                    bytes[count++] = (byte)code;
                    control = false;
                }
                
                // 110xxxxx (5Bit/1Byte), 1110xxxx (4Bit/2Byte),
                // 11110xxx (3Bit/3Byte), 111110xx (2Bit/4Byte),
                // 1111110x (1Bit/5Byte)                
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
     * Formats the date in the specified format and zone.
     * @param  format Format description
     * @param  date   Date to be formatted
     * @param  zone   Time zone, {@code null} default zone
     * @return the formatted date as string, in case of error empty string
     */
    private static String dateFormat(String format, Date date, String zone) {
        SimpleDateFormat pattern = new SimpleDateFormat(format, Locale.US);
        if (zone != null)
            pattern.setTimeZone(TimeZone.getTimeZone(zone));
        return pattern.format(date);
    }
    
    /**
     * Reads the data of a file as a byte array.
     * In case of error null is returned.
     * @param  file file to be read
     * @return the read data, in case of error {@code null}
     */
    private static byte[] fileRead(File file) {
        try {return Files.readAllBytes(file.toPath());
        } catch (Throwable throwable) {
            return null;
        }
    }
    
    /**
     * Normalizes a path, resolves path statements, and changes to slashes.
     * @param  path Path to be normalized
     * @return the normalized path
     */
    private static String fileNormalize(String path) {

        // path is changed to slash
        String string = path.replace('\\', '/').trim();

        // multiple slashes are combined
        for (int cursor; (cursor = string.indexOf("//")) >= 0;)
            string = string.substring(0, cursor).concat(string.substring(cursor +1));

        // compensates in the path /.
        // e.g. /abc/./def/../ghi -> /abc/def/../ghi
        if (string.endsWith("/."))
            string = string.concat("/");

        for (int cursor; (cursor = string.indexOf("/./")) >= 0;)
            string = string.substring(0, cursor).concat(string.substring(cursor +2));

        // compensates in the path /..
        // e.g. /abc/./def/../ghi -> /abc/./ghi
        if (string.endsWith("/.."))
            string = string.concat("/");

        for (int cursor; (cursor = string.indexOf("/../")) >= 0;) {

            String stream;
            
            stream = string.substring(cursor +3);
            string = string.substring(0, cursor);

            cursor = string.lastIndexOf("/");
            cursor = Math.max(0, cursor);
            string = string.substring(0, cursor).concat(stream);
        }

        // multiple consecutive slashes are combined
        for (int cursor; (cursor = string.indexOf("//")) >= 0;)
            string = string.substring(0, cursor).concat(string.substring(cursor +1));
        
        return string;
    }

    /**
     * Deletes the resource.
     * Directories, all files and subdirectories will be deleted recursively.
     * @param  resource zu l&ouml;schende Ressource
     * @return if successful {@code true}, otherwise {@code false}
     */
    private static boolean fileDelete(File resource) {

        // Directories are deleted recursively via the file list
        if (resource.isDirectory()) {
            File[] files = resource.listFiles();
            if (files == null)
                return true;
            for (int loop = 0; loop < files.length; loop++)
                if (!Worker.fileDelete(files[loop]))
                    return false;
        }

        // file or the empty directory will be finally deleted,
        // if this is not possible false will be returned
        return resource.delete();
    }

    /**
     * Checks if the resource corresponds to the {@code IF-(UN)MODIFIED-SINCE}.
     * Returns {@code false} if the resource matches in date and file size,
     * otherwise {@code true}.
     * @param  file   File object
     * @param  string If-(Un)Modified-Since} phrase
     * @return {@code true} if differences in date or file size were detected
     */
    private static boolean fileIsModified(File file, String string) {

        if (string.length() <= 0)
            return true;

        try {

            // die Formatierung wird eingerichtet
            // die Zeitzone wird gegebenenfalls fuer die Formatierung gesetzt
            SimpleDateFormat pattern = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US);
            pattern.setTimeZone(TimeZone.getTimeZone("GMT"));

            // If-(Un)Modified-Since wird geprueft
            // If-(Un)Modified-Since:Timedate wird geprueft
            StringTokenizer tokenizer = new StringTokenizer(string, ";");
            long timing = pattern.parse(tokenizer.nextToken()).getTime() /1000L;
            if (timing != file.lastModified() /1000L)
                return true;

            // If-(Un)Modified-Since:Length wird geprueft
            while (tokenizer.hasMoreTokens()) {
                string = tokenizer.nextToken().trim();
                if (!string.toLowerCase().startsWith("length"))
                    continue;
                int cursor = string.indexOf("=");
                if (cursor < 0)
                    continue;
                return file.length() != Long.parseLong(string.substring(cursor +1).trim());
            }

        } catch (Throwable throwable) {
            return true;
        }

        return false;
    }
    
    /**
     * Creates for an abstract file the physical file object.
     * @param  file abstract file 
     * @return the physical file object, otherwise {@code null}
     */
    private static File fileCanonical(File file) {
        try {return file.getCanonicalFile();
        } catch (Throwable throwable) {
            return null;
        }
    }

    /**
     * Calls a module method.<br>
     * If necessary, the module is loaded and initialized beforehand. Modules
     * are loaded and initialized globally. If a module is not yet loaded when
     * the method is called, this is done without specifying parameters with the
     * first request.<br>
     * If a module is to be initialized with parameters, the module must be
     * declared in section {@code INITIALIZE} or loaded via the MAPI-ClassLoader
     * from the Devwex-Module-SDK.
     * @param  module Modul
     * @param  invoke Method to be invoked
     * @return {@code true} if successfully loaded the module and called the
     *     method,otherwise {@code false}
     */
    private boolean invoke(String module, String invoke)
            throws Exception {

        if (invoke == null
                || invoke.trim().length() <= 0)
            return false;
        
        Object object = Service.load(Service.load(module), null);
        if (object == null)
            return false;

        String string = this.environment.get("module_opts");
        if (string.length() <= 0)
            string = null;
        
        // method for the module entry is determined and called
        Method method = object.getClass().getMethod(invoke, Object.class, String.class);
        method.invoke(object, this, string);

        return true;
    }

    /**
     * TODO:
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

        // der Pfad wird getrimmt, in Kleinbuchstaben umformatiert, die
        // Backslashs gegen Slashs getauscht
        path = path.replace('\\', '/').trim();

        // fuer die Ermittlung wird der Pfad mit / abgeschlossen um auch
        // Referenzen wie z.B. /a mit /a/ ermitteln zu koennen
        locale = path.toLowerCase();

        if (!locale.endsWith("/"))
            locale = locale.concat("/");

        // initiale Einrichtung der Variablen
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

        // die Liste der Referenzen wird ermittelt
        enumeration = this.references.elements();

        // die virtuellen Verzeichnisse werden ermittelt
        while (enumeration.hasMoreElements()) {

            // die Regel wird ermittelt
            rules = ((String)enumeration.nextElement()).toLowerCase();
            rules = this.references.get(rules);

            // Alias, Ziel und Optionen werden ermittelt
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
            
            // die Optionen werden ermittelt
            string  = rules.toUpperCase();
            virtual = string.contains("[A]") || string.contains("[M]");
            connect = string.contains("[M]") || string.contains("[R]");
            
            // ungueltige bzw. unvollstaendige Regeln werden ignoriert
            //   - ohne Alias
            //   - Redirect / Modul ohne Ziel
            //   - Alias ohne Ziel und Optionen
            if (alias.length() <= 0
                    || (connect && target.length() <= 0)
                    || (rules.length() <= 0 && target.length() <= 0))
                continue;
            
            // ggf. wird der Alias um Slash erweitert, damit spaeter DOCROOT
            // und Alias einen plausiblen Pfad ergeben
            if (!alias.startsWith("/"))
                alias = ("/").concat(alias);            
            
            // ggf. wird der Alias als Target uebernommen, wenn kein Target
            // angegeben wurde, z.B. wenn fuer einen realen Pfad nur Optionen
            // festgelegt werden
            if (target.length() <= 0)
                target = this.docroot.concat(alias);

            // das Ziel wird mit / abgeschlossen um auch Referenzen zwischen /a
            // und /a/ ermitteln zu koennen
            buffer = alias;
            if (!virtual && !buffer.endsWith("/") && buffer.length() > 0)
                buffer = buffer.concat("/");
            
            // die qualifizierteste laengste Refrerenz wird ermittelt
            if (locale.startsWith(buffer.toLowerCase())
                    && buffer.length() > 0 && reference.length() <= buffer.length()) {

                // optional wird die Sperrung des Verzeichnis ermittelt
                forbidden = string.contains("[C]");

                if (!connect) {

                    // die Zieldatei wird eingerichtet
                    // der absolute Pfad wird ermittelt
                    // Verzeichnisse werden ggf. mit Slash beendet
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

            // der Zielpfad wird mit / abgeschlossen um auch Referenzen
            // zwischen /a und /a/ ermitteln zu koennen
            buffer = alias;
            if (!absolute
                    && !module
                    && !buffer.endsWith("/")
                    && buffer.length() > 0)
                buffer = buffer.concat("/");

            // die ACC-Eintraege zur Authentifizierung werden zusammengesammelt
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

        // die Referenz wird alternativ ermittelt
        if (reference.length() <= 0) reference = path;

        // konnte eine Referenz ermittelt werden wird diese geparst
        if (reference.length() > 0) {

            // die Location wird ermittelt und gesetzt
            result = absolute || module ? location : location.concat(path.substring(Math.min(path.length(), reference.length())));

            // die Option [A] und [M] wird fuer absolute Referenzen geprueft
            if (absolute || module) {

                string = path.substring(0, Math.min(path.length(), reference.length()));

                this.environment.set("script_name", string);
                this.environment.set("path_context", string);
                this.environment.set("path_info", path.substring(string.length()));
            }

            // die Moduldefinition wird als Umgebungsvariablen gesetzt
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
     * Verifies the access permissions for a reference and sets the appropriate
     * status if necessary.
     * @param  reference Reference
     * @throws Exception
     *     In case of unexpected errors
     */
    private void authorize(String reference)
            throws Exception {
        
        String string = reference.toLowerCase();
        if (!string.contains("[acc:")
                || this.status >= 500)
            return;
        
        // optionally the realm caption is determined
        // and is added to the request header for other processes
        String realm = reference.replaceAll("^(.*(\\[\\s*(?i)realm:([^\\[\\]]*?)\\s*\\]).*)|.*$", "$3").replace("\"", "\\\"");
        this.fields.set("auth_realm", realm);
        
        // authentication method is determined
        // and is added to the request header for other processes
        boolean digest = string.contains("[d]");
        this.fields.set("auth_type", digest ? "Digest" : "Basic");
        
        // die Werte der ACC-Optionen werden ermittelt
        string = string.replaceAll("\\[acc:([^\\[\\]]*?)\\]", "\00$1\01");
        string = string.replaceAll("((((^|\01).*?)\00)|(\01.*$))|(^.*$)", " ").trim();
        
        String access = "";

        // ACC entries (groups) are collected
        // with the option [ACC:NONE] the authorization is cancelled
        boolean authorize = false;
        StringTokenizer tokenizer = new StringTokenizer(string);
        while (tokenizer.hasMoreTokens()) {
            string = tokenizer.nextToken();
            if (string.equals("none"))
                return;
            authorize = true;
            access = access.concat(" ").concat(this.access.get(string));
        }
        
        access = access.trim();
        if (access.length() <= 0) {
            if (authorize)
                this.status = 401;
            return;
        }
        
        string = this.fields.get("http_authorization");
        if (string.toLowerCase().startsWith("digest ")
                && digest) {
            
            string = string.replaceAll("(\\w+)\\s*=\\s*(?:(?:\"(.*?)\")|([^,]*))", "\00$1=$2$3\n");
            string = string.replaceAll("[^\n]+\00", "");
            Section section = Section.parse(string, true);
            
            String response = section.get("response");
            String username = section.get("username");

            string = Worker.textHash(this.environment.get("request_method").concat(":").concat(section.get("uri")));
            string = (":").concat(section.get("nonce")).concat(":").concat(section.get("nc")).concat(":").concat(section.get("cnonce")).concat(":").concat(section.get("qop")).concat(":").concat(string);

            tokenizer = new StringTokenizer(access);
            while (tokenizer.hasMoreTokens()) {
                access = tokenizer.nextToken();
                if (username.equals(access))
                    access = access.substring(username.length());
                else if (access.startsWith(username.concat(":")))
                    access = access.substring(username.length() +1);
                else continue;
                
                access = Worker.textHash(username.concat(":").concat(realm).concat(":").concat(access));
                access = Worker.textHash(access.concat(string));
                if (response.equals(access)) {
                    this.fields.set("auth_user", username);
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
     * Verifies the filters and applies them if necessary.
     * Filters have no direct return value, they affect server status and data
     * flow control, among other things.
     * @return a target if the filter refers to one as a result
     */
    private String filter()
            throws Exception {

        if (this.status == 400
                || this.status >= 500)
            return this.resource;
        
        String resource = this.resource;

        // FILTER the filters are in a vector
        // the processing is done by REFERENCE, SCRIPT_URI and CODE
        Enumeration enumeration = this.filters.elements();
        while (enumeration.hasMoreElements()) {

            String filter = this.filters.get((String)enumeration.nextElement());
            int cursor = filter.indexOf('>');
            String reference = cursor >= 0 ? filter.substring(cursor +1).trim() : "";
            if (cursor >= 0)
                filter = filter.substring(0, cursor);
            
            // The filters are evaluated according to the exclusion principle.
            // For this purpose, all rules of a line are checked individually in
            // a loop and the loop is exited with the first rule that does not
            // apply. Thus, the end of the loop is reached only if none of the
            // conditions has failed and thus all are true.
            
            StringTokenizer rules = new StringTokenizer(filter, "[+]");
            while (rules.hasMoreTokens()) {

                // filter description is determined
                // method and condition must be set
                // with tolerance for [+] when concatenating empty conditions
                String rule = rules.nextToken().toLowerCase().trim();
                StringTokenizer words = new StringTokenizer(rule);
                if (words.countTokens() < 2)
                    continue;

                // method is determined
                // method must match the HTTP method
                String method = words.nextToken();
                if (method.length() > 0
                        && !method.equals("all")
                        && !method.equals(this.environment.get("request_method").toLowerCase()))
                    break;

                boolean condition;
                
                // logical condition is determined
                // the pseudo-condition ALWAYS always matches
                String logical = words.nextToken();
                if (!logical.equals("always")) {
                    
                    // filter control is set, only IS and NOT are allowed,
                    // other values are not allowed.
                    if (!logical.equals("is")
                            && !logical.equals("not"))
                        break;

                    condition = logical.equals("is");
                    
                    // method and condition must be set
                    if (words.countTokens() < 2)
                        break;

                    // function and parameters are determined
                    String function = words.nextToken();
                    
                    // variable to be verified is determined
                    // and their value to be verified is determined
                    String valueA = this.environment.get(words.nextToken()).toLowerCase();
                    String valueB = Worker.textDecode(valueA);
                    
                    // comparative value is determined
                    // and the comparison is done
                    String pattern = words.hasMoreTokens() ? words.nextToken() : "";
                    if ((function.equals("starts")
                            && condition != (valueA.startsWith(pattern)
                                || valueB.startsWith(pattern)))
                        || (function.equals("contains")
                                && condition != (valueA.contains(pattern)
                                        || valueB.contains(pattern)))
                        || (function.equals("equals")
                                && condition != (valueA.equals(pattern)
                                        || valueB.equals(pattern)))
                        || (function.equals("ends")
                                && condition != (valueA.endsWith(pattern)
                                        || valueB.endsWith(pattern)))
                        || (function.equals("match")
                                && condition != (valueA.matches(pattern)
                                        || valueB.matches(pattern)))
                        || (function.equals("empty")
                                && condition != (valueA.length() <= 0)))
                        break;
                
                    if (rules.hasMoreTokens())
                        continue;
                }
                    
                // target is determined
                // target is a reference without options, the pure target
                String target = Worker.cleanOptions(reference);
                
                // if a module has been defined, it is optionally called in the
                // background as a filter or process module, processing does not
                // end until the module changes the data flow control
                if (reference.toUpperCase().contains("[M]")
                        && target.length() > 0) {
                    condition = this.control;
                    int status = this.status;
                    this.environment.set("module_opts", reference);
                    this.invoke(target, "filter");
                    if (this.control != condition
                            || this.status != status)
                        return this.resource;
                    continue;
                }
                
                // in case of a redirect STATUS 302 is set
                if (reference.toUpperCase().contains("[R]")
                        && target.length() > 0) {
                    this.environment.set("script_uri", target);
                    this.status = 302;
                    return resource;
                }
                
                // references to files or directories are returned as a location
                if (target.length() > 0) {
                    File file = Worker.fileCanonical(new File(reference));
                    if (file != null
                            && file.exists())
                        return file.getPath();
                }
                
                // if all conditions match and there is no special
                // reference/target, STATUS 403 is set
                this.status = 403;

                return resource;
            }
        }
        
        return resource;
    }
    
    /**
     * TODO:
     * Initialisiert die Connection, liest den Request, analysiert diesen und
     * richtet die Connection in der Laufzeitumgebung entsprechen ein.
     * @throws Exception
     *     Im Fall nicht erwarteter Fehler
     */
    private void initiate()
            throws Exception {

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

        // der Datenpuffer wird zum Auslesen vom Header eingerichtet
        buffer = new ByteArrayOutputStream(65535);
        
        if ((this.accept instanceof SSLSocket))
            try {this.fields.set("auth_cert", ((SSLSocket)this.accept).getSession().getPeerPrincipal().getName());
            } catch (Throwable throwable) {
            }
        
        try {

            // das SO-Timeout wird fuer den ServerSocket gesetzt
            this.accept.setSoTimeout((int)this.timeout);

            // die Datenstroeme werden eingerichtet
            this.output = this.accept.getOutputStream();
            this.input  = this.accept.getInputStream();

            // der Inputstream wird gepuffert
            this.input = new BufferedInputStream(this.input, this.blocksize);
            
            count = cursor = offset = 0;
            
            // der Header vom Requests wird gelesen, tritt beim Zugriff auf die
            // Datenstroeme ein Fehler auf, wird STATUS 400 gesetzt
            while (true) {

                if ((digit = this.input.read()) >= 0) {

                    // der Request wird auf kompletten Header geprueft
                    cursor = (digit == ((cursor % 2) == 0 ? 13 : 10)) ? cursor +1 : 0;

                    if (cursor > 0 && count > 0 && offset > 0 && buffer.size() > 0) {

                        string = new String(buffer.toByteArray(), offset, buffer.size() -offset);

                        offset = string.indexOf(':');
                        shadow = string.substring(offset < 0 ? string.length() : offset +1).trim();
                        string = string.substring(0, offset < 0 ? string.length() : offset).trim();

                        // entsprechend RFC 3875 (CGI/1.1-Spezifikation) werden
                        // alle Felder vom HTTP-Header als HTTP-Parameter zur
                        // Verfuegung gestellt, dazu wird das Zeichen - durch _
                        // ersetzt und allen Parametern das Praefix "http_"
                        // vorangestellt
                        
                        string = ("http_").concat(string.replace('-', '_'));
                        if (!this.fields.contains(string)
                                && string.length() > 0
                                && shadow.length() > 0)
                            this.fields.set(string, shadow);

                        offset = buffer.size();
                    }

                    // die Feldlaenge wird berechnet
                    count = cursor > 0 ? 0 : count +1;

                    if (count == 1) offset = buffer.size();

                    // die Zeile eines Felds vom Header muss sich mit 8-Bit
                    // addressieren lassen (fehlende Regelung im RFC 1945/2616)
                    if (count > 32768) this.status = 413;

                    // die Daten werden gespeichert
                    buffer.write(digit);

                    // der Header des Request wird auf 65535 Bytes begrenzt
                    if (buffer.size() >= 65535
                            && cursor < 4) {
                        this.status = 413;
                        break;
                    }
                    
                    // der Request wird auf kompletter Header geprueft
                    if (cursor == 4)
                        break;

                } else {

                    // der Datenstrom wird auf Ende geprueft
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
        
        // der Header wird vorrangig fuer die Schnittstellen gesetzt
        this.header = buffer.toString().trim();

        // die zusaetzlichen Header-Felder werden vorrangig fuer Filter
        // ermittelt und sind nicht Bestandteil vom (X)CGI, dafuer werden nur
        // die relevanten Parameter wie Methode, Pfade und Query uebernommen

        // die erste Zeile wird ermittelt
        tokenizer = new StringTokenizer(this.header, "\r\n");

        string = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";

        this.fields.set("req_line", string);

        // die Methode vom Request wird ermittelt
        offset = string.indexOf(' ');
        shadow = string.substring(0, offset < 0 ? string.length() : offset);
        string = string.substring(offset < 0 ? string.length() : offset +1);

        this.fields.set("req_method", shadow);

        // ohne HTTP-Methode ist die Anfrage ungueltig, somit STATUS 400
        if (this.status == 0
                && shadow.length() <= 0)
            this.status = 400;

        // Protokoll und Version vom Request werden ermittelt aber ignoriert
        offset = string.lastIndexOf(' ');
        string = string.substring(0, offset < 0 ? string.length() : offset);

        // Pfad und Query vom Request werden ermittelt
        offset = string.indexOf(' ');
        string = string.substring(0, offset < 0 ? string.length() : offset);

        this.fields.set("req_path", string);

        // Pfad und Query vom Request werden ermittelt
        offset = string.indexOf('?');
        shadow = string.substring(offset < 0 ? string.length() : offset +1);
        string = string.substring(0, offset < 0 ? string.length() : offset);

        this.fields.set("req_query", shadow);
        this.fields.set("req_uri", string);

        // der Pfad wird dekodiert
        shadow = Worker.textDecode(string);
        
        // der Pfad wird ausgeglichen /abc/./def/../ghi/ -> /abc/ghi
        string = Worker.fileNormalize(shadow);
        if (shadow.endsWith("/") && !string.endsWith("/"))
            string = string.concat("/");

        this.fields.set("req_path", string);

        // ist der Request nicht korrekt wird STATUS 400 gesetzt
        // enthaelt der Request keinen Header wird STATUS 400 gesetzt
        // enthaelt der Request kein gueltige Pfadangabe wird STATUS 400 gesetzt
        if (this.status == 0
                && (!string.startsWith("/")
                        || this.header.length() <= 0))
            this.status = 400;
        
        // der Host wird ohne Port ermittelt und verwendet
        string = this.fields.get("http_host");
        offset = string.indexOf(':');
        string = string.substring(0, offset < 0 ? string.length() : offset);

        while (string.endsWith("."))
            string = string.substring(0, string.length() -1);

        // ist kein Host im Request, wird die aktuelle Adresse verwendet
        if (string.length() <= 0)
            string = this.accept.getLocalAddress().getHostAddress();

        this.fields.set("http_host", string);

        // der Host wird zur Virtualisierung ermittelt
        if (string.length() > 0) {

            string  = ("virtual:").concat(string);
            section = this.initialize.get(string.concat(":ini"));
            shadow  = section.get("server").toLowerCase();

            // die Optionen werden mit allen Vererbungen ermittelt bzw.
            // erweitert wenn ein virtueller Host fuer den Server existiert
            if ((" ").concat(shadow).concat(" ").contains((" ").concat(this.context.toLowerCase()).concat(" "))
                    || shadow.length() <= 0) {
                this.options.merge(section);
                this.references.merge(this.initialize.get(string.concat(":ref")));
                this.access.merge(this.initialize.get(string.concat(":acc")));
                this.filters.merge(this.initialize.get(string.concat(":flt")));
                this.environment.merge(this.initialize.get(string.concat(":env")));
                this.interfaces.merge(this.initialize.get(string.concat(":cgi")));
            }
            
            // die zu verwendende Blockgroesse wird ermittelt
            try {this.blocksize = Integer.parseInt(this.options.get("blocksize"));
            } catch (Throwable throwable) {
                this.blocksize = 65535;
            }

            if (this.blocksize <= 0)
                this.blocksize = 65535;

            string = this.options.get("timeout");

            this.isolation = string.toUpperCase().contains("[S]") ? -1 : 0;

            // das Timeout der Connection wird ermittelt
            try {this.timeout = Long.parseLong(Worker.cleanOptions(string));
            } catch (Throwable throwable) {
                this.timeout = 0;
            }
        }

        // das aktuelle Arbeitsverzeichnis wird ermittelt
        file   = Worker.fileCanonical(new File("."));
        string = file != null ? file.getPath().replace('\\', '/') : ".";
        if (string.endsWith("/"))
            string = string.substring(0, string.length() -1);

        // das Systemverzeichnis wird ermittelt
        file = Worker.fileCanonical(new File(this.options.get("sysroot")));
        this.sysroot = file != null ? file.getPath().replace('\\', '/') : string;
        if (this.sysroot.endsWith("/"))
            this.sysroot = this.sysroot.substring(0, this.sysroot.length() -1);
        if (this.options.get("sysroot").length() <= 0)
            this.sysroot = string;

        // das Dokumentenverzeichnis wird ermittelt
        file = Worker.fileCanonical(new File(this.options.get("docroot")));
        this.docroot = file != null ? file.getPath().replace('\\', '/') : string;
        if (this.docroot.endsWith("/"))
            this.docroot = this.docroot.substring(0, this.docroot.length() -1);
        if (this.options.get("docroot").length() <= 0)
            this.docroot = string;

        // die serverseitig festen Umgebungsvariablen werden gesetzt
        this.environment.set("server_port", String.valueOf(this.accept.getLocalPort()));
        this.environment.set("server_protocol", "HTTP/1.0");
        this.environment.set("server_software", "Seanox-Devwex/#[ant:release-version] #[ant:release-date]");

        this.environment.set("document_root", this.docroot);

        // die Requestspezifischen Umgebungsvariablen werden gesetzt
        this.environment.set("content_length", this.fields.get("http_content_length"));
        this.environment.set("content_type", this.fields.get("http_content_type"));
        this.environment.set("query_string", this.fields.get("req_query"));
        this.environment.set("request", this.fields.get("req_line"));
        this.environment.set("request_method", this.fields.get("req_method"));
        this.environment.set("remote_addr", this.accept.getInetAddress().getHostAddress());
        this.environment.set("remote_port", String.valueOf(this.accept.getPort()));

        // die Unique-Id wird aus dem HashCode des Sockets, den Millisekunden
        // sowie der verwendeten Portnummer ermittelt, die Laenge ist variabel
        string = Long.toString(Math.abs(this.accept.hashCode()), 36);
        string = string.concat(Long.toString(((Math.abs(System.currentTimeMillis()) *100000) +this.accept.getPort()), 36));

        // die eindeutige Request-Id wird gesetzt
        this.environment.set("unique_id", string.toUpperCase());

        // der Path wird ermittelt
        shadow = this.fields.get("req_path");

        // die Umgebungsvariabeln werden entsprechend der Ressource gesetzt
        this.environment.set("path_url", shadow);
        this.environment.set("script_name", shadow);
        this.environment.set("script_url", this.fields.get("req_uri"));
        this.environment.set("path_context", "");
        this.environment.set("path_info", "");

        // REFERENCE - Zur Ressource werden ggf. der virtuellen Pfad im Unix
        // Fileformat (Slash) bzw. der reale Pfad, sowie optionale Parameter,
        // Optionen und Verweise auf eine Authentication ermittelt.
        this.resource = this.locate(shadow);

        // Option [A] fuer eine absolute Referenz wird ermittelt
        string = this.resource.toUpperCase();

        digit  = string.contains("[A]") ?  1 : 0;
        digit |= string.contains("[M]") ?  2 : 0;
        digit |= string.contains("[R]") ?  4 : 0;
        digit |= string.contains("[C]") ?  8 : 0;
        digit |= string.contains("[X]") ? 16 : 0;
        
        virtual = (digit & 1) != 0;
        connect = (digit & (2|4)) != 0;

        // HINWEIS - Auch Module koennen die Option [R] besitzen, diese wird
        // dann aber ignoriert, da die Weiterleitung zu Modulen nur ueber deren
        // virtuellen Pfad erfolgt

        if ((this.status == 0 || this.status == 404) && (digit & 8) != 0) this.status = 403;
        if ((this.status == 0 || this.status == 404) && (digit & 2) != 0) this.status = 0;
        if ((this.status == 0 || this.status == 404) && (digit & (2|4)) == 4) {
            this.environment.set("script_uri", Worker.cleanOptions(this.resource));
            this.status = 302;
        }
        
        // ggf. wird die Zugriffsbrechtigung geprueft
        // unterstuetzt werden Basic- und Digest-Authentication
        this.authorize(this.resource);
        
        // die Request-Parameter (nur mit dem Praefix http_ und auth_) werden in
        // die Umgebungsvariablen uebernommen
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

        // die Ressource wird als File eingerichtet
        file = new File(this.resource);
        
        // die Ressource wird syntaktisch geprueft, nicht real existierende
        // Ressourcen sowie Abweichungen im  kanonisch Pfad werden mit Status
        // 404 quitiert (das schliesst die Bugs im Windows-Dateisystem / / und
        // Punkt vorm Slash ein), die Gross- und Kleinschreibung wird in Windows
        // ignoriert
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

        // der HOST oder VIRTUAL HOST wird ermittelt
        entry = this.fields.get("http_host");
        if (entry.length() <= 0)
            entry = this.accept.getLocalAddress().getHostAddress();

        // die OPTION IDENTITY wird geprueft
        if (this.options.get("identity").toLowerCase().equals("on"))
            this.environment.set("server_name", entry);

        // aus dem Schema wird die Verwendung vom Secure-Layer ermittelt
        secure = this.socket instanceof javax.net.ssl.SSLServerSocket;
        
        // die Location wird zusammengestellt
        string = this.environment.get("server_port");
        string = (!string.equals("80") && !secure || !string.equals("443") && secure) && string.length() > 0 ? (":").concat(string) : "";
        string = (secure ? "https" : "http").concat("://").concat(entry).concat(string);

        // die URI vom Skript wird komplementiert
        if (this.status != 302)
            this.environment.set("script_uri", string.concat(this.fields.get("req_path")));

        // bei abweichendem Path wird die Location als Redirect eingerichtet
        if (this.status == 0 && !this.environment.get("path_url").equals(shadow) && !virtual && !connect) {
            this.environment.set("script_uri", string.concat(shadow));
            this.status = 302;
        }

        // die aktuelle Referenz wird ermittelt
        string = this.environment.get("script_uri");
        string = Worker.fileNormalize(string);

        // bezieht sich die Referenz auf ein Verzeichnis und der URI endet
        // aber nicht auf "/" wird STATUS 302 gesetzt
        if (file.isDirectory() && !string.endsWith("/")) {
            this.environment.set("script_uri", string.concat("/"));
            if (this.status == 0)
                this.status = 302;
        }
        
        // DEFAULT, beim Aufruf von Verzeichnissen wird nach einer alternativ
        // anzuzeigenden Datei gesucht, was intern wie ein Verweis funktioniert
        if (file.isDirectory() && this.status == 0) {

            // das Verzeichnis wird mit Slash abgeschlossen
            if (!this.resource.endsWith("/"))
                this.resource = this.resource.concat("/");

            // die Defaultdateien werden ermittelt
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

        // der Query String wird ermittelt
        string = this.environment.get("query_string");

        // der Query String wird die Request URI aufbereite
        if (string.length() > 0) string = ("?").concat(string);

        // die Request URI wird gesetzt
        this.environment.set("request_uri", this.fields.get("req_uri").concat(string));

        // die aktuelle HTTP-Methode wird ermittelt
        string = this.environment.get("request_method").toLowerCase();
        
        // METHODS, die zulaessigen Methoden werden ermittelt        
        shadow = (" ").concat(this.options.get("methods").toLowerCase()).concat(" ");

        // die aktuelle Methode wird in der Liste der zulaessigen gesucht, ist
        // nicht enthalten, wird STATUS 405 gesetzt, ausgenommen sind Module
        // mit der Option [X], da an diese alle Methoden weitergereicht werden
        if (((digit & (2 | 16)) != (2 | 16))
                && !shadow.contains((" ").concat(string).concat(" "))
                && this.status <= 0)
            this.status = 405;
        
        this.resource = this.filter();

        string = Worker.cleanOptions(this.resource);

        this.environment.set("script_filename", string);
        this.environment.set("path_translated", string);

        // handelt es sich bei der Ressource um ein Modul wird keine Zuweisung
        // fuer das (X)CGI und den Mediatype vorgenommen
        if (connect || this.resource.toUpperCase().contains("[M]")) {

            this.mediatype = this.options.get("mediatype");
            this.gateway   = this.resource;

            return;
        }
        
        if (this.status == 302) 
            return;

        // die Dateierweiterung wird ermittelt
        cursor = this.resource.lastIndexOf(".");
        entry  = cursor >= 0 ? this.resource.substring(cursor +1) : this.resource;

        // CGI - zur Dateierweiterung wird ggf. eine Anwendung ermittelt
        this.gateway = this.interfaces.get(entry);

        if (this.gateway.length() > 0) {

            cursor = this.gateway.indexOf('>');

            // die zulaessigen Methoden werden ermittelt
            method = this.gateway.substring(0, Math.max(0, cursor)).toLowerCase().trim();

            // die eigentliche Anwendung ermittelt
            if (cursor >= 0) this.gateway = this.gateway.substring(cursor +2).trim();

            // die Variable GATEWAY-INTERFACE wird fuer das (X)CGI gesetzt
            if (this.gateway.length() > 0) {

                this.environment.set("gateway_interface", "CGI/1.1");

                // die Methode wird geprueft, ob diese fuer das CGI zugelassen
                // ist, wenn nicht zulaessig, wird STATUS 405 gesetzt
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
        
        // der Mediatype wird ermittelt
        this.mediatype = this.mediatypes.get(entry);

        // kann dieser nicht festgelegt werden wird der Standardeintrag aus den
        // Server Basisoptionen eingetragen
        if (this.mediatype.length() <= 0) this.mediatype = this.options.get("mediatype");

        // die vom Client unterstuetzten Mediatypes werden ermittelt
        shadow = this.fields.get("http_accept");
        
        if (shadow.length() > 0) {

            // es wird geprueft ob der Client den Mediatype unterstuetzt,
            // ist dies nicht der Fall, wird STATUS 406 gesetzt
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
     * Creates the base header for a response and adds passed parameters.
     * @param  status HTTP-Status
     * @param  header optional list with parameters
     * @return the created response header
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
     * Creates an array with the environment variables.
     * Environment variables without value are ignored.
     * @return the environment variables as array
     */
    private String[] getEnvironment() {
        
        ArrayList list = new ArrayList();
        
        // environment variables are determined and applied
        Enumeration enumeration = this.environment.elements();
        while (enumeration.hasMoreElements()) {
            String label = (String)enumeration.nextElement();
            String value = this.environment.get(label);
            if (value.length() <= 0)
                continue;
            value = label.concat("=").concat(value);
            if (list.contains(value))
                continue;
            list.add(value);
        }
        
        // lines from the header are determined
        // the first line with the request is discarded
        StringTokenizer tokenizer = new StringTokenizer(this.header, "\r\n");
        if (tokenizer.hasMoreTokens())
            tokenizer.nextToken();
        
        while (tokenizer.hasMoreTokens()) {
            String value = tokenizer.nextToken();
            int index = value.indexOf(':');
            if (index <= 0)
                continue;
            String label = value.substring(0, index).trim();
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
    
    private void doGateway()
            throws Exception {
        
        // modules can also be defined as a target with the gateway
        // module definition is set as environment variables
        if (this.gateway.toUpperCase().contains("[M]")) {
            this.environment.set("module_opts", this.gateway);
            this.invoke(Worker.cleanOptions(this.gateway), "service");
            return;
        }
        
        String target = this.gateway;
        
        String script = this.environment.get("script_filename");
        script = script.replace('\\', File.separatorChar);
        script = script.replace('/', File.separatorChar);

        target = target.replace("[c]", "[C]");
        target = target.replace("[C]", script);

        String directory = script.replaceAll("[^\\\\/]+$", "");
        target = target.replace("[d]", "[D]");
        target = target.replace("[D]", directory);

        String name = script.replaceAll("(^.*[\\\\/])|(\\..*$)", "");
        target = target.replace("[n]", "[N]");
        target = target.replace("[N]", name);
        
        target = Worker.cleanOptions(target);
        
        // maximum process run time is determined
        long duration = 0;
        try {duration = Long.parseLong(this.options.get("duration"));
        } catch (Throwable throwable) {
        }

        // the time limit of the process is determined
        long timeout = 0;
        if (duration > 0)
            timeout = System.currentTimeMillis() +duration;

        // environment variables are determined
        String[] environment = this.getEnvironment();
        
        // process is started
        // data is read until the process is terminated or an error occurs
        Process process = Runtime.getRuntime().exec(target.trim(), environment);

        try {

            // data streams are established
            // data stream of StdErr is opened only when needed
            InputStream input = process.getInputStream();
            OutputStream output = process.getOutputStream();

            // data buffer is established
            byte[] bytes = new byte[this.blocksize];

            // the XCGI header is composed from the CGI environment variables
            // and written to the StdIO in a similar way to the HTTP header
            if (this.gateway.toUpperCase().contains("[X]"))
                output.write(String.join("\r\n", environment).trim().concat("\r\n\r\n").getBytes());
            
            // length of the content is determined
            int length = 0;
            try {length = Integer.parseInt(this.fields.get("http_content_length"));
            } catch (Throwable throwable) {
            }
            
            // data is read from the socket data stream and written to the CGI
            // the amount of data is limited by CONTENT-LENGHT
            while (length > 0) {

                long size = this.input.read(bytes);
                if (size < 0)
                    break;
                if (size > length)
                    size = length;
                output.write(bytes, 0, (int)size);

                // remaining amount of data is calculated
                length -= size;
                
                // maximum process run time is verified
                if (timeout > 0
                        && timeout < System.currentTimeMillis()) {
                    this.status = 504;
                    break;
                }

                Thread.sleep(this.interrupt);
            }        

            // data stream is closed
            if (process.isAlive())
                output.close();
            
            // if the status is not 200, the termination of the process is
            // forced via the final block, in order to also terminate the
            // processes whose processing was faulty
            if (this.status != 200)
                return;
            
            // data buffer for the header is established
            String header = "";
            
            // response data is read until the end of the application and the
            // data flow control is initially set for this purpose
            while (true) {
                
                // termination of the connection is verified
                try {this.accept.getSoTimeout();
                } catch (Throwable throwable) {
                    this.status = 503;
                    break;
                }
                
                if (input.available() > 0) {
                
                    // data is read from the StdIO
                    length = input.read(bytes);
                    int offset = 0;
                    
                    // for the analysis of the header only the first line of the
                    // CGI output is analyzed, the header itself is not verified
                    // or manipulated by the server, only the first line is
                    // prepared HTTP-conform.     
                    if (this.control
                            && header != null) {
                        
                        header = header.concat(new String(bytes, 0, length));
                        int cursor = header.indexOf("\r\n\r\n");
                        if (cursor >= 0) {
                            offset = length -(header.length() -cursor -4); 
                            header = header.substring(0, cursor);
                        } else offset = header.length();
                        
                        // buffer for header analysis is limited to 65535 bytes,
                        // exceeding causes STATUS 502  
                        if (header.length() > 65535) {
                            
                            this.status = 502;
                            break;
                            
                        } else if (cursor >= 0) {
                            
                            header = header.trim();
                            cursor = header.indexOf("\r\n");

                            String buffer = cursor >= 0 ? header.substring(0, cursor).trim() : header;
                            String status = buffer.toUpperCase();
                            
                            if (status.startsWith("HTTP/")) {

                                if (status.matches("^HTTP/STATUS(\\s.*)*$"))
                                    header = null;
                                    
                                try {this.status = Math.abs(Integer.parseInt(buffer.replaceAll("^(\\S+)\\s*(\\S+)*\\s*(.*?)\\s*$", "$2")));
                                } catch (Throwable throwable) {
                                }
                                
                                // if necessary the status codes new codes and
                                // text from the CGI response are added
                                // temporarily so that all other worker
                                // functions work with the code from the CGI
                                // response. With the end of the connection
                                // (accept) the status codes are then discarded
                                buffer = buffer.replaceAll("^(\\S+)\\s*(\\S+)*\\s*(.*?)\\s*$", "$3");
                                if (buffer.length() > 0
                                        && !this.statuscodes.contains(String.valueOf(this.status)))
                                    this.statuscodes.set(String.valueOf(this.status), buffer);
                            }
                            
                            // if the response starts with HTTP/STATUS, the data
                            // stream is read but not forwarded to the client
                            // and the request is responded by the server, for
                            // which the data flow control is used
                            if (header != null) {
                                header = header.replaceAll("(?si)^ *HTTP */ *[^\r\n]*([\r\n]+|$)", "");
                                header = this.header(this.status, header.split("[\r\n]+"));
                                this.control = false;
                            }                                
                        }
                    }

                    // data is written only if the data flow control allows it
                    // and the response does not start with HTTP/STATUS
                    if (!this.control) {
                        
                        // Isolation defines the time from the last output
                        // when strict timeout is used to detect blocking data
                        // streams, because there is no socket-based timeout.
                        // If the stream is still reacting at the end, the time
                        // is reset.
                        if (this.isolation != 0)
                            this.isolation = System.currentTimeMillis();
                        if (header != null)
                            this.output.write(header.concat("\r\n\r\n").getBytes());
                        header = null;
                        this.output.write(bytes, offset, length -offset);
                        if (this.isolation != 0)
                            this.isolation = -1;
                        
                        // volume of sent data is registered
                        this.volume += length -offset;
                    }
                }

                // maximum process run time is verified
                if (timeout > 0
                        && timeout < System.currentTimeMillis()) {
                    this.status = 504;
                    break;
                 }
                
                // data stream is verified for present data
                // and the process is verified for its end
                if (input.available() <= 0
                        && !process.isAlive())
                    break;
                
                Thread.sleep(this.interrupt);
            }
            
        } finally {
            
            try {
                // data buffer is established
                byte[] bytes = new byte[this.blocksize];

                String message = "";
                InputStream error = process.getErrorStream();
                while (error.available() > 0) {
                    int length = error.read(bytes);
                    message = message.concat(new String(bytes, 0, length));
                }
                message = message.trim();
                if (message.length() > 0)
                    Service.print(("GATEWAY ").concat(message));
                
            } finally {
                
                // the termination of the process is forced, in order to also
                // terminate the processes whose processing was faulty
                try {process.destroy();
                } catch (Throwable throwable) {
                }
            }
        }
    }
    
    /**
     * Creates a navigable HTML page for a requested directory based on the
     * {@code index.html} template.
     * @param  directory Directory
     * @param  query     Sorting options
     * @return the navigable HTML to the directory
     */
    private byte[] createDirectoryIndex(File directory, String query) {
        
        Hashtable values = new Hashtable();
        
        // header and environment variables are merged,
        // server side variables have the higher priority
        Enumeration enumeration = this.environment.elements();
        while (enumeration.hasMoreElements()) {
            String entry = (String)enumeration.nextElement();
            if (entry.toLowerCase().equals("path")
                    || entry.toLowerCase().equals("file"))
                continue;
            values.put(entry, this.environment.get(entry));
        }
        
        // assignment of the fields for sorting is defined
        if (query.length() <= 0)
            query = "n";
        query = query.substring(0, 1);
        
        char order = query.charAt(0);
        boolean reverse = order >= 'A' && order <= 'Z';
        
        query = query.toLowerCase(); 
        order = query.charAt(0);
         
        // case, name, date, size, type
        int[] assign = new int[] {0, 1, 2, 3, 4};
        
        // sorting is determined by the query and works according to: case,
        // query and name, the entries are put into an array to achieve a simple
        // and flexible assignment of the sort order
        // 0 - case, 1 - name, 3 - date, 4 - size, 5 - type
        if (order == 'd') {
            // case, date, name, size, type
            assign = new int[] {0, 2, 1, 3, 4};
        } else if (order == 's') {
            // case, size, name, date, type
            assign = new int[] {0, 3, 1, 2, 4};
        } else if (order == 't') {
            // case, type, name, date, size
            assign = new int[] {0, 4, 1, 2, 3};
        } else query = "n";
        
        // default template for the INDEX is determined and loaded
        File file = new File(this.sysroot.concat("/index.html"));
        Generator generator = Generator.parse(Worker.fileRead(file));
        
        String path = this.environment.get("path_url");
        if (!path.endsWith("/"))
            path = path.concat("/");
        values.put("path_url", path);
        
        // path is generated as breadcrumb navigation.
        // each subpath is generated as a clickable path.
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        for (String chain = "", entry; tokenizer.hasMoreTokens();) {
            entry = tokenizer.nextToken();
            chain = chain.concat("/").concat(entry);
            values.put("path", chain);
            values.put("name", entry);
            generator.set("location", values);
        }
        
        // file list is determined
        File[] files = directory.listFiles();
        if (files == null)
            files = new File[0];
        ArrayList storage = new ArrayList(Arrays.asList(files));

        // with the option [S] hidden files are not displayed
        boolean hidden = this.options.get("index").toUpperCase().contains("[S]");

        // file information is assembled
        String[] columns = new String[5];
        for (int loop = 0, cursor; loop < storage.size(); loop++) {

            // physical file is determined
            file = (File)storage.get(loop);
            
            // the entries are put into an array to achieve a simple and
            // flexible assignment of the sort order
            // 0 - base, 1 - name, 2 - date, 3 - size, 4 - type

            // data type is determined
            columns[0] = file.isDirectory() ? "directory" : "file";
            
            // name is determined
            columns[1] = file.getName();
            
            // timestamp of the last change is determined
            columns[2] = String.format("%tF %<tT", new Date(file.lastModified()));

            // size is determined, but not for directories
            columns[3] = file.isDirectory() ? "-" : String.valueOf(file.length());
            
            // size is extended at the first position with the character which
            // results from the length of the size to sort it by numerical size
            columns[3] = String.valueOf((char)columns[3].length()).concat(columns[3]);

            // file type is determined, but not for directories
            cursor = columns[1].lastIndexOf(".");
            columns[4] = cursor >= 0 ? columns[1].substring(cursor +1) : "";
            columns[4] = file.isDirectory() ? "-" : columns[4].toLowerCase().trim();
            
            // files and directories of the "hidden" option are marked
            String row = String.join("\00 ", new String[] {columns[assign[0]], columns[assign[1]],
                    columns[assign[2]], columns[assign[3]], columns[assign[4]]});
            if (hidden && file.isHidden())
                row = "";
            storage.set(loop, row);
        }

        // file list is sorted
        Collections.sort(storage, String.CASE_INSENSITIVE_ORDER);
        if (reverse)
            Collections.reverse(storage);
        
        // file information is collected
        ArrayList list = new ArrayList();
        for (int loop = 0; loop < storage.size(); loop++) {
            
            // unrecognized files are suppressed
            tokenizer = new StringTokenizer((String)storage.get(loop), "\00");
            if (tokenizer.countTokens() <= 0)
                continue;
            
            Hashtable data = new Hashtable(values);
            list.add(data);
            
            // the entries are put into an array to achieve a simple and
            // flexible assignment of the sort order
            // 0 - base, 1 - name, 2 - date, 3 - size, 4 - type

            columns[assign[0]] = tokenizer.nextToken();
            columns[assign[1]] = tokenizer.nextToken().substring(1);
            columns[assign[2]] = tokenizer.nextToken().substring(1);
            columns[assign[3]] = tokenizer.nextToken().substring(1);
            columns[assign[4]] = tokenizer.nextToken().substring(1);
            
            data.put("case", columns[0]);
            data.put("name", columns[1]);
            data.put("date", columns[2]);
            data.put("size", columns[3].substring(1));
            data.put("type", columns[4]);
            
            String mime = columns[4];
            if (!mime.equals("-")) {
                mime = this.mediatypes.get(mime);
                if (mime.length() <= 0)
                    mime = this.options.get("mediatype");
            } else mime = "";
            data.put("mime", mime);
        }
        
        query = query.concat(reverse ? "d" : "a");
        if (list.size() <= 0)
            query = query.concat(" x");
        values.put("sort", query);

        values.put("file", list);
        generator.set(values);

        return generator.extract();
    }

    private void doGet()
            throws Exception {

        // HTTP method is determined
        // because the logic is used for GET and HEAD
        String method = this.fields.get("req_method").toLowerCase();

        ArrayList headers = new ArrayList();
        
        // resource is established
        File file = new File(this.resource);
        
        if (file.isDirectory()) {

            // INDEX ON option is checked
            // for directories and the INDEX OFF STATUS 403 is set
            // otherwise a navigable HTML is generated for the directory
            if (Worker.cleanOptions(this.options.get("index")).toLowerCase().equals("on")) {
                headers.add(("Content-Type: ").concat(this.mediatypes.get("html")));
                byte[] bytes = new byte[0]; 
                if (method.equals("get")) {
                    bytes = this.createDirectoryIndex(file, this.environment.get("query_string"));
                    headers.add(("Content-Length: ").concat(String.valueOf(bytes.length)));
                }

                // header is composed for the output
                String header = this.header(this.status, (String[])headers.toArray(new String[0])).concat("\r\n\r\n");

                // connection is marked as used
                this.control = false;
                
                // Isolation defines the time from the last output when strict
                // timeout is used to detect blocking data streams, because
                // there is no socket-based timeout. If the stream is still
                // reacting at the end, the time is reset.
                if (this.isolation != 0)
                    this.isolation = System.currentTimeMillis();
                this.output.write(header.getBytes());
                this.output.write(bytes);
                if (this.isolation != 0)
                    this.isolation = -1;
                
                // volume of sent data is registered
                this.volume += bytes.length;
                
                return;
            } 
            
            this.status = 403;
            return;
        }       

        // if the client sends IF-(UN)MODIFIED-SINCE it is verified
        // if (un)valid STATUS 304/412 is set
        if (!Worker.fileIsModified(file, this.fields.get("http_if_modified_since"))) {
            this.status = 304;
            return;
        }
        if (this.fields.contains("http_if_unmodified_since")
                && Worker.fileIsModified(file, this.fields.get("http_if_unmodified_since"))) {
            this.status = 412;
            return;
        }

        long offset = 0;
        long size   = file.length();
        long limit  = size;

        // if necessary, the partial data range RANGE is determined
        if (this.fields.contains("http_range")
                && size > 0) {
            
            // possible headers for the range:
            //   Range: ...; bytes=500-999 ...
            //          ...; bytes=-999 ...
            //          ...; bytes=500- ...

            String range = this.fields.get("http_range").replace(';', '\n');
            range = Section.parse(range).get("bytes");
            if (range.matches("^(\\d+)*\\s*-\\s*(\\d+)*$")) {
                StringTokenizer tokenizer = new StringTokenizer(range, "-");
                try {
                    offset = Long.parseLong(tokenizer.nextToken().trim());
                    if (tokenizer.hasMoreTokens()) {
                        limit = Long.parseLong(tokenizer.nextToken().trim());
                    } else if (range.startsWith("-")) {
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
        
        // header is composed for the output
        headers.add(("Last-Modified: ").concat(Worker.dateFormat("E, dd MMM yyyy HH:mm:ss z", new Date(file.lastModified()), "GMT")));
        headers.add(("Content-Length: ").concat(String.valueOf(limit -offset)));
        headers.add(("Accept-Ranges: bytes"));
        
        // if available the content type is set
        if (this.mediatype.length() > 0)
            headers.add(("Content-Type: ").concat(this.mediatype));

        // if available the content type is set
        if (this.status == 206)
            headers.add(("Content-Range: bytes ").concat(String.valueOf(offset)).concat("-").concat(String.valueOf(limit -1)).concat("/").concat(String.valueOf(size)));
        
        // header is composed for the output
        String header = this.header(this.status, (String[])headers.toArray(new String[0])).concat("\r\n\r\n");
        
        // connection is marked as used
        this.control = false;

        // Isolation defines the time from the last output when strict timeout
        // is used to detect blocking data streams, because there is no
        // socket-based timeout. If the stream is still reacting at the end, the
        // time is reset.
        if (this.isolation != 0)
            this.isolation = System.currentTimeMillis();
        this.output.write(header.getBytes());
        if (this.isolation != 0)
            this.isolation = -1;
        
        if (method.equals("get")) {

            // data buffer is established
            byte[] bytes = new byte[this.blocksize];
            
            // data stream is established
            InputStream input = new FileInputStream(this.resource);
            
            try {

                // if necessary, the partial data range is applied
                input.skip(offset);

                while ((offset +this.volume < limit)
                        && ((size = input.read(bytes)) >= 0)) {

                    if (this.status == 206
                            && (offset +this.volume +size > limit))
                        size = limit -(offset +this.volume);

                    // Isolation defines the time from the last output when
                    // strict timeout is used to detect blocking data streams,
                    // because there is no socket-based timeout. If the stream
                    // is still reacting at the end, the time is reset.
                    if (this.isolation != 0)
                        this.isolation = System.currentTimeMillis();
                    this.output.write(bytes, 0, Math.max(0, (int)size));
                    if (this.isolation != 0)
                        this.isolation = -1;

                    // volume of sent data is registered
                    this.volume += size;
                }

            } finally {
                try {input.close();
                } catch (Throwable throwable) {
                }
            }
        }
    }

    private void doPut()
            throws Exception {
        
        // resource is determined
        File file = new File(this.resource);
        
        // without CONTENT-LENGTH directories, otherwise files are created
        if (!this.fields.contains("http_content_length")) {
            // directory structure is created
            // if successful STATUS 201 is set
            // in case of errors STATUS 424 is set
            if (!file.isDirectory()
                    && !file.mkdirs())
                this.status = 424;
            if (this.status == 200
                    || this.status == 404) {
                this.fields.set("req_location", this.environment.get("script_uri"));
                this.status = 201;
            }
            return;
        } 
        
        // ength of the content is determined
        // a valid content length is expected, without it STATUS 411 is set   
        long length;
        try {
            length = Long.parseLong(this.fields.get("http_content_length"));
            if (length < 0)
                throw new Throwable();
        } catch (Throwable throwable) {
            this.status = 411;
            return;
        }

        // if necessary existing files are removed
        file.delete();

        // data buffer is established
        byte[] bytes = new byte[this.blocksize];

        // data stream is established
        OutputStream output = new FileOutputStream(this.resource);

        try {

            // data is read from the data stream
            // CONTENT-LENGHT is not exceeded in the process
            for (long size; length > 0;) {
                if ((size = this.input.read(bytes)) < 0)
                    break;
                size = Math.min(size, length);
                output.write(bytes, 0, (int)size);
                length -= size;
                Thread.sleep(this.interrupt);
            }
            
            // if the file cannot be created or can only be created incompletely
            // due to timeout, STATUS 424 is set
            if (length > 0)
                this.status = 424;

        } finally {
            try {output.close();
            } catch (Throwable throwable) {
            }
        }
        
        // if everything is successful, STATUS 201 is set
        if (this.status == 200
                || this.status == 404) {
            this.fields.set("req_location", this.environment.get("script_uri"));
            this.status = 201;
        }
    }
    
    private void doDelete() {
        
        // requested resource is completely deleted,
        // if an error occurs STATUS 424 is set.
        if (Worker.fileDelete(new File(this.resource)))
            return;
        
        this.status = 424;
    }

    private void doStatus()
            throws Exception {
        
        String method = this.fields.get("req_method").toLowerCase();
        if (method.equals("options")
                && (this.status == 302 || this.status == 404))
            this.status = 200;

        if (this.status == 302) {

            // determination of the LOCATION for redirection 
            String string = this.environment.get("query_string");
            if (string.length() > 0)
                string = ("?").concat(string);
            string = this.environment.get("script_uri").concat(string);
            this.fields.set("req_location", string);

        } else {

            // determination of the template for the server status
            // three variants with decreasing qualification are supported
            String string = String.valueOf(this.status);
            this.resource = this.sysroot.concat("/status-").concat(string).concat(".html");
            if (!new File(this.resource).exists())
                this.resource = this.sysroot.concat("/status-").concat(string.substring(0, 1)).concat("xx.html");
            if (!new File(this.resource).exists())
                this.resource = this.sysroot.concat("/status.html");
        }
        
        ArrayList headers = new ArrayList();

        // determination of the type of authorization, if available 
        String authentication = this.fields.get("auth_type");
        if (this.status == 401
                && authentication.length() > 0) {
            String string = (" realm=\"").concat(this.fields.get("auth_realm")).concat("\"");
            if (authentication.equals("Digest")) {
                string = ("Digest").concat(string);
                string = string.concat(", qop=\"auth\"");
                authentication = this.environment.get("unique_id");
                string = string.concat(", nonce=\"").concat(authentication).concat("\"");
                authentication = Worker.textHash(authentication.concat(String.valueOf(authentication.hashCode())));
                string = string.concat(", opaque=\"").concat(authentication).concat("\"");
                string = string.concat(", algorithm=\"MD5\"");
            } else string = ("Basic").concat(string);
            headers.add(("WWW-Authenticate: ").concat(string));
        }

        if (this.fields.contains("req_location"))
            headers.add(("Location: ").concat(this.fields.get("req_location")));
        
        // The generator is established when it is not a HEAD or OPTION request
        // and the status code is not labeled with the option [H] (head only).
        String string = this.statuscodes.get(String.valueOf(this.status));
        Generator generator;
        if (method.equals("head")
                || method.equals("options")
                || string.toUpperCase().contains("[H]"))
            generator = Generator.parse(null);
        else generator = Generator.parse(Worker.fileRead(new File(this.resource)));
        
        // Header and environment variables are merged, server side variables
        // have the higher priority and override system environment variables.

        Hashtable values = new Hashtable();

        Enumeration fields = this.fields.elements();
        while (fields.hasMoreElements()) {
            string = (String)fields.nextElement();
            values.put(string, this.fields.get(string));
        }
        
        Enumeration environment = this.environment.elements();
        while (environment.hasMoreElements()) {
            string = (String)environment.nextElement();
            values.put(string, this.environment.get(string));
        }
        
        string = String.valueOf(this.status);
        values.put("HTTP_STATUS", string);
        values.put("HTTP_STATUS_TEXT", Worker.cleanOptions(this.statuscodes.get(string)));
        
        generator.set(values);
        byte[] bytes = generator.extract();

        // if necessary, CONTENT-TYPE / CONTENT-LENGTH is set in the header
        if (bytes.length > 0) {
            headers.add(("Content-Type: ").concat(this.mediatypes.get("html")));
            headers.add(("Content-Length: ").concat(String.valueOf(bytes.length)));
        }
        
        // available methods are collected
        string = String.join(", ", this.options.get("methods").split("\\s+"));
        if (string.length() > 0)
            headers.add(("Allow: ").concat(string));
        
        // header is composed for the output
        string = this.header(this.status, (String[])headers.toArray(new String[0])).concat("\r\n\r\n");

        // connection is marked as used
        this.control = false;         

        // Isolation defines the time from the last output when strict timeout
        // is used to detect blocking data streams, because there is no
        // socket-based timeout. If the stream is still reacting at the end, the
        // time is reset.
        if (this.isolation != 0)
            this.isolation = System.currentTimeMillis();
        if (this.output != null) {
            this.output.write(string.getBytes());
            this.output.write(bytes);
        }
        if (this.isolation != 0)
            this.isolation = -1;
        
        // volume of sent data is registered
        this.volume += bytes.length;  
    }

    /**
     * Accepts the request and organizes the processing and response.
     * @throws Exception
     *     In case of unexpected errors
     */
    private void service()
            throws Exception {

        try {
            
            // the connection is already accepted, so that the server process
            // does not block unnecessarily, the connection is initialized only
            // with running thread as asynchronous process
            try {this.initiate();
            } catch (Exception exception) {
                this.status = 500;
                throw exception;
            }             
            
            // determine the resource
            File file = new File(this.resource);

            // resource must refer a module, file or directory
            if (this.status == 0)
                this.status = this.resource.toUpperCase().contains("[M]")
                        || file.isDirectory() || file.isFile() ? 200 : 404;            
            
            if (this.control) {

                // PROCESS/(X)CGI/MODULE - wird bei gueltiger Angabe ausgefuehrt
                if (this.status == 200
                        && this.gateway.length() > 0) {
                    
                    try {this.doGateway();
                    } catch (Exception exception) {
                        this.status = 502;
                        throw exception;
                    }
                    
                } else {

                    // determine the HTTP method
                    String method = this.fields.get("req_method").toLowerCase();

                    // server supports: OPTIONS, HEAD, GET, PUT, DELETE
                    // for other methods STATUS 501 is responded
                    if (this.status == 200
                            && !method.equals("options")
                            && !method.equals("head")
                            && !method.equals("get")
                            && !method.equals("put")
                            && !method.equals("delete"))
                        this.status = 501;

                    if (this.status == 200
                            && (method.equals("head")
                                    || method.equals("get")))
                        try {this.doGet();
                        } catch (Exception exception) {
                            this.status = 500;
                            throw exception;
                        }                    

                    if ((this.status == 200
                                || this.status == 404)
                            && method.equals("put"))
                        try {this.doPut();
                        } catch (Exception exception) {
                            this.status = 424;
                            throw exception;
                        }                    
                    
                    if (this.status == 200
                            && method.equals("delete"))
                        try {this.doDelete();
                        } catch (Exception exception) {
                            this.status = 424;
                            throw exception;
                        }    
                }
            }            
            
        } finally {

            // Isolation defines the time from the last output when strict
            // timeout is used to detect blocking data streams, because there is
            // no socket-based timeout. If the data stream is still reacting at
            // the end, the time is reset.
            if (this.isolation != 0)
                this.isolation = -1;               
            
            // STATUS/ERROR/METHOD:OPTIONS - will be executed if necessary
            if (this.control)
                try {this.doStatus();            
                } catch (IOException exception) {
                }
        }
    }
   
    /** Logs the access in the logging medium. */
    private void register()
            throws Exception {

        // client is determined
        String string = this.environment.get("remote_addr");
        try {string = InetAddress.getByName(string).getHostName();
        } catch (Throwable throwable) {
        }
        this.environment.set("remote_host", string);
        this.environment.set("response_length", String.valueOf(this.volume));
        this.environment.set("response_status", String.valueOf(this.status == 0 ? 500 : this.status));

        synchronized (Worker.class) {
            
            // format of the ACCESSLOG is determined
            String format = this.options.get("accesslog");
            
            // converting string formatter syntax to generator syntax
            format = format.replaceAll("#", "#[0x23]");
            format = format.replaceAll("%%", "#[0x25]");
            format = format.replaceAll("%\\[", "#[");
            format = format.replaceAll("%t", "%1\\$t");
 
            // time symbols are resolved
            format = String.format(Locale.US, format, new Date());
            
            String output;

            // format and path are separated by the > character
            if (format.contains(">")) {
                output = format.split(">")[1].trim();
                format = format.split(">")[0].trim();
            } else output = "";
            
            // without format and/or with OFF no access log is created
            if (format.length() <= 0
                    || format.toLowerCase().equals("off"))
                return;

            Hashtable values = new Hashtable();
            Enumeration environment = this.environment.elements();
            while (environment.hasMoreElements()) {
                string = (String)environment.nextElement();
                values.put(string, Worker.textEscape(this.environment.get(string)));
            }            
            
            Generator generator;
            
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
            
            // If no ACCESSLOG is defined, it is written to the StdIO,
            // otherwise to the corresponding file.
            if (output.length() > 0) {
                OutputStream stream = new FileOutputStream(output, true);
                try {stream.write(format.getBytes());
                } finally {
                    stream.close();
                }
            } else Service.print(format, true);            
        }
    }
    
    /** Marks the worker for abandonment in case of inactivity. */
    void isolate() {
        if (this.accept == null
                && this.socket != null)
            this.socket = null;
    }

    /**
     * Returns {@code true} if the worker is active and available to continue
     * accepting requests. This will also check for blocked streams if a strict
     * timeout is configured. Then the socket will be closed if necessary.
     * @return {@code true} if the worker is active and available
     */
    boolean available() {
        if (this.socket != null
                && this.isolation > 0
                && this.isolation < System.currentTimeMillis() -this.timeout)
            this.destroy();
        return this.socket != null && this.accept == null;
    }

    /** 
     * Terminates the worker by closing the data stream.
     * Current requests will abort hard.
     * After that, the worker cannot be reactivated.
     */
    void destroy() {

        // ServerSocket is reset,
        // as indicator to abandonment of the worker in case of inactivity
        this.socket = null;

        // socket is finally closed
        try {this.accept.close();
        } catch (Throwable throwable) {
        }
    }

    @Override
    public void run() {

        while (this.socket != null) {

            // initial setup of the variables
            this.status = 0;
            this.volume = 0;

            this.control = true;

            this.docroot   = "";
            this.gateway   = "";
            this.resource  = "";
            this.mediatype = "";
            this.sysroot   = "";

            // fields from the header are configured
            this.fields = new Section(true);

            // configuration is loaded
            this.access      = (Section)this.initialize.get(this.context.concat(":acc")).clone();
            this.environment = (Section)this.initialize.get(this.context.concat(":env")).clone();
            this.filters     = (Section)this.initialize.get(this.context.concat(":flt")).clone();
            this.interfaces  = (Section)this.initialize.get(this.context.concat(":cgi")).clone();
            this.options     = (Section)this.initialize.get(this.context.concat(":ini")).clone();
            this.references  = (Section)this.initialize.get(this.context.concat(":ref")).clone();
            this.statuscodes = (Section)this.initialize.get("statuscodes").clone();
            this.mediatypes  = (Section)this.initialize.get("mediatypes").clone();

            // block size to be used is determined
            try {this.blocksize = Integer.parseInt(this.options.get("blocksize"));
            } catch (Throwable throwable) {
                this.blocksize = 65535;
            }

            if (this.blocksize <= 0)
                this.blocksize = 65535;

            String string = this.options.get("timeout");
            this.isolation = string.toUpperCase().contains("[S]") ? -1 : 0;

            // timeout of the connection is determined
            try {this.timeout = Long.parseLong(Worker.cleanOptions(string));
            } catch (Throwable throwable) {
                this.timeout = 0;
            }

            // maximum process timeout is corrected if necessary
            if (this.timeout < 0)
                this.timeout = 0;

            // interrupt is determined
            try {this.interrupt = Long.parseLong(this.options.get("interrupt"));
            } catch (Throwable throwable) {
                this.interrupt = 10;
            }

            if (this.interrupt < 0)
                this.interrupt = 10;

            try {this.accept = socket.accept();
            } catch (InterruptedIOException exception) {
                continue;
            } catch (IOException exception) {
                break;
            }

            // request is processed
            try {this.service();
            } catch (Throwable throwable) {
                Service.print(throwable);
            }

            // socket is finally closed
            try {this.accept.close();
            } catch (Throwable throwable) {
            }

            // access is logged
            try {this.register();
            } catch (Throwable throwable) {
                Service.print(throwable);
            }

            // socket is discarded
            this.accept = null;
        }
    }
}