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
 *     und &uuml;ber den Konstruktor {@codeServer(String name,
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
 * Service 5.3.0 20200621<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 5.3.0 20200621
 */
public class Service implements Runnable, UncaughtExceptionHandler {

    /** ClassLoader f&uuml;r geladene Ressourcen */
    private volatile ClassLoader loader;

    /** Konfiguration des Services */
    private volatile Initialize initialize;

    /** Liste der eingerichteten Server */
    private volatile Vector servers;
    
    /** Liste der initialisierten Module */
    private volatile Hashtable modules;

    /** Betriebsstatus des Services */
    private volatile int status;

    /** Startzeit des Serices */
    private volatile long timing;

    /** Referenz des generischen Services */
    private static volatile Service service;

    /** Konstante mit der aktuellen Version des Services */
    public static final String VERSION = "#[ant:release-version] #[ant:release-date]";

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
    public static Class load(String name) throws ClassNotFoundException {
        
        Service service;
        service = Service.service;
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
    public static Object load(Class module, String options) throws Exception {

        Object  object;
        Service service;
        
        synchronized (Service.class) {
            
            service = Service.service;
            if (service == null
                    || module == null)
                return null;            
            
            //Das Modul wird aus der Modulliste ermittelt, ist dieses noch nicht
            //registriert, wird es neu initialisiert und eingetragen.
            //Wenn Module nicht richtig beendet werden koennen, bleiben diese
            //ebenfall erhalten und benutzen dann noch den alten ClassLoader.
            //Das Verhalten ist gewollt, damit kein Zombies erzeugt werden.
            
            object = service.modules.get(module);
            if (object != null)
                return object;
            
            Service.print(("SERVICE INITIATE MODULE ").concat(module.getName()));
            
            //Die Mindestimplementierung der Modul-API wird per Reflections
            //geprueft und erwartet einen passenden Konstruktor sowie eine
            //destroy-Methode.
            //Ein Interface ist wegen der Groesse nicht verfuegbar.
            
            try {
                module.getMethod("destroy", new Class[0]);
                object = module.getConstructor(new Class[] {String.class});
            } catch (NoSuchMethodException exception) {
                throw new NoSuchMethodException("Invalid interface");
            }

            //die Instanz vom Modul wird eingerichtet und registriert 
            object = ((Constructor)object).newInstance(new Object[] {options});
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

        ClassLoader     loader;
        Class           source;
        Enumeration     enumeration;
        Object          object;
        Section         section;
        Service         service;
        StringTokenizer tokenizer;
        String          context;
        String          scope;
        String          string;
        Thread          thread;
        Vector          libraries;

        File[]          files;

        double          timing;
        int             loop;
        
        synchronized (Service.class) {
            
            //die Zeitpunkt der Initialisierung wird ermittelt
            timing = System.currentTimeMillis();

            //wenn noch nicht initialisiert, wird der Service als Singleton mit
            //globalem Exception Handler eingerichtet
            if (Service.service == null) {
                Service.service = new Service();
                Thread.setDefaultUncaughtExceptionHandler(Service.service);
            }
            
            service = Service.service;

            if (mode == Service.STOP
                    || mode == Service.RESTART) {

                if (service.status != Service.RUN)
                    return false;
                
                Service.print(("SERVICE INITIATE ").concat(mode == Service.RESTART ? "RESTART" : "STOP"));
                
                //alle registrierten Server werden zum Beenden aufgefordert
                enumeration = service.servers.elements();
                while (enumeration.hasMoreElements()) {
                    object = ((Object[])enumeration.nextElement())[0];
                    try {object.getClass().getMethod("destroy", new Class[0]).invoke(object, new Object[0]);
                    } catch (Throwable throwable) {
                        Service.print(throwable);
                    }
                }

                //alle registrierten Module werden zum Beenden aufgefordert,
                //dabei werden alle Modul aus der Modulliste entfernt
                enumeration = service.modules.keys();
                while (enumeration.hasMoreElements()) {
                    object = service.modules.remove(enumeration.nextElement());
                    try {object.getClass().getMethod("destroy", new Class[0]).invoke(object, new Object[0]);
                    } catch (Throwable throwable) {
                        Service.print(throwable);
                    }
                }
                
                while (service.servers.size() > 0) {
                    
                    //Alle aktiven Server werden ermittelt und durchsucht.
                    //Bei noch laufenden Servern wird gewartet bis diese enden.
                    //Inaktive Server werden aus der Serverliste entfernt
                    enumeration = ((Vector)service.servers.clone()).elements();
                    while (enumeration.hasMoreElements()) {
                        object = enumeration.nextElement();
                        thread = (Thread)((Object[])object)[1];
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
                
                if (mode == Service.START)
                    Service.print("SERVICE INITIATE START");

                string = "SERVICE INITIATE RESOURCES";
                
                //die Liste der Bibliotheken wird eingerichtet
                libraries = new Vector();

                //die Pfade des Klassenpfads werden ermittelt
                tokenizer = new StringTokenizer(Section.parse("libraries [?]", true).get("libraries"), File.pathSeparator);
                while (tokenizer.hasMoreTokens()) {

                    //In der ersten Ebene werden Dateien und Verzeichnisse
                    //beruecksichtigt. Verzeichnisse werden dabei als
                    //Dateiliste verarbeitet. In der zweiten Ebene werden nur
                    //noch Dateien beruecksichtigt, weitere Unterverzeichnisse
                    //werden ignoriert.

                    files = new File[] {new File(tokenizer.nextToken())};
                    if (files[0].isDirectory())
                        files = files[0].listFiles();
                    if (files == null)
                        continue;
                    for (loop = 0; loop < files.length; loop++) {
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
                
                //der ClassLoader wird ermittelt
                loader = Service.class.getClassLoader();

                //der ClassLoader wird (neu) eingerichtet
                loader = service.loader = new Loader(loader, libraries);
                
                //die aktuelle Konfiguration wird geladen
                try {service.initialize = Initialize.parse(new String(Files.readAllBytes(new File("devwex.ini").toPath())), true);
                } catch (Throwable throwable) {
                    Service.print("SERVICE CONFIGURATION FAILED");
                    Service.print(throwable);
                }
                
                //die Basisoptionen werden ermittelt
                section = service.initialize.get("initialize");

                Service.print("SERVICE INITIATE MODULES");                
                
                //alle Parameter werden ermittelt
                enumeration = section.elements();
                while (enumeration.hasMoreElements()) {

                    //die Ressource wird ggf. mit Parametern/Optionen ermittelt
                    context = (String)enumeration.nextElement();
                    scope   = section.get(context);
                    string  = scope.replaceAll("^([^\\s\\[]*)\\s*(.*)$", "$2");
                    scope   = scope.replaceAll("^([^\\s\\[]*)\\s*(.*)$", "$1");
                    
                    //das Modul wird geladen und initial eingerichtet
                    try {Service.load(loader.loadClass(scope), string);
                    } catch (Throwable throwable) {
                        if (throwable instanceof ClassNotFoundException
                                && string.matches("(\\s*\\[\\s*\\*\\s*\\]\\s*)+"))
                            continue;
                        Service.print(throwable);
                    }
                }
                
                //die Server werden aus der Konfiguration ermittelt
                enumeration = service.initialize.elements();
                while (enumeration.hasMoreElements()) {

                    //der Server(Scope) wird aus der Basiskonfiguration ermittelt
                    context = (String)enumeration.nextElement();
                    if (!context.matches("^(?i)(?!virtual\\s*:.*$)([^:]+)(?=:).*:ini$"))
                        continue;
                    
                    object = null;

                    try {

                        Service.print(String.format("SERVICE INITIATE %s", new Object[] {context.replaceAll(":[^:]+$", "").trim()}));
                        
                        //die Server Klasse wird geladen
                        scope = service.initialize.get(context).get("scope", "com.seanox.devwex");
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
                        
                        //Die Mindestimplementierung der Server-API wird per
                        //Reflections geprueft und erwartet einen passenden
                        //Konstruktor sowie eine destroy-Methode.
                        //Ein Interface ist wegen der Groesse nicht verfuegbar.

                        try {
                            source.getMethod("destroy", new Class[0]);
                            object = source.getConstructor(new Class[] {String.class, Object.class});
                        } catch (NoSuchMethodException exception) {
                            throw new NoSuchMethodException("Invalid interface");
                        }
                        
                        object = ((Constructor)object).newInstance(new Object[] {context, service.initialize.clone()});
                        
                        //Die Implementierung von Runnable ist optional.
                        //Server koennen auch durch andere Module initialisiert
                        //werden, z.B. wenn die Server einen eigenen ClassLoader
                        //verwenden sollen. Womit hier auch ein Server-Modul
                        //aufgerufen werden kann. Server-Modul haben den
                        //gleichen Konstruktor ohne Runnable zu implementieren.
                        
                        thread = null;
                        if (Runnable.class.isAssignableFrom(source)) {
                            
                            //Mit Implementierung vom Runnable-Interface wird
                            //der Thread als Daemon eingerichtet und gestartet. 
                            thread = new Thread((Runnable)object);
                            thread.setDaemon(true);
                            thread.start();
                        }

                        //der Server wird mit Thread registriert
                        service.servers.add(new Object[] {object, thread});

                    } catch (Throwable throwable) {

                        Service.print(throwable);

                        //das Beenden des Servers wird angestossen
                        if (object != null
                                && !(object instanceof Constructor)) {
                            try {object.getClass().getMethod("destroy", new Class[0]).invoke(object, new Object[0]);
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
                
                //wurde kein Server gefunden, wird eine Information ausgegeben
                if (service.servers.size() <= 0)
                    Service.print("SERVICE NOT AVAILABLE");

                //die Dauer des Startvorgangs wird ermittelt
                timing = (System.currentTimeMillis() -timing) /1000;

                if (service.servers.size() > 0)
                    Service.print(String.format("SERVICE %SSTARTED (%S SEC)",
                            new Object[] {mode == Service.RESTART ? "RE" : "", String.valueOf(timing)}));

                //beim Restart wird Status READY gesetzt und die Methode verlassen
                if (mode == Service.RESTART) {
                    service.status = Service.RUN;
                    return true;
                }
                
                //beim Start wird ohne eingerichtete Server, der Service beendet
                if (mode == Service.START
                        && service.servers.size() <= 0) {
                    service.status = Service.STOP;
                    Service.service = null;
                    return false;
                }
                
                //im Startprozess wird Status und ShutdownHook gesetzt
                if (mode == Service.START)
                    Runtime.getRuntime().addShutdownHook(new Thread(new Service()));

                //der Thread wird eingerichtet und gestartet,
                //scheitert diese wird der Service zurueckgesetzt
                try {new Thread(service).start();
                } catch (Throwable throwable) {

                    Service.print("SERVICE START FAILED");                               
                    Service.print(throwable);
                    
                    Service.initiate(Service.STOP);
                    
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
     * R&uuml;ckgabe des aktuellen und detaillierten Betriebsstatus.
     * @return der aktuelle und detaillierte Betriebsstatus
     */
    public static String details() {
        
        Enumeration enumeration;
        Object      object;
        Object      caption;
        Service     service;
        String      result;
        
        result = "VERS: #[ant:release-version] #[ant:release-date]\r\n";
        result = result.concat(String.format("TIME: %tF %<tT\r\n", new Object[] {new Date()}));
     
        synchronized (Service.class) {

            service = Service.service;

            if (service != null) {

                result = result.concat(String.format("TIUP: %tF %<tT\r\n", new Object[] {new Date(service.timing)}));
                
                //alle registrierten Module werden ermittelt
                enumeration = service.modules.elements();
                while (enumeration.hasMoreElements()) {
                    
                    //die Modul-Instanzen werden einzeln ermittelt
                    object = enumeration.nextElement();
                    
                    //die Modulkennung wird ueber Caption ermittelt
                    try {caption = object.getClass().getMethod("explain", new Class[0]).invoke(object, new Object[0]);
                    } catch (Throwable throwable) {
                        caption = null;
                    }
                    
                    if (caption == null)
                        caption = object.getClass().getName();

                    //die Ausgabe wird zusammengesetzt
                    result = result.concat(String.format("XAPI: %s\r\n", new Object[] {caption}));
                }
                
                //alle registrierten Server werden ermittelt
                enumeration = service.servers.elements();
                while (enumeration.hasMoreElements()) {

                    //die Server-Instanzen werden einzeln ermittelt
                    object = ((Object[])enumeration.nextElement())[0];

                    //die Serverkennung wird ueber Caption ermittelt
                    try {caption = object.getClass().getMethod("explain", new Class[0]).invoke(object, new Object[0]);
                    } catch (Throwable throwable) {
                        caption = null;
                    }

                    if (caption == null)
                        caption = object.getClass().getName();                      

                    //die Ausgabe wird zusammengesetzt
                    result = result.concat(String.format("SAPI: %s\r\n", new Object[] {caption}));
                }
            }
        }
        
        return result;
    }
    
    /**
     * Startet alle eingerichteten Server Instanzen neu.
     * @return {@code true} bei erfolgreichem Neustart
     */
    public static boolean restart() {
        return Service.initiate(Service.RESTART);
    }

    /**
     * Beendet den Service mit allen Server Instanzen.
     * @return {@code true} bei erfolgreicher Ausf&uuml;hrung
     */
    public static boolean destroy() {
        return Service.initiate(Service.STOP);
    }

    /**
     * Haupteinsprung in die Anwendung.
     * @param options Startargumente
     */
    public static void main(String options[]) {
        
        String address;
        String port;        
        String string;
        
        //Ausgabeinformation wird zusammen gestellt und ausgegeben
        Service.print("Seanox Devwex [Version #[ant:release-version] #[ant:release-date]]", false);
        Service.print("Copyright (C) #[ant:release-year] Seanox Software Solutions", false);
        Service.print("Advanced Server Development", false);
        Service.print("\r\n", false);
        
        if (options == null)
            options = new String[0];

        //das Kommando wird ermittelt
        string = options.length > 0 ? options[0].toLowerCase().trim() : "";
        
        //START - starten der Serverdienste
        if (string.matches("start")) {
            Service.initiate(Service.START);
            return;
        }

        //bei unbekannten Kommandos wird die Kommandoliste ausgegeben
        if (!string.matches("start|restart|status|stop")) {
            Service.print("Usage: devwex [start|restart|status|stop] [address:port]", false);
            return;
        }
        
        //RESTART | STATUS | STOP - die Kommandos werden an den Remote-Server
        //gesendet und der Response als Result ausgegeben
        
        try {
            
            //fuer die Konfiguration wird der Standardwert (127.0.0.1:25000)
            //oder ein alternatives Programmargument verwendet
            address = options.length > 1 ? options[1] : null;
            address = address == null ? "" : address.trim();
            
            port = address.replaceAll("^(.*?)(?::(\\d+)){0,1}$", "$2").trim();
            if (port.length() <= 0)
                port = "25000";            
            address = address.replaceAll("^(.*?)(?::(\\d+)){0,1}$", "$1").trim();
            if (address.length() <= 0)
                address = "127.0.0.1";
            
            //der Remoteausfruf wird ausgefuehrt und ausgegeben
            string = new String(Remote.call(address, Integer.parseInt(port), string));
            if (string.length() <= 0)
                string = "REMOTE ACCESS NOT AVAILABLE";
            System.out.println(string.trim());
            
        } catch (Throwable throwable) {
            
            System.out.println("REMOTE ACCESS FAILED");
            System.out.println(throwable.getMessage());
        }        
    }
    
    /**
     * Protokolliert das &uuml;bergebene Objekt zeilenweise und mit
     * vorangestelltem Zeitstempel in den Standard IO. Zur Ermittlung des
     * Protokolltexts wird {@code Object.toString()} vom &uuml;bergebenen Objekt
     * verwendet. Bei der &uuml;bergabe von Fehlerobjekten wird der StackTrace
     * protokolliert.
     * @param object Objekt mit dem Protokolleintrag
     */
    public static void print(Object object) {
        Service.print(object, true);
    }
    
    /**
     * Protokolliert das &uuml;bergebene Objekt zeilenweise und mit
     * vorangestelltem Zeitstempel in den Standard IO. Zur Ermittlung des
     * Protokolltexts wird {@code Object.toString()} vom &uuml;bergebenen Objekt
     * verwendet. Bei der &uuml;bergabe von Fehlerobjekten wird der StackTrace
     * protokolliert.
     * Leere Informationen werden ignoriert.
     * Die automatische Einr&uuml;ckung wird bei Exceptions ausgesetzt.
     * @param object Objekt mit dem Protokolleintrag
     * @param strict {@code false} Unterdr&uuuml;cht das Voranstellen vom
     *     Zeitstempel und es kann mit {@code \r\n} (Anzahl und Zeichenfolge
     *     wird ignoriert) eine Leerzeilen ausgegeben werden   
     */
    public static void print(Object object, boolean strict) {
        
        String    string;
        Throwable throwable;
        
        throwable = null;
        
        if (object instanceof Throwable) {

            throwable = ((Throwable)object);
            while (throwable instanceof InvocationTargetException
                    && (object = ((InvocationTargetException)throwable).getTargetException()) != null)
                throwable = ((Throwable)object);

            object = new StringWriter();
            throwable.printStackTrace(new PrintWriter((StringWriter)object));
        }
        
        string = String.valueOf(object);

        synchronized (System.out) {

            if (strict || !string.matches("[\r\n]+")) {

                string = string.trim();
                if (object == null
                        || string.length() <= 0)
                    return;

                //der Zeitstempel wird ermittelt
                //ggf. wird eine Einrueckung fuer die Folgezeilen eingefuegt
                string = strict ? String.format("%tF %<tT %s", new Object[] {new Date(), string}) : string;
                if (throwable == null
                        && string.matches("(?s).*[\r\n][^\\s].*"))
                    string = string.replaceAll("([\r\n]+)", "$1\t");

                //der Inhalt wird ausgegeben
                System.out.println(string.trim());

            } else System.out.println();
        }        
    }
    
    /**
     * R&uuml;ckgabe vom aktuellen Betriebsstatus.
     * @return der aktuelle Betriebsstatus
     * @see    {@link #START}, {@link #RUN}, {@link #RESTART}, {@link #STOP},
     *         {@link #UNKNOWN} 
     */
    public static int status() {
        
        Service service;
        
        service = Service.service;

        //die Abfrage des Betriebsstatus ist asynchron moeglich womit nicht
        //sichergestellt werden kann, das der Service verfuegbar ist
        return service != null ? service.status : Service.UNKNOWN; 
    }

    /**
     * Stellt den Einsprung f&uuml;r den Thread zur Verf&uuml;gung.
     * Der Thread kontrolliert die Server und steuert den Garbage Collector.
     */
    @Override
    public void run() {

        File    file; 
        Section section;

        int     count;
        int     delta;
        int     total;
        
        long    modified;
        
        //beim Einsprung per ShutdownHook wird das Beenden eingeleitet
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
        
        //die Startzeit des Services wird gesetzt
        this.timing = System.currentTimeMillis();

        //der Betriebsstatus wird gesetzt
        this.status = Service.RUN;
        
        //die Konfigurationsdatei wird ermittelt
        file = new File("devwex.ini");

        modified = file.lastModified();

        count = 0;
        delta = 0;
        total = 0;
        
        while (this.status < Service.STOP) {

            //die Konfiguration wird ermittelt
            section = this.initialize.get("common");
            
            //die Option CLEANUP wird ermittelt
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

            //die Option RELOAD wird ermittelt
            if (section.get("reload").toLowerCase().equals("on")
                    && modified != file.lastModified()) {

                modified = file.lastModified();

                //der Server wird durchgestartet
                Service.restart();
            }

            try {Thread.sleep(250);
            } catch (Throwable throwable) {
                Service.destroy();
            }
        }
        
        //Standardinformation wird ausgegeben
        Service.print("SERVICE STOPPED");
        
        Service.service = null;
    }
    
    /**
     * Globale Behanldung von nicht behandelten Fehlern.
     * @param thread    Thread
     * @param throwable Throwable
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Service.print(throwable);
    }
}