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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
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
 * Service ist ein Container mit den ben&ouml;tigten Mechanismen, APIs und
 * Sequenzen zur Verwaltung und Ausf&uuml;hrung von Servern und Modulen.
 * 
 * <h2>Begriffe</h2>
 * 
 * <h3>Server</h3>
 * Server stellen den physischen Zugriff im Netzwerk f&uuml;r ein Protokoll an
 * einer Adresse und einem Port zur Verf&uuml;gung. Seanox Devwex bindet Server
 * &uuml;ber das Server-API (SAPI) ein. Mit dem API lassen sich auch bestehende
 * Server- und Netzwerk-Funktionalit&auml;ten &auml;ndern bzw. neue
 * bereitstellen.<br>
 * Das Server-API basiert auf der Implementierung vom Runnable-Interface. Die
 * Initialisierung, deren Reihenfolge mit der Reihenfolge der
 * Server-Konfigurationen in Konfigurationsdatei festgelegt wird, und die
 * Konfiguration erfolgt &uuml;ber den Konstruktor. Die erstellten
 * Server-Instanzen werden vom Laufzeit-Container (Service) mit der Methode
 * {@code Server.run()} gestartet und per {@code Service.destroy()} zum Beenden
 * aufgefordert. Der Aufruf beider Methoden ist asynchron.
 * Verz&ouml;gert sich das Beenden, wartet der Laufzeit-Container auf das Ende
 * aller registrierten Server-Instanzen. Optional wird die Implementierung der
 * Methode {@code Server.explain()} zur Abfrage allgemeiner Informationen, wie
 * Protokoll und Netzwerkverbindung, unterst&uuml;tzt.
 * 
 * <h3>Module</h3>
 * Die f&uuml;r Hintergrundaktivit&auml;ten gedachten Erweiterungen stellen nach
 * aussen hin keine direkten Funktionen bereit. Sie werden &uuml;ber die
 * Ausf&uuml;hrungsklasse identifiziert und beim Start bzw. Restart vom Service
 * oder nachtr&auml;glich in Servern und Modulen &uuml;ber den
 * Application-ClassLoader geladen, initialisiert und konfiguriert. Module sind
 * globale Erweiterungen, die einmal instanziert und dann allen Komponenten
 * bereitgestellt werden.<br>
 * Optional ist die Verwendung vom Context-ClassLoader aus dem Seanox Devwex SDK
 * m&ouml;glich, womit Module auch in mehreren unabh&auml;ngigen Instanzen mit
 * eigenem ClassLoader verwendet werden k&ouml;nnen.<br>
 * Das Module-API umfasst lediglich die Initialisierung und das Beenden von
 * Modulen. Die Initialisierung erfolgt &uuml;ber den Konstruktor initial mit
 * dem Start vom Laufzeit-Container (Service), dabei wird die Abfolge in der
 * Konfigurationsdatei mit der Reihenfolge der Module innerhalb der Sektion
 * {@code INITIALIZE} festgelegt, oder die Initialisierung erfolgt mit der
 * ersten Anforderung eines Moduls zur Laufzeit. Die Konfiguration wird einem
 * Module nur dann &uuml;bergeben, wenn es initial &uuml;ber die Sektion
 * {@code INITIALIZE} geladen wird. Die Aufforderung zum Beenden erfolgt
 * &uuml;ber die Methode {@code Module.destroy()}. Der Laufzeit-Container
 * &uuml;berwacht das Beenden nicht aktiv und verwirft die Module durch das
 * Entladen vom ClassLoader.Optional wird die Implementierung der Methode
 * {@code Module.explain()} zur Abfrage allgemeiner Informationen, wie
 * Hersteller und Version, unterst&uuml;tzt.
 * 
 * <h3>Arbeitsweise</h3>
 * Alle f&uuml;r die Initialisierung und den Betrieb erforderlichen Daten werden
 * aus der Konfigurationsdatei {@code devwex.ini} gelesen, die aus dem aktuellen
 * Arbeitsverzeichnis geladen wird. Beim Start, Neustart und Stop werden feste
 * Sequenzen zum Laden und Entladen von Servern und Modulen durchlaufen<br>
 * Im Betrieb &uuml;berwacht der Service Konfiguration, Server sowie Module und
 * steuert den Garbage Collector f&uuml;r eine schnellere Freigabe von
 * Ressourcen.
 * 
 * <h2>Sequenzen</h2>
 * Start, Neustart und Beenden der Server sowie das Laden, Anfordern und
 * Entladen von Modulen sind feste Abfolgen vom Laufzeit-Container.
 * 
 * <h3>Start</h3>
 * <ul>
 *   <li>
 *     Der Klassenpfad wird um alle Dateien der Verzeichnisse erweitert, die mit
 *     dem VM-Argument {@code -Dlibraries} angegeben wurden.
 *   </li>
 *   <li>
 *     Alle Module aus der Sektion {@code INITIALIZE} werden geladen und
 *     &uuml;ber den Konstruktor {@code Module(String options)} initialisiert
 *     und registriert.
 *   </li>
 *   <li>
 *     Alle Server werden ermittelt, indem nach Sektionen gesucht wird, die auf
 *     {@code INI} enden und zu denen eine Implementierung im Klassenpfad
 *     gefunden werden kann. Die gefundenen Server werden geladen, registriert
 *     und &uuml;ber den Konstruktor {@code Server(String name,
 *         Object initialize)} initialisiert. Dazu werden jedem Server der Name
 *     entsprechend der ermittelten Sektion sowie eine komplette Kopie der
 *     zentralen Konfiguration als Initialize-Objekt &uuml;bergeben. Nach
 *     erfolgreicher Initialisierung wird der Server als (Daemon)Thread
 *     gestartet und kann seine Arbeit in der Methode {@code Server.run()}
 *     aufnehmen.
 *   </li>
 * </ul>
 * 
 * <h3>Modulaufruf</h3>
 * <ul>
 *   <li>
 *     Ist das Modul noch nicht geladen, wird dies aus dem aktuellen
 *     Klassenpfad ermittelt, &uuml;ber {@code Module(String options)}
 *     initialisiert und registriert. Eine Konfiguration wird dabei nicht
 *     &uuml;bergeben, da f&uuml;r Module nur eine zentrale Konfiguration in der
 *     Sektion {@code INITIALIZE} vorgesehen ist.
 *   </li>
 *   <li>
 *     Ist das Modul bereits geladen, wird die aktuelle Instanz verwendet.
 *   </li>
 * </ul> 
 * 
 * <h3>Neustart</h3>
 * Die Sequenz entspricht der Kombination aus <i>Beenden</i> und <i>Start</i>.
 * <ul>
 *   <li>
 *     Alle registrierten Server-Instanzen werden &uuml;ber die Methode
 *     {@code Server.destroy()} zum Beenden aufgefordert.
 *   </li>
 *   <li>
 *     Alle registrierten Module werden &uuml;ber die Methode
 *     {@code Module.destroy()} zum Beenden aufgefordert.
 *   </li>
 *   <li>
 *     Das Einleiten vom Beenden der Server verl&auml;uft asynchron. Der
 *     Laufzeit-Container wartet auf das Ende aller registrierten Server.
 *   </li>
 *   <li>
 *     Alle Module und Server werden durch das Verwerfen vom aktuell verwendeten
 *     ClassLoader entladen.
 *   </li>
 *   <li>
 *     Der Klassenpfad wird um alle Dateien der Verzeichnisse erweitert, die mit
 *     dem VM-Argument {@code -Dlibraries} angegeben wurden.
 *   </li>
 *   <li>
 *     Alle Module aus der Sektion {@code INITIALIZE} werden geladen, und
 *     &uuml;ber den Konstruktor {@code Module(String options)} initialisiert
 *     und registriert.
 *   </li>
 *   <li>
 *     Alle Server werden ermittelt, indem nach Sektionen gesucht wird, die auf
 *     {@code INI} enden und zu denen eine Implementierung im Klassenpfad
 *     gefunden werden kann. Die gefundenen Server werden geladen, registriert
 *     und &uuml;ber den Konstruktor {@code Server(String name,
 *         Object initialize)} initialisiert. Dazu werden jedem Server der Name
 *     entsprechend der ermittelten Sektion sowie eine komplette Kopie der
 *     zentralen Konfiguration als Initialize-Objekt &uuml;bergeben. Nach
 *     erfolgreicher Initialisierung wird der Server als Thread gestartet und
 *     kann seine Arbeit in der Methode {@code Server.run()} aufnehmen.
 *   </li>
 * </ul>
 * 
 * <h3>Beenden</h3>
 * <ul>
 *   <li>
 *     Alle registrierten Server-Instanzen werden &uuml;ber die Methode
 *     {@code Server.destroy()} zum Beenden aufgefordert.
 *   </li>
 *   <li>
 *     Alle registrierten Module werden &uuml;ber die Methode
 *     {@code Module.destroy()} zum Beenden aufgefordert.
 *   </li>
 *   <li>
 *     Das Einleiten vom Beenden der Server verl&auml;uft asynchron. Der
 *     Laufzeit-Container wartet auf das Ende aller registrierten Server.
 *   </li>
 *   <li>
 *     Alle Module und Server werden durch das Verwerfen vom aktuell verwendeten
 *     ClassLoader entladen.
 *   </li>
 * </ul>
 *
 * @author  Seanox Software Solutions
 * @version 5.5.0 20220919
 */
public class Service implements Runnable, UncaughtExceptionHandler {

    // Configuration file
    private volatile File configuration;

    /** ClassLoader f&uuml;r geladene Ressourcen */
    private volatile ClassLoader loader;

    /** Konfiguration des Services */
    private volatile Initialize initialize;

    /** Betriebsstatus des Services */
    private volatile int status;

    /** Startzeit des Serices */
    private volatile long timing;

    /** Liste der eingerichteten Server */
    private final Vector servers;

    /** Liste der initialisierten Module */
    private final Hashtable modules;

    /** Referenz des generischen Services */
    private static volatile Service service;

    /** Konstante mit der aktuellen Version des Services */
    public static final String VERSION = "0.0.0 00000000";

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
        this.servers    = new Vector();
        this.modules    = new Hashtable();
    }

    /**
     * L&auml;dt die per Name angegebene Klasse &uuml;ber den servereigenen
     * ClassLoader. Ist dieser noch nicht eingerichtet wird {@code null}
     * zur&uuml;ckgegeben. Kann die Klasse nicht geladen werden, f&uuml;hrt der
     * Aufruf in diesem Fall zur Ausnahme {@code ClassNotFoundException}.
     * @param  name Name der Klasse
     * @return die &uuml;ber den servereigenen ClassLoader ermittelte Klasse,
     *     liegt dieser noch nicht vor oder wurde noch nicht eingerichtet wird
     *     {@code null} zur&uuml;ck gegeben
     * @throws ClassNotFoundException
     *     Wenn die Klasse nicht geladen werden kann.
     */
    public static Class load(String name)
            throws ClassNotFoundException {
        Service service = Service.service;
        if (service == null
                || name == null
                || name.trim().length() <= 0)
            return null;
        return service.loader.loadClass(name.trim());
    }

    /**
     * Fordert ein Modul an. Ist dieses noch nicht registriert, wird es vom
     * Service mit den optional &uuml;bergebenen Daten eingerichtet, wenn es
     * sich um einen Modul-Context handelt, sonst wird nur die Klasse als
     * Referenz vorgehalten. Bei der Initialisierung auftretende Fehler werden
     * nicht behandelt und weitergereicht.
     * @param  module  Modul-Klasse
     * @param  options optinale Daten zur Einrichtung
     * @return die Klasse deren vorhandene Instanz, sonst {@code null}
     * @throws Exception
     *     Bei Fehlern im Zusammenhang mit der Initialisierung.
     */
    public static Object load(Class module, String options)
            throws Exception {

        synchronized (Service.class) {

            Service service = Service.service;
            if (service == null
                    || module == null)
                return null;            
            
            // Das Modul wird aus der Modulliste ermittelt, ist dieses noch
            // nicht registriert, wird es neu initialisiert und eingetragen.
            // Wenn Module nicht richtig beendet werden koennen, bleiben diese
            // ebenfall erhalten und benutzen dann noch den alten ClassLoader.
            // Das Verhalten ist gewollt, damit kein Zombies erzeugt werden.
            
            Object object = service.modules.get(module);
            if (object != null)
                return object;
            
            Service.print(("SERVICE INITIATE MODULE ").concat(module.getName()));
            
            // Die Mindestimplementierung der Modul-API wird per Reflections
            // geprueft und erwartet einen passenden Konstruktor sowie eine
            // destroy-Methode.
            // Ein Interface ist wegen der Groesse nicht verfuegbar.
            
            try {
                module.getMethod("destroy");
                object = module.getConstructor(String.class);
            } catch (NoSuchMethodException exception) {
                throw new NoSuchMethodException("Invalid interface");
            }

            // die Instanz vom Modul wird eingerichtet und registriert 
            object = ((Constructor)object).newInstance(options);
            service.modules.put(module, object);
         
            return object;
        }
    }

    /**
     * Initiiert eine Sequenze beim Server.
     * Folgende Sequenzen Sequenzen werden unterst&uuml;tzt:<br>
     *     <dir>START</dir>
     * Ist der Serivce noch nicht eingerichtet, wird dieser initialisiert, der
     * Klassenfad erweitert, die Module geladen, die Server ermittelt und
     * gestartet sowie der Service gestartet.<br>
     *     <dir>RESTART</dir>
     * Der laufenden Service bleibt erhalten, alle Module und Server werden
     * beendet, der Klassenfad erweitert, die Module geladen (wenn sich diese
     * beenden liesen) und die Server neu initialisiert.<br>
     *     <dir>STOP</dir>
     * Der laufenden Service bleibt erhalten, alle Module und Server werden
     * beendet. Scheitert dieses erfolgt ein normaler Restart um einen stabielen
     * und kontrollierbaren Betriebszustand herbei zuf&uuml;hren.
     * Dazu wird auf Basis der letzten lauff&auml;higen Konfiguration der
     * Klassenfad erweitert, die Module geladen (wenn sich diese beenden liesen)
     * und die Server neu initialisiert. In diesem Fall wird der Betriebzustand
     * auf READY gesetzt und DESTROY verworfen.
     * @param  mode Betriebsmodus
     * @return {@code true} bei erfolgreicher Ausf&uuml;hrung
     */
    public static boolean initiate(int mode) {
        return Service.initiate(mode, null);
    }
        
    /**
     * Initiiert eine Sequenze beim Server.
     * Folgende Sequenzen Sequenzen werden unterst&uuml;tzt:<br>
     *     <dir>START</dir>
     * Ist der Serivce noch nicht eingerichtet, wird dieser initialisiert, der
     * Klassenfad erweitert, die Module geladen, die Server ermittelt und
     * gestartet sowie der Service gestartet.<br>
     *     <dir>RESTART</dir>
     * Der laufenden Service bleibt erhalten, alle Module und Server werden
     * beendet, der Klassenfad erweitert, die Module geladen (wenn sich diese
     * beenden liesen) und die Server neu initialisiert.<br>
     *     <dir>STOP</dir>
     * Der laufenden Service bleibt erhalten, alle Module und Server werden
     * beendet. Scheitert dieses erfolgt ein normaler Restart um einen stabielen
     * und kontrollierbaren Betriebszustand herbei zuf&uuml;hren.
     * Dazu wird auf Basis der letzten lauff&auml;higen Konfiguration der
     * Klassenfad erweitert, die Module geladen (wenn sich diese beenden liesen)
     * und die Server neu initialisiert. In diesem Fall wird der Betriebzustand
     * auf READY gesetzt und DESTROY verworfen.
     * @param  mode Betriebsmodus
     * @param  file optional configuration file
     * @return {@code true} bei erfolgreicher Ausf&uuml;hrung
     */
    public static boolean initiate(int mode, String file) {

        Class  source;
        Object object;
        String context;
        String scope;
        String string;

        synchronized (Service.class) {
            
            // die Zeitpunkt der Initialisierung wird ermittelt
            long timing = System.currentTimeMillis();

            // wenn noch nicht initialisiert, wird der Service als Singleton mit
            // globalem Exception Handler eingerichtet
            if (Service.service == null) {
                Service.service = new Service();
                Thread.setDefaultUncaughtExceptionHandler(Service.service);
            }
            
            Service service = Service.service;

            if (mode == Service.STOP
                    || mode == Service.RESTART) {

                if (service.status != Service.RUN)
                    return false;
                
                Service.print(("SERVICE INITIATE ").concat(mode == Service.RESTART ? "RESTART" : "STOP"));
                
                // alle registrierten Server werden zum Beenden aufgefordert
                Enumeration enumeration = service.servers.elements();
                while (enumeration.hasMoreElements()) {
                    object = ((Object[])enumeration.nextElement())[0];
                    try {object.getClass().getMethod("destroy").invoke(object);
                    } catch (Throwable throwable) {
                        Service.print(throwable);
                    }
                }

                // alle registrierten Module werden zum Beenden aufgefordert,
                // dabei werden alle Modul aus der Modulliste entfernt
                enumeration = service.modules.keys();
                while (enumeration.hasMoreElements()) {
                    object = service.modules.remove(enumeration.nextElement());
                    try {object.getClass().getMethod("destroy").invoke(object);
                    } catch (Throwable throwable) {
                        Service.print(throwable);
                    }
                }
                
                while (service.servers.size() > 0) {
                    
                    // Alle aktiven Server werden ermittelt und durchsucht.
                    // Bei noch laufenden Servern wird gewartet bis diese enden.
                    // Inaktive Server werden aus der Serverliste entfernt
                    enumeration = ((Vector)service.servers.clone()).elements();
                    while (enumeration.hasMoreElements()) {
                        object = enumeration.nextElement();
                        Thread thread = (Thread)((Object[])object)[1];
                        if (thread == null
                                || !thread.isAlive())
                            service.servers.remove(object);
                    }

                    try {Thread.sleep(25);
                    } catch (Throwable throwable) {
                        break;
                    }
                }
                
                service.status = mode;
                if (service.status == Service.STOP)
                    return true;
            }

            if (mode == Service.START
                    || mode == Service.RESTART) {
                
                if (service.status == Service.RUN)
                    return false;
                
                if (mode == Service.START) {
                    Service.print("SERVICE INITIATE START");
                    if (file != null)
                        file = file.trim();
                    if (file == null
                            || file.length() <= 0)
                        file = "devwex.ini";
                    service.configuration = new File(file);
                }

                string = "SERVICE INITIATE RESOURCES";
                
                // list of libraries is established
                Vector libraries = new Vector();

                // paths of the classpath are determined
                StringTokenizer tokenizer = new StringTokenizer(Section.parse("libraries [?]", true).get("libraries"), File.pathSeparator);
                while (tokenizer.hasMoreTokens()) {

                    // In the first step, files and directories are considered.
                    // Directories are processed as a file list. In the second
                    // step only files are considered, further subdirectories
                    // are ignored.
                    
                    File[] files = new File[] {new File(tokenizer.nextToken())};
                    if (files[0].isDirectory())
                        files = files[0].listFiles();
                    if (files == null)
                        continue;
                    for (int loop = 0; loop < files.length; loop++) {
                        if (!files[loop].isFile())
                            continue;
                        try {libraries.add(files[loop].getCanonicalFile());
                        } catch (Throwable throwable) {
                            continue;
                        }
                        string = string.concat("\r\n").concat(files[loop].getPath());
                    }
                }
                
                Service.print(string);
                
                // ClassLoader is determined and (re) established
                ClassLoader loader = Service.class.getClassLoader();
                service.loader = new Loader(loader, libraries);
                loader = service.loader;
                
                // current configuration is loaded
                try {service.initialize = Initialize.parse(new String(Files.readAllBytes(service.configuration.toPath())), true);
                } catch (Throwable throwable) {
                    Service.print("SERVICE CONFIGURATION FAILED");
                    Service.print(throwable);
                }
                
                Service.print("SERVICE INITIATE MODULES");                

                // base options are determined
                Section section = service.initialize.get("initialize");
                Enumeration initialize = section.elements();
                while (initialize.hasMoreElements()) {

                    // resource is determined with parameters/options if necessary
                    context = (String)initialize.nextElement();
                    scope   = section.get(context);
                    string  = scope.replaceAll("^([^\\s\\[]*)\\s*(.*)$", "$2");
                    scope   = scope.replaceAll("^([^\\s\\[]*)\\s*(.*)$", "$1");
                    
                    // module is loaded and initially established
                    try {Service.load(loader.loadClass(scope), string);
                    } catch (Throwable throwable) {
                        if (throwable instanceof ClassNotFoundException
                                && string.matches("(\\s*\\[\\s*\\*\\s*\\]\\s*)+"))
                            continue;
                        Service.print(throwable);
                    }
                }
                
                // configured servers are detected
                Enumeration enumeration = service.initialize.elements();
                while (enumeration.hasMoreElements()) {

                    // server scope is determined for the configuration
                    context = (String)enumeration.nextElement();
                    if (!context.matches("^(?i)(?!virtual\\s*:.*$)([^:]+)(?=:).*:ini$"))
                        continue;
                    context = context.replaceAll(":[^:]+$", "").trim();
                    
                    object = null;

                    try {

                        Service.print(String.format("SERVICE INITIATE %s", context));
                        
                        // server class is loaded
                        scope = service.initialize.get(context.concat(":ini")).get("scope", "com.seanox.devwex");
                        scope = scope.replaceAll("\\s*>.*$", "");
                        try {source = loader.loadClass(scope);
                        } catch (ClassNotFoundException exception1) {
                            string = context.replaceAll("\\s*:.*$", "");
                            string = string.substring(0, 1).toUpperCase().concat(string.substring(1).toLowerCase());
                            string = scope.concat(".").concat(string);                        
                            try {source = loader.loadClass(string);
                            } catch (ClassNotFoundException exception2) {
                                throw exception1;
                            }
                        }
                        
                        // Minimum implementation of the server API is verified
                        // via reflections and expects a matching constructor
                        // and a destroy method. An interface is not available
                        // because of the size the binary.

                        source.getMethod("destroy");
                        try {object = source.getConstructor(String.class, Initialize.class);
                        } catch (NoSuchMethodException exception) {
                            object = source.getConstructor(String.class, Object.class);
                        }
                            
                        object = ((Constructor)object).newInstance(context, service.initialize.clone());
                        
                        // Implementation of Runnable is optional. Servers can
                        // also be initialized by other modules, e.g. if the
                        // servers should use own ClassLoader. With which also a
                        // server module can be called. Server module have the
                        // same constructor without implementing Runnable.

                        // With implementation of Runnable, the thread is
                        // established and started as a daemon.

                        Thread thread = null;
                        if (Runnable.class.isAssignableFrom(source)) {
                            thread = new Thread((Runnable)object);
                            thread.setDaemon(true);
                            thread.start();
                        }

                        // server is registered with thread
                        service.servers.add(new Object[] {object, thread});

                    } catch (Throwable throwable) {
                        
                        if (throwable instanceof NoSuchMethodException)
                            throwable = new NoSuchMethodException("Invalid interface");
                        
                        Service.print(throwable);

                        // termination of the server is triggered
                        if (object != null
                                && !(object instanceof Constructor)) {
                            try {object.getClass().getMethod("destroy").invoke(object);
                            } catch (Throwable error) {
                                Service.print(error);
                            }
                        }
                    }

                    try {Thread.sleep(25);
                    } catch (Throwable throwable) {
                        break;
                    }
                }
                
                // if no server was found, an information is output
                if (service.servers.size() <= 0)
                    Service.print("SERVICE NOT AVAILABLE");

                // duration of the start process is determined
                if (service.servers.size() > 0)
                    Service.print(String.format("SERVICE %sSTARTED (%s SEC)",
                            mode == Service.RESTART ? "RE" : "",
                            String.valueOf((System.currentTimeMillis() -timing) /1000)));

                // at restart, the status READY is set and the method is exited,
                // subsequently mode can then only be START
                if (mode == Service.RESTART) {
                    service.status = Service.RUN;
                    return true;
                }

                // when starting without established servers, the service ends
                if (service.servers.size() <= 0) {
                    service.status = Service.STOP;
                    Service.service = null;
                    return false;
                }

                // status and ShutdownHook are set during the startup
                Runtime.getRuntime().addShutdownHook(new Thread(new Service()));

                // thread is created and started,
                // if this fails the service will be reset
                try {new Thread(service).start();
                } catch (Throwable throwable) {
                    Service.print("SERVICE START FAILED");                               
                    Service.print(throwable);
                    Service.initiate(Service.STOP);
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
     * Return of the current and detailed operating status.
     * @return the current and detailed operating status
     */
    public static String details() {
        
        Object      caption;
        
        String result = "VERS: 0.0.0 00000000\r\n";
        result = result.concat(String.format("TIME: %tF %<tT\r\n", new Date()));
     
        synchronized (Service.class) {

            Service service = Service.service;
            if (service != null) {

                result = result.concat(String.format("TIUP: %tF %<tT\r\n", new Date(service.timing)));
                
                // all registered modules are determined
                // module identifier is determined via caption
                Enumeration enumeration = service.modules.elements();
                while (enumeration.hasMoreElements()) {
                    Object object = enumeration.nextElement();
                    try {caption = object.getClass().getMethod("explain").invoke(object);
                    } catch (Throwable throwable) {
                        caption = null;
                    }
                    if (caption == null)
                        caption = object.getClass().getName();
                    result = result.concat(String.format("XAPI: %s\r\n", caption));
                }
                
                // all registered servers are detected
                // server identifier is determined via explain
                enumeration = service.servers.elements();
                while (enumeration.hasMoreElements()) {
                    Object object = ((Object[])enumeration.nextElement())[0];
                    try {caption = object.getClass().getMethod("explain").invoke(object);
                    } catch (Throwable throwable) {
                        caption = null;
                    }
                    if (caption == null)
                        caption = object.getClass().getName();                      
                    result = result.concat(String.format("SAPI: %s\r\n", caption));
                }
            }
        }
        
        return result;
    }
    
    /**
     * Restarts all established server instances.
     * @return {@code true} when the restart is successful
     */
    public static boolean restart() {
        return Service.initiate(Service.RESTART);
    }

    /**
     * Terminates the service with all server instances.
     * @return {@code true} when successful
     */
    public static boolean destroy() {
        return Service.initiate(Service.STOP);
    }

    /**
     * Main application entry point.<br>
     * After startup, only: RESTART | STATUS | STOP local are available.<br>
     * comparable to: {@link #restart()}, {@link #details()}, {@link #destroy()}
     * @param options Start arguments
     */
    public static void main(String[] options) {

        if (options == null
                || options.length < 1)
            options = new String[] {null, null};
        else if (options.length < 2)
            options = new String[] {options[0], null};

        // command is determined
        String string = "";
        if (options[0] != null)
            string = options[0].trim().toLowerCase();
        
        if (Service.service != null) {
            if (string.matches("status"))
                Service.print(("SERVICE STATUS\r\n").concat(Service.details()));
            if (string.matches("restart"))
                Service.restart();
            if (string.matches("stop"))
                Service.destroy();
            return;
        }
        
        // output information is composed and output
        Service.print("Seanox Devwex [Version 0.0.0 00000000]", true);
        Service.print("Copyright (C) 0000 Seanox Software Solutions", true);
        Service.print("Advanced Server Development", true);
        Service.print("\r\n", true);
        
        // unknown commands output the command list
        if (!string.matches("^start|restart|status|stop$")) {
            Service.print(("Usage: devwex [start] [ini file]")
                    .concat("\r\n       devwex [restart|status|stop] [address:port]"), true);
            return;
        }

        // START - starten der Serverdienste
        if (string.matches("start")) {
            Service.initiate(Service.START, options[1]);
            return;
        }

        // RESTART | STATUS | STOP - The command is sent to the remote server
        // and the response is output. For configuration the program argument is
        // used and otherwise the default value 127.0.0.1:25000.
        
        try {
            String address = null;
            String port = "25000";
            if (options[1] != null) {
                String pattern = "^\\s*(\\w(?:[\\w.:-]*?\\w)?)(?::(\\d{1,5}))?\\s*$";
                if (!options[1].matches(pattern)) {
                    Service.print("INVALID REMOTE DESTINATION", true);
                    return;
                }
                address = options[1].replaceAll(pattern, "$1");
                port = options[1].replaceAll(pattern, "$2");
                if (port.length() <= 0)
                    port = "25000";
            }
            string = new String(Remote.call(address, Integer.parseInt(port), string));
            if (string.length() <= 0)
                string = "REMOTE ACCESS NOT AVAILABLE";
            String[] lines = string.trim().split("[\r\n]+");
            for (int loop = 0; loop < lines.length; loop++)
                Service.print(lines[loop], true);
        } catch (Throwable throwable) {
            Service.print("REMOTE ACCESS FAILED", true);
            Service.print(throwable.getMessage(), true);
        }
    }
    
    /**
     * Writes the string value of an object line by line, prefixed with a
     * timestamp, to the standard IO. The content of the output is determined by
     * {@code Object.toString()}. For error objects the StackTrace is logged.
     * Line breaks in the content are summarized and output indented.
     * @param object Object that is to be output
     */
    public static void print(Object object) {
        Service.print(object, false);
    }
    
    /**
     * Writes the string value of an object line by line, prefixed with a
     * timestamp, to the standard IO. The content of the output is determined by
     * {@code Object.toString()}. For error objects the StackTrace is logged.
     * Line breaks in the content are summarized and output indented.
     * @param object Object that is to be output
     * @param plain {@code false} Suppresses the output of the timestamp, as
     *     well as the optimization.
     */
    public static void print(Object object, boolean plain) {
        
        Throwable throwable = null;
        if (object instanceof Throwable) {
            throwable = ((Throwable)object);
            while (throwable instanceof InvocationTargetException
                    && (object = ((InvocationTargetException)throwable).getTargetException()) != null)
                throwable = ((Throwable)object);
            object = new StringWriter();
            throwable.printStackTrace(new PrintWriter((StringWriter)object));
        }

        String string = String.valueOf(object);
        synchronized (System.out) {
            if (object == null
                    || (string.matches("\\s*")
                            && !string.matches("\\s*\\R\\s*")))
                return;
            string = string.trim();
            if (!plain) {
                // timestamp is determined
                // if necessary, following lines are indented
                string = String.format("%tF %<tT %s", new Date(), string);
                if (throwable == null
                        && string.matches("(?s).*\\R\\S.*"))
                    string = string.replaceAll("(\\R+)", "$1\t");
            }
            System.out.println(string);
        }        
    }
    
    /**
     * Returns the current operating status.
     * @return the current operating status
     * @see    {@link #START}, {@link #RUN}, {@link #RESTART}, {@link #STOP},
     *         {@link #UNKNOWN} 
     */
    public static int status() {

        // The query of the operating status is asynchronous, which does not
        // ensure that the service is available or running.
        Service service = Service.service;
        if (service != null)
            return service.status;
        return Service.UNKNOWN; 
    }

    @Override
    public void run() {

        // When called via ShutdownHook, the shutdown is initiated
        if (!this.equals(Service.service)) {
            if (Service.service != null)
                Service.destroy();
            while (Service.service != null)
                try {Thread.sleep(250);
                } catch (Throwable throwable) {
                    break;
                }
            return;
        }
        
        // Start time of the service is set
        this.timing = System.currentTimeMillis();

        // Operating status is set
        this.status = Service.RUN;
        
        long modified = this.configuration.lastModified();

        int count = 0;
        int delta = 0;
        int total = 0;
        
        while (this.status < Service.STOP) {

            Section section = this.initialize.get("common");
            if (section.get("cleanup").toLowerCase().equals("on"))
                count = Thread.activeCount();
            
            if (total != count)
                delta = delta << 1;
            if (total > count)
                delta = delta | 1;

            if ((delta & 0xFF) == 0xFF) 
                System.gc();
            if ((delta & 0xFF) == 0xFF)
                delta = delta << 1;

            delta = delta & 0xFF;
            total = count;

            if (section.get("reload").toLowerCase().equals("on")
                    && modified != this.configuration.lastModified()) {
                modified = this.configuration.lastModified();
                Service.restart();
            }

            try {Thread.sleep(250);
            } catch (Throwable throwable) {
                Service.destroy();
            }
        }
        
        Service.print("SERVICE STOPPED");
        Service.service = null;
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Service.print(throwable);
    }
}