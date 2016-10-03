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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *  Service ist ein Container f&uuml;r Server, Services und Module mit den
 *  ben&ouml;tigten Mechanismen, APIs und Sequenzen zur Anbindung, Kontrolle und
 *  Steuerung.<br>
 *  <br>
 *  Server, Services und Module sind Erweiterungen. Sie verwenden eine Basis-API
 *  und werden durch Art und Zeitpunkt der Anbindung klassifiziert, wodurch sich
 *  die API entsprechend ihrer Verwendung erweitert.<br>
 *  <br>
 *  <dir>
 *    <b>Begriffe</b>
 *  </dir>
 *  <dir>
 *    <dir>
 *      Server
 *    </dir>
 *  </dir>
 *  Diese Form der Erweiterungen ist essentiell und stellt die nach aussen hin
 *  verf&uuml;gbaren Funktionen und Kommunikation bereit. Server werden in
 *  eigenst&auml;ndigen SERVER-Sektionen konfiguriert und &uuml;ber ihren Namen
 *  identifiziert und vom Application-ClassLoader als multiple Instanzen
 *  initialisiert.<br>
 *  Beim Start vom Service wird mindestens eine lauff&auml;hige Server-Instanz
 *  ben&ouml;tigt.<br>
 *  <br>
 *  <dir>
 *    <dir>
 *      Services
 *    </dir>
 *  </dir>
 *  Diese optionalen Erweiterungen f&uuml;r Hintergrundaktivit&auml;ten werden
 *  in der Sektion <code>INITIALIZE</code> konfiguriert und mit Start und
 *  Restart vom Service &uuml;ber den Application-ClassLoader initialisiert.
 *  Services werden &uuml;ber die Ausf&uuml;hrungsklasse identifiziert und
 *  werden vom Application-ClassLoader nur einmalig angebunden und
 *  initialisiert.<br>
 *  <br>
 *  <dir>
 *    <dir>
 *      Module
 *    </dir>
 *  </dir>
 *  Diese optionalen Erweiterungen zur Bereitstellung von service-bezogenen
 *  Funktionalit&auml;ten haben keine definierte Form der Konfiguration. Die
 *  Initialisierung erfolgt individuell durch Server und Services mit Angabe der
 *  anzuwendenden Parameter. Module werden &uuml;ber die Ausf&uuml;hrungsklasse
 *  identifiziert und werden vom Application-ClassLoader nur einmalig angebunden
 *  und initialisiert.<br>
 *  Optional ist die Verwendung vom Context-ClassLoader auf dem Seanox Devwex
 *  SDK m&ouml;glich, womit Module auch in mehreren unabh&auml;ngigen Instanzen
 *  mit eigenem ClassLoader verwendet werden k&ouml;nnen.<br>
 *  <br>
 *  <dir>
 *    <dir>
 *      Anmerkung
 *    </dir>
 *  </dir>
 *  Der Service unterteilt alle Erweiterungen nur in die Kategorien: Server und
 *  Module. Services und Module unterscheiden sich nur &uuml;ber den Initiator.
 *  Services werden beim (Neu)Start aus der Sektion <code>INITIALIZE</code>
 *  ermittelt und geladen. Die Initialisierung von Modulen obliegt anderen
 *  Komponenten und erfolgt erst zur Laufzeit.<br>
 *  In der Nachfolgenden Dokumentation werden daher nur die Begriffe Server und
 *  Module verwendet.<br>
 *  <br>
 *  <dir>
 *    <b>Arbeitsweise</b>
 *  </dir>
 *  Alle f&uuml;r die Initialisierung und den Betrieb erforderlichen Daten
 *  werden aus der Konfigurationsdatei <code>devwex.ini</code> gelesen, die aus
 *  dem aktuellen Arbeitsverzeichnis geladen wird. Beim Start, Neustart und Stop
 *  werden feste Sequenzen zum Laden und Entladen von Servern und Modulen
 *  durchlaufen, welche &uuml;ber diese Konfigurationsdatei ermittelt werden.<br>
 *  Im Betrieb &uuml;berwacht der Service Konfiguration, Server sowie Module und
 *  steuert den Garbage Collector f&uuml;r eine schneller Freigabe von
 *  Ressourcen.<br>
 *  <br>
 *  <dir>
 *    <b>Sequenzen</b>
 *  </dir>
 *  <dir>
 *    <dir>
 *      Start
 *    </dir>
 *  </dir>
 *  <ul>
 *    <li>
 *      Der Klassenpfad wird um alle Dateien der Verzeichnisse erweitert, die
 *      mit dem VM-Argument <code>-Dlibraries</code> angegeben wurden.
 *    <li>
 *      Alle Module aus der Sektion <code>INITIALIZE</code> werden geladen und
 *      optional &uuml;ber <code>Module.initialize(String)</code> initialisiert.
 *    <li>
 *      Alle Server werden &uuml;ber das Muster <code>SERVER:*:BAS</code> in den
 *      Sektionen ermittelt und &uuml;ber <code>Server(String, Object)</code>
 *      initialisiert.
 *  </ul>
 *  <dir>
 *    <dir>
 *      Fehler beim Start
 *    </dir>
 *  </dir>
 *  <ul>
 *    <li>
 *      Alle Server-Instanzen werden &uuml;ber <code>Server.destroy()</code> zum
 *      Beenden aufgefordert
 *    <li>
 *      Alle geladenen Module werden &uuml;ber <code>Module.destroy()</code> zum
 *      Beenden aufgefordert.
 *    <li>
 *      Alle Module werden entladen und der ClassLoader verworfen.
 *  </ul> 
 *  <dir>
 *    <dir>
 *      Neustart
 *    </dir>
 *  </dir>
 *  <ul>
 *    <li>
 *      Alle Server-Instanzen werden &uuml;ber <code>Server.destroy()</code> zum
 *      Beenden aufgefordert.
 *    <li>
 *      Alle geladenen Module werden &uuml;ber <code>Module.destroy()</code> zum
 *      Beenden aufgefordert.
 *    <li>
 *      Alle Module werden entladen und der ClassLoader verworfen.
 *    <li>
 *      Der Klassenpfad wird um alle Dateien der Verzeichnisse erweitert, die
 *      mit dem VM-Argument <code>-Dlibraries</code> angegeben wurden.
 *    <li>
 *      Alle Module aus der Sektion <code>INITIALIZE</code> werden geladen und
 *      optional &uuml;ber <code>Module.initialize(String)</code> initialisiert.
 *    <li>
 *      Alle Server werden &uuml;ber das Muster <code>SERVER:*:BAS</code> in den
 *      Sektionen ermittelt und &uuml;ber <code>Server(String, Object)</code>
 *      initialisiert.
 *  </ul> 
 *  <dir>
 *    <dir>
 *      Modulaufruf
 *    </dir>
 *  </dir>
 *  <ul>
 *    <li>
 *      Wenn das Modul noch nicht initialisiert wurde, wird es geladen und
 *      optional initialisiert, wenn diese implementiert wurde.
 *  </ul>
 *  <dir>
 *    <dir>
 *      Beenden
 *    </dir>
 *  </dir>
 *  <ul>
 *    <li>
 *      Alle Server-Instanzen werden &uuml;ber <code>Server.destroy()</code> zum
 *      Beenden aufgefordert
 *    <li>
 *      Alle geladenen Module werden &uuml;ber <code>Module.destroy()</code> zum
 *      Beenden aufgefordert.
 *    <li>
 *      Alle Module werden entladen und der ClassLoader verworfen.
 *  </ul>
 *  <br>
 *  <dir>
 *    <b>API</b>
 *  </dir>
 *  Seanox Devwex verwendet keine Interfaces. Es war einfach kein Platz
 *  daf&uuml;r. Zum anderen war es eine Entscheidung f&uuml;r Seanox Commons als
 *  Bestandteil vom Seanox Devwex SDK, damit dieses auch ohne Seanox Devwex
 *  nutzbar ist. So war es eine Entscheidung zur Nutzung stiller Standards.<br>
 *  <br>
 *  <dir>
 *    <dir>
 *      Server
 *    </dir>
 *  </dir>
 *  <dir>
 *    <dir>
 *      <dir>
 *        Implementierung
 *      </dir>
 *    </dir>
 *  </dir>
 *  Server basieren auf der folgenden Basis-Implementierung, die auch ohne
 *  Interface zur Laufzeit gepr&uuml;ft wird.<br>
 *  <pre>
 *    public class Server implements Runnable {
 *        
 *        public Server(String name, Object data) {
 *            ...
 *            
 *            Der Server wird durch Seanox Devwex ueber den Konstruktor
 *            initiiert. Liegt dieser nicht vor, wird der Server als
 *            inkompatible betrachtet und nicht geladen. Die Konfiguration wird
 *            neben dem Namen als Initialize-Objekt uebergeben.
 *        }
 *
 *        public String getCaption() {
 *            ...
 *            
 *            Die Methode dient zur allgemeinen Information und gibt die
 *            Serverkennung im Format: &lt;PROTOCOL HOST-NAME:PORT&gt; oder 
 *            &lt;PROTOCOL HOST-ADRESSE:PORT&gt; zurueck und sollte mit dem
 *            Aufruf des Konstruktors oder statisch gesetzt werden.
 *        }
 *
 *        public void run() {
 *            ...
 *            
 *            Der Server wird als Thread gestartet.
 *        }
 *
 *        public void destroy {
 *            ...
 *            
 *            Der Server wird ueber die Server-API zum Beenden aufgeforder.
 *            Der Service entlaedt den Server samt ClassLoader. Da je nach Art
 *            der Implementierung Sockets oder Datenstroeme unter
 *            Umst&auuml;nden blockieren, sollten diese mit dem Aufruf
 *            geschlossen werden.
 *        }
 *    }
 *  </pre>
 *  <dir>
 *    <dir>
 *      <dir>
 *        Konfiguration
 *      </dir>
 *    </dir>
 *  </dir>
 *  Die Konfiguration der Server erfolgt in <code>devwex.ini</code>. Seanox
 *  Devwex ermittelt und l&auml;dt alle Server &uuml;ber eingest&auml;ndige
 *  Server-Sektionen im Format: <code>[SERVER:&lt;NAME&gt;:BAS]</code>.<br>
 *  <pre>
 *    [SERVER:NAME:BAS]
 *       ...
 *  </pre>
 *  Weitere Details zu Sektionen und Parametern ergeben sich durch die
 *  Implementierung vom Server.<br>
 *  <br>
 *  <dir>
 *    <dir>
 *      Module
 *    </dir>
 *  </dir>
 *  <dir>
 *    <dir>
 *      <dir>
 *        Implementierung
 *      </dir>
 *    </dir>
 *  </dir> 
 *  Module basieren auf der folgenden Basis-Implementierung, die auch ohne
 *  Interface zur Laufzeit gepr&uuml;ft wird.<br>
 *  <pre>
 *  public class Module {
 *  
 *      public Module() {
 *          ...
 *          
 *          Das Modul wird durch Seanox Devwex ueber den Standard-Konstruktor
 *          initiiert. Liegt dieser nicht vor, wird das Module als inkompatible
 *          zur API betrachtet und nicht geladen.
 *      }
 *
 *      public String getCaption() {
 *          ...
 *          
 *          Die Methode dient zur allgemeinen Information und gibt die
 *          Modulkennung im Format &lt;PRODUCER-MODULE/VERSION&gt; zur&uuml;ck
 *          und sollte mit dem Aufruf des Konstruktors oder statisch gesetzt
 *          werden.
 *      }
 *
 *      public void initialize(String options) {
 *          ...
 *          
 *          Das Modul wird initialisiert. Die Konfiguration ist optional und
 *          wird abhaengig vom Initiator als String uebergeben.
 *      }
 *
 *      public void destroy() {
 *        ...
 *        
 *         Das Modul wird ueber die Modul-API zum Beenden aufgeforder.
 *         Der Service entlaedt das Modul samt ClassLoader. Da je nach Art der
 *         weitere Ressourcen oder Datenstroeme verwendet werden, sollten diese
 *         mit dem Aufruf geschlossen werden.
 *      }
 *  }
 *  </pre>
 *  <dir>
 *    <dir>
 *      <dir>
 *        Konfiguration
 *      </dir>
 *    </dir>
 *  </dir>
 *  Module werden u.a. mit dem Start von Seanox Devwex geladen und daher in der
 *  Sektion <code>INITIALIZE</code> deklariert und konfiguriert.<br>
 *  <pre>
 *    [INITIALIZE]
 *      EXAMPLE = exampe.Module [parameter:value] ...
 *  </pre>
 *  Mit der Option <code>[*]</code> werden Module als optional gekennzeichnet.
 *  Sind diese im Fall der Initialisierung nicht vorhanden, f&uuml;hrt das mit
 *  dieser Option zu keiner Fehlerausgabe.<br>
 *  <pre>
 *    [INITIALIZE]
 *      EXAMPLE = exampe.Module [parameter:value] ... [*]
 *  </pre>
 *  Weitere Details zu Parametern ergeben sich durch die Implementierung vom
 *  Modul.<br>
 *  <br>
 *  Service 5.0 20160810<br>
 *  Copyright (C) 2016 Seanox Software Solutions<br>
 *  Alle Rechte vorbehalten.
 *
 *  @author  Seanox Software Solutions
 *  @version 5.0 20160810
 */
public class Service implements Runnable, UncaughtExceptionHandler {

    /** ClassLoader f&uuml;r geladene Ressourcen */
    private volatile ClassLoader loader;

    /** Konfiguration des Services */
    private volatile Initialize initialize;
    
    /** Liste der initialisierten Module */
    private volatile Hashtable modules;

    /** Liste der eingerichteten Server */
    private volatile Vector server;

    /** Betriebsstatus des Services */
    private volatile int status;

    /** Startzeit des Serices */
    private volatile long timing;

    /** Referenz des generischen Services */
    private static volatile Service service;

    /** Option f&uuml;r die erweiterte Fehlerausgabe */
    private static volatile boolean verbose;

    /** Konstante mit der aktuellen Version des Services */
    public static final String VERSION = "#[ant:release-version]";

    /** Konstante f&uuml;r den Betriebsstatus Unbekannt */
    public static final int UNKNOWN = 0;

    /** Konstante f&uuml;r den Betriebsstatus Start */
    public static final int START = 1;

    /** Konstante f&uuml;r den Betriebsstatus Betrieb */
    public static final int RUN = 2;

    /** Konstante f&uuml;r den Betriebsstatus Neustart */
    public static final int RESTART = 3;

    /** Konstante f&uuml;r den Betriebsstatus Beenden */
    public static final int STOP = 4;

    /** Konstruktor, richtet den Service ein. */
    private Service() {

        this.initialize = new Initialize(true);
        this.modules    = new Hashtable();
        this.server     = new Vector();
    }

    /**
     *  Wartet f&uuml;r die in Millisekunden angegebenen Dauer, unterbricht
     *  jedoch nicht die Verarbeitung der laufenden Prozesse.
     *  @param duration Dauer in Millisekunden
     */
    static void sleep(long duration) {

        try {Thread.sleep(duration);
        } catch (Throwable throwable) {

            //keine Fehlerbehandlung erforderlich
        }
    }

    /**
     *  L&auml;dt die per Name angegebene Klasse &uuml;ber den servereigenen
     *  ClassLoader. Ist dieser noch nicht eingerichtet wird <code>null</code>
     *  zur&uuml;ckgegeben. Kann die Klasse nicht geladen werden, f&uuml;hrt der
     *  Aufruf in diesem Fall zur Ausnahme <code>ClassNotFoundException</code>.
     *  @param  name Name der Klasse
     *  @return die &uuml;ber den servereigenen ClassLoader ermittelte Klasse,
     *          liegt dieser noch nicht vor oder wurde noch nicht eingerichtet
     *          wird <code>null</code> zur&uuml;ck gegeben
     *  @throws ClassNotFoundException
     *      Wenn die Klasse nicht geladen werden kann.
     */
    public static Class load(String name) throws ClassNotFoundException {
        
        Service service;
        service = Service.service;
        if (service == null || name == null)
            return null;
        return service.loader.loadClass(name);
    }

    /**
     *  Fordert ein Modul an. Ist dieses noch nicht registriert, wird es vom
     *  Service mit den optional &uuml;bergebenen Daten eingerichtet wenn es
     *  sich um einen Modul-Context handelt, sonst wird nur die Klasse als
     *  Referenz vorgehalten. Bei der Initialisierung auftretende Fehler werden
     *  nicht behandelt und weitergereicht.
     *  @param  module  Modul-Klasse
     *  @param  options optinale Daten zur Einrichtung
     *  @return die Klasse deren vorhandene Instanz, sonst <code>null</code>
     *  @throws Exception
     *      Bei Fehlern im Zusammenhang mit der Initialisierung.
     */
    public static Object call(Class module, String options) throws Exception {

        Object  object;
        Service service;
        
        service = Service.service;
        if (service == null || module == null)
            return null;
        
        synchronized (service.getClass()) {
            
            //das Modul wird aus der Modulliste ermittelt, ist dieses noch nicht
            //registriert, wird es neu initialisiert und eingetragen
            object = service.modules.get(module);

            if (object != null) return object;
                
            //die Instanz vom Modul wird optional eingerichtet
            //kann keine Instanz angelegt werden, wird die Klasse verwendet
            try {object = module.newInstance();
            } catch (InstantiationException exception) {
                
                object = module;
            }

            //die Initialisierung Modulen ist optional
            try {module.getMethod("initialize", new Class[] {String.class}).invoke(object, new Object[] {options});
            } catch (NoSuchMethodException exception) {

                //keine Fehlerbehandlung erforderlich
            }

            //das Modul wird registriert
            service.modules.put(module, object);
         
            return object;
        }
    }

    /**
     *  (Re)Initialisiert den Service und alle Server, wozu die folgenden
     *  Betriebszust&auml;nde unterschieden werden:<br>
     *      <dir>START</dir>
     *  Ist der Serivce noch nicht eingerichtet, wird dieser initialisiert, der
     *  Klassenfad erweitertet, die Module geladen, die Server ermittelt und
     *  gestartet sowie der Service gestartet.<br>
     *      <dir>RESTART</dir>
     *  Der laufenden Service bleibt erhalten, alle Module und Server werden
     *  beendet, der Klassenfad erweitertet, die Module geladen (wenn sich diese
     *  beenden liesen) und die Server neu initialisiert.<br>
     *      <dir>STOP</dir>
     *  Der laufenden Service bleibt erhalten, alle Module und Server werden
     *  beendet. Scheitert dieses erfolgt ein normaler Restart um einen
     *  stabielen und kontrollierbaren Betriebszustand herbei zuf&uuml;hren.
     *  Dazu wird auf Basis der letzten lauff&auml;higen Konfiguration der
     *  Klassenfad erweitertet, die Module geladen (wenn sich diese beenden
     *  liesen) und die Server neu initialisiert. In diesem Fall wird der
     *  Betriebzustand auf READY gesetzt und DESTROY verworfen.
     *  @param  modus Betriebsmodus 
     *  @return <code>true</code> bei erfolgreicher Ausf&uuml;hrung
     */
    public static boolean initialize(int modus) {

        Class           source;
        ClassLoader     loader;
        Enumeration     enumeration;
        Object          object;
        Section         section;
        Service         service;
        StringTokenizer tokenizer;
        String          context;
        String          buffer;
        String          string;
        Thread          thread;
        Vector          libraries;

        File[]          files;

        double          timing;
        int             loop;
        int             size;
        long            timeout;
        
        synchronized (Service.class) {
            
            //die Zeitpunkt der Initialisierung wird ermittelt
            timing = System.currentTimeMillis();

            //der Service wird gesetzt wenn keine statische Instanz vorliegt
            if (Service.service == null)
                Service.service = new Service();
            
            service = Service.service;

            if (modus == Service.STOP
                    || modus == Service.RESTART) {

                if (service.status != Service.RUN && service.status != UNKNOWN)
                    return false;
                
                Service.print(("SERVICE INITIALIZE ").concat(modus == Service.RESTART ? "RESTART" : "STOP"));

                //alle registrierten Server werden ermittelt
                enumeration = service.server.elements();
                while (enumeration.hasMoreElements()) {

                    //der Server wird angefordert
                    object = ((Object[])enumeration.nextElement())[0];

                    //der Server wird zum Beenden aufgefordert
                    try {object.getClass().getMethod("destroy", new Class[0]).invoke(object, new Object[0]);
                    } catch (Throwable throwable) {
                 
                        Service.print(throwable);
                    }
                }

                //alle registrierten Module werden ermittelt
                enumeration = service.modules.keys();
                while (enumeration.hasMoreElements()) {

                    //das Modul wird aus der Modulliste entfernt
                    //und dabei die Instanz vom Modul ermittelt
                    object = service.modules.remove(enumeration.nextElement());

                    //das Beenden vom Modulen ist optional
                    try {object.getClass().getMethod("destroy", new Class[0]).invoke(object, new Object[0]);
                    } catch (Throwable throwable) {
                        
                        if (throwable instanceof NoSuchMethodException)
                            continue;

                        Service.print(throwable);
                    }
                }
                
                //das Timeout wird ermittelt
                timeout = System.currentTimeMillis() +65535;

                //die Anzahl aktiver Server wird ermittelt
                size = service.server.size();

                while (service.server.size() > 0
                        && timeout > System.currentTimeMillis()) {
                    
                    //alle aktiven Server werden ermittelt und durchsucht, bei
                    //noch laufenden Servern wird gewartet bis diese enden oder
                    //das Timeout erreicht wird
                    enumeration = ((Vector)service.server.clone()).elements();
                    while (enumeration.hasMoreElements()) {

                        //der Server wird angefordert
                        //inaktive Server werden aus der Serverliste entfernt
                        object = enumeration.nextElement();
                        if (!((Thread)((Object[])object)[1]).isAlive())
                            service.server.remove(object);
                    }

                    //das Timeout wird bei Erfolgen verlaengert
                    if (service.server.size() != size)
                        timeout = System.currentTimeMillis() +65535;
                    
                    Service.sleep(25);
                }

                if (service.server.size() > 0) {
                    
                    Service.print("SERVICE STOP FAILED");
                    
                    return false;
                }
                
                service.status = modus;
                if (service.status == Service.STOP)
                    return true;
            }

            if (modus == Service.START
                    || modus == Service.RESTART) {
                
                if (service.status != Service.UNKNOWN
                        && service.status != Service.RESTART)
                    return false;
                
                if (modus == Service.START)
                    Service.print("SERVICE INITIALIZE START");

                string = "SERVICE RESOURCES LOADING";
                
                //die Liste der Bibliotheken wird eingerichtet
                libraries = new Vector();

                //die Pfade des Klassenpfads werden ermittelt
                tokenizer = new StringTokenizer(Section.parse("libraries [?]", true).get("libraries"), File.pathSeparator);
                while (tokenizer.hasMoreTokens()) {
                    
                    files = new File[] {new File(tokenizer.nextToken())};
                    if (files[0].isDirectory())
                        files = files[0].listFiles();

                    for (loop = 0; loop < files.length; loop++) {

                        try {

                            //es werden nur Dateien beruecksichtigt
                            if (!files[loop].isFile())
                                continue;

                            string = string.concat("\r\n").concat(files[loop].getPath());
                            files[loop] = files[loop].getCanonicalFile();
                            libraries.add(files[loop]);
                           
                        } catch (Throwable throwable) {

                            //keine Fehlerbehandlung vorgesehen
                        }
                    }
                }
                
                if (string.length() > 0)
                    Service.print(string);
                
                //der ClassLoader wird ermittelt
                loader = service.getClass().getClassLoader();

                //der ClassLoader wird (neu) eingerichtet
                loader = service.loader = new Loader(loader, libraries);
                
                //die aktuelle Konfiguration wird geladen
                try {service.initialize = Initialize.parse(new String(Service.read(new File("devwex.ini"))), true);
                } catch (Throwable throwable) {

                    Service.print("SERVICE CONFIGURATION LOADING FAILED");

                    Service.print(throwable);
                }
                
                //die Basisoptionen werden ermittelt
                section = service.initialize.get("initialize");

                //alle Parameter werden ermittelt
                enumeration = section.elements();
                while (enumeration.hasMoreElements()) {

                    string = (String)enumeration.nextElement();

                    //die Ressource wird ermittelt
                    buffer = section.get(string).concat(" ");

                    //die Position der Optionen wird ermittelt
                    size = Math.min(buffer.indexOf(" "), buffer.concat("[").indexOf("["));

                    try {

                        //die Klasses der Ressource wird geladen
                        source = loader.loadClass(buffer.substring(0, size).trim());

                        //das Modul wird initial eingerichtet, konnte dessen
                        //Instanz zuvor nicht beendet werden und liegen noch vor
                        //und wird es ignoriert um ein Vollaufen des Speichers
                        //zu verhindern
                        Service.call(source, buffer.substring(size).trim());

                    } catch (Throwable throwable) {

                        if (throwable instanceof ClassNotFoundException
                                && buffer.matches("\\[\\s*\\*\\s*\\]"))
                            continue;
                        
                        Service.print(String.format("MODULE %S INITIALIZE FAILED", new Object[] {string}));

                        Service.print(throwable);
                    }
                }
                
                //die Server werden aus der Konfiguration ermittelt
                enumeration = service.initialize.elements();
                while (enumeration.hasMoreElements()) {

                    context = (String)enumeration.nextElement();
                    buffer = context.replaceAll("(?i)(^server:(.*):bas$)|(^.*$)", "$2").trim();
                    if (buffer.length() <= 0)
                        continue;
                    
                    object = null;

                    try {

                        //der Connector wird aus der Basiskonfiguration ermittelt
                        //namenlose Server werden jedoch ignoriert
                        string = service.initialize.get(context).get("connector");
                        
                        //die Server Klasse wird geladen
                        if (string.length() == 0 && buffer.matches("(?i)remote"))
                            source = Remote.class;
                        else if (string.length() == 0)
                            source = Server.class;
                        else source = loader.loadClass(string);

                        //Server muessen ueber diese Methode verfuegen
                        source.getMethod("destroy", new Class[0]);

                        //Server muessen ueber diese Methode verfuegen
                        source.getMethod("getCaption", new Class[0]);

                        context = context.replaceAll("(?i):bas$", "");

                        //der Server wird eingerichtet
                        object = source.getConstructor(new Class[] {String.class, Object.class});
                        object = ((Constructor)object).newInstance(new Object[] {context, service.initialize.clone()});

                        //der Server muss Runnable implementieren
                        if (!(object instanceof Runnable))
                            throw new NoSuchMethodException();

                        //der Server Thread wird eingerichtet
                        thread = new Thread((Runnable)object);

                        //der Server Thread wird als Daemon verwendet
                        thread.setDaemon(true);

                        //der Server Thread wird gestartet
                        thread.start();

                        //der Server wird mit Thread registriert
                        service.server.add(new Object[] {object, thread});

                    } catch (Throwable throwable) {

                        Service.print(String.format("SERVER %S INITIALIZE FAILED", new Object[] {buffer}));

                        Service.print(throwable);

                        if (object != null) {

                            //das Beenden des Servers wird angestossen
                            try {object.getClass().getMethod("destroy", new Class[0]).invoke(object, new Object[0]);
                            } catch (Throwable error) {

                                Service.print(error);
                            }
                        }
                    }

                    Service.sleep(25);
                }
                
                //wurde kein Server gefunden, wird eine Information ausgegeben
                if (service.server.size() <= 0)
                    Service.print("SERVICE NO SERVER AVAILABLE");

                //die Dauer des Startvorgangs wird ermittelt
                timing = (System.currentTimeMillis() -timing) /1000;

                if (service.server.size() > 0)
                    Service.print(String.format("SERVICE %SSTARTED (%S SEC)",
                            new Object[] {modus == Service.RESTART ? "RE" : "", String.valueOf(timing)}));

                //beim Restart wird Status READY gesetzt und die Methode verlassen
                if (modus == Service.RESTART) {
                    
                    service.status = Service.RUN;
                    
                    return true;
                }
                
                //beim Start wird ohne eingerichtete Server, der Service beendet
                if (modus == Service.START && service.server.size() <= 0) {
                    
                    service.status = Service.STOP;
                    
                    Service.service = null;
                    
                    return false;
                }
                
                //im Startprozess wird Status und ShutdownHook gesetzt
                if (modus == Service.START)
                    Runtime.getRuntime().addShutdownHook(new Thread(new Service()));

                //der Thread wird eingerichtet und gestartet,
                //scheitert diese wird der Service zurueckgesetzt
                try {new Thread(service).start();
                } catch (Throwable throwable) {

                    Service.print("SERVICE START FAILED");                               
                    Service.print(throwable);
                    
                    Service.initialize(Service.STOP);
                    
                    //Standardinformation wird ausgegeben
                    Service.print("SERVICE STOPPED");
                    
                    Service.service = null;
                    
                    return false;
                }                

                return true;
            }
            
            return false;
        }
    }

    /**
     *  R&uuml;ckgabe des aktuellen und detaillierten Betriebsstatus.
     *  @return der aktuelle und detaillierte Betriebsstatus
     */
    public static String details() {

        Object      object;
        Object      caption;
        Service     service;
        String      result;
        String      string;
        Enumeration enumeration;
        
        result = "VERS: #[ant:release-version]\r\n";

        result = result.concat(String.format("\r\nTIME: %tF %<tT\r\n", new Object[] {new Date()}));
        
        service = Service.service;
     
        if (service != null) {

            result = result.concat(String.format("TIUP: %tF %<tT\r\n", new Object[] {new Date(service.timing)}));

            synchronized (service.getClass()) {
                
                string  = "";  

                //alle registrierten Module werden ermittelt
                enumeration = service.modules.elements();
                while (enumeration.hasMoreElements()) {
                    
                    //die Modul-Instanzen werden einzeln ermittelt
                    object = ((Object[])enumeration.nextElement())[0];
                    
                    //die Modulkennung wird ueber Caption ermittelt
                    try {caption = object.getClass().getMethod("getCaption", new Class[0]).invoke(object, new Object[0]);
                    } catch (Throwable throwable) {
                        caption = null;
                    }
                    
                    if (caption == null)
                        caption = object;

                    //die Ausgabe wird zusammengesetzt
                    string = string.concat("XAPI: ").concat(String.valueOf(caption)).concat("\r\n");
                }
                
                string = string.trim();
                if (string.length() > 0)
                    result = result.concat("\r\n").concat(string).concat("\r\n");

                string = "";
                
                //alle registrierten Server werden ermittelt
                enumeration = service.server.elements();
                while (enumeration.hasMoreElements()) {

                    //die Server-Instanzen werden einzeln ermittelt
                    object = ((Object[])enumeration.nextElement())[0];

                    //die Serverkennung wird ueber Caption ermittelt
                    try {caption = object.getClass().getMethod("getCaption", new Class[0]).invoke(object, new Object[0]);
                    } catch (Throwable throwable) {
                        caption = null;
                    }

                    if (caption == null)
                        caption = object;                        

                    //die Ausgabe wird zusammengesetzt
                    string = string.concat("SAPI: ").concat((String)caption).concat("\r\n");
                }
                
                string = string.trim();
                if (string.length() > 0)
                    result = result.concat("\r\n").concat(string).concat("\r\n");
            }
        }
        
        return result;
    }
    
    /**
     *  Startet alle eingerichteten Server Instanzen neu.
     *  @return <code>true</code> bei erfolgreichem Neustart
     */
    public static boolean restart() {

        //beim Service wird das Beenden eingeleitet
        return Service.initialize(Service.RESTART);
    }

    /**
     *  Beendet den Service mit allen Server Instanzen.
     *  @return <code>true</code> bei erfolgreicher Ausf&uuml;hrung
     */
    public static boolean destroy() {

        //beim Service wird das Beenden eingeleitet
        return Service.initialize(Service.STOP);
    }
    
    /**
     *  Liest die angegebene Datei ein.
     *  R&uuml;ckgabe das ByteArray, im Fehlerfall ein leeres ByteArray.
     *  @param  file zu lesenden Datei
     *  @return das gelesene ByteArray, im Fehlerfall ein leeres ByteArray
     */
    private static byte[] read(File file) {
        
        try {return Files.readAllBytes(file.toPath());
        } catch (Throwable throwable) {
            return new byte[0];
        }
    }

    /**
     *  Haupteinsprung in die Anwendung.
     *  @param options Startargumente
     */
    public static void main(String options[]) {
        
        Initialize initialize;
        Section    section;
        String     string;
        
        //Ausgabeinformation wird zusammen gestellt und ausgegeben
        System.out.println("Seanox Devwex [Version #[ant:release-version]]");
        System.out.println("Copyright (C) #[ant:release-year] Seanox Software Solutions");
        System.out.println("Advanced Server Development\r\n");

        //Kommando wird ermittelt
        string = (options != null && options.length > 0) ? options[0].trim().toLowerCase() : "";

        Service.service = new Service();
        
        //der globale Exception Handler wird gesetzt
        //Thread.setDefaultUncaughtExceptionHandler(Service.service);
        
        //die Option fuer die erweiterte Ausgabe wird ermittelt
        Service.verbose = string.endsWith("*");
        
        //START - starten der Serverdienste
        if (string.matches("start\\**")) {

            Service.initialize(Service.START);
        
            return;
        }

        //RESTART | STATE | STOP - die Kommandos werden an den Server
        //gesendet und der Response als Result gesetzt
        if (string.matches("(restart|state|stop)\\**")) {

            try {

                //die Konfiguration wird geladen
                initialize = Initialize.parse(new String(Service.read(new File("devwex.ini"))), true);
                
                //die Konfiguration des Remote Access wird ermittelt
                section = initialize.get("server:remote:bas");

                //der Remoteausfruf wird ausgefuehrt
                string = new String(Remote.call(section.get("address"), Integer.parseInt(section.get("port")), string));

            } catch (Throwable throwable) {

                string = "INFO: REMOTE ACCESS FAILED";
            }

            if (string.length() == 0)
                string = "INFO: REMOTE ACCESS NOT AVAILABLE";

            //der Result wird als Information ausgegeben
            System.out.println(string.trim());
            
            return;
        } 

        //bei unbekannten Kommandos wird die Kommandoliste ausgegeben
        System.out.println("Usage: devwex [start|restart|state|stop][*]");
    }
    
    /**
     *  Protokolliert das &uuml;bergebene Objekt zeilenweise und mit
     *  vorangestelltem Zeitstempel in den Standard IO. Zur Ermittlung des
     *  Protokolltexts wird <code>Object.toString()</code> vom &uuml;bergebenen
     *  Objekt verwendet. Bei der &uuml;bergabe von Fehlerobjekten wird der
     *  StackTrace zeilenweise protokolliert wenn der Server mit erweiterter
     *  Ausgabe gestartet wurde.
     *  @param object Objekt mit dem Protokolleintrag
     */
    public static void print(Object object) {

        String    string;
        String    timing;
        Throwable throwable;

        while (object instanceof InvocationTargetException
                && (throwable = ((InvocationTargetException)object).getTargetException()) != null)
            object = throwable;

        if (object instanceof Throwable) {

            if (!Service.verbose)
                return;
            
            throwable = ((Throwable)object);
            object    = new StringWriter();
            throwable.printStackTrace(new PrintWriter((Writer)object));
        }
        
        string = String.valueOf(object).trim();

        if (string.length() == 0)
            return;

        synchronized (System.out.getClass()) {

            //der Zeitstempel wird ermittelt
            timing = String.format("%tF %<tT ", new Object[] {new Date()});

            //der Informationstext wird bereinig
            string = timing.concat(string).replaceAll("[\\x00-\\x09]+", " ").trim();

            //Zeilenumbrueche werden mit Zeitstempel und Einrueckung formatiert
            string = string.replaceAll("(?s)([\r\n]\\s*)+", String.format("%n%s... ", new Object[] {timing}));
            
            //die Folgezeilen werden nur im erweiterten Modus ausgegeben
            if (!Service.verbose)
                string = string.replaceAll("(?s)[r\n].*$", "");
            
            //der Inhalt wird ausgegeben
            System.out.println(string.trim());
        }
    }
    
    /**
     *  R&uuml;ckgabe vom aktuellen Betriebsstatus.
     *  @return der aktuelle Betriebsstatus
     *  @see    {@link #UNKNOWN}, {@link #INITIALIZE}, {@link #READY},
     *          {@link #RESTART}, {@link #STOP}
     */
    public static int status() {
        
        Service service;
        
        service = Service.service;

        //die Abfrage des Betriebsstatus ist asynchron moeglich womit nicht
        //sichergestellt werden kann, das der Service verfuegbar ist
        return service != null ? service.status : Service.UNKNOWN; 
    }

    /**
     *  Stellt den Einsprung f&uuml;r den Thread zur Verf&uuml;gung.
     *  Der Thread kontrolliert die Server und steuert den Garbage Collector.
     */
    public void run() {

        File    file;
        Section section;

        int     count;
        int     timing;

        long    free;
        long    modified;
        long    total;
        long    threads;
        
        //beim Einsprung per ShutdownHook wird das Beenden eingeleitet
        if (!this.equals(Service.service)) {
            
            if (Service.service != null)
                Service.destroy();
            
            while (Service.service != null)
                Service.sleep(250);

            return;
        }

        //die Konfigurationsdatei wird ermittelt
        file = new File("devwex.ini");

        //der Betriebsstatus wird gesetzt
        this.status = Service.RUN;

        //die Startzeit des Services wird gesetzt
        this.timing = System.currentTimeMillis();

        threads = free = total = count = 0;
        
        modified = file.lastModified();

        //ohne Server Instanzen wird der Service beendet
        //ausgenommen im Status INITIALIZE und RESTART
        while (this.server.size() > 0 || this.status == Service.RESTART) {

            //die Konfiguration wird ermittelt
            section = this.initialize.get("common");

            //der Interval fuer das CLEANUP wird ermittelt und berrechnet
            //als Standard werden 5 Sekunden verwendet
            try {timing = (Integer.parseInt(section.get("cleanup")) /250) -1;
            } catch (Throwable throwable) {
                timing = 19;
            }

            //bei Aktivitaeten wird die Bereinigung zurueckgestellt
            //und die Garbage Collection wird nur bei Bedarf angefordert
            if (++count > timing && (Thread.activeCount() < threads
                    || (Runtime.getRuntime().freeMemory()  /0x100000) < free
                    || (Runtime.getRuntime().totalMemory() /0x100000) < total)) {

                threads = Thread.activeCount();
                free    = Runtime.getRuntime().freeMemory()  /0x100000;
                total   = Runtime.getRuntime().totalMemory() /0x100000;

                System.gc();

                count = 0;
            }

            //die Option RELOAD wird ermittelt
            if (section.get("reload").toLowerCase().equals("on")
                    && modified != file.lastModified()) {

                modified = file.lastModified();

                //der Server wird durchgestartet
                Service.restart();
            }

            Service.sleep(250);
        }

        //Standardinformation wird ausgegeben
        Service.print("SERVICE STOPPED");
        
        Service.service = null;
    }
    
    /**
     *  Globale Behanldung von nicht behandelten Fehlern.
     *  @param thread    Thread
     *  @param throwable Throwable
     */
    public  void uncaughtException(Thread thread, Throwable throwable) {
        Service.print(throwable);
    }
}