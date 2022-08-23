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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loader, stellt Funktionen zum Laden von Bibliotheken zur Verf&uuml;gung.
 * Die Dateien der geladene Bibliotheken werden dabei nicht gesperrt und
 * k&ouml;nnen somit zur Laufzeit ge&auml;ndert werden.
 *
 * @author  Seanox Software Solutions
 * @version 5.0.1 20220823
 */
public class Loader extends URLClassLoader {
    
    /** Zwischenspeicher geladener Klassen (Name, Klasse) */
    private volatile Hashtable classes;

    /** &uuml;bergeordneter ClassLoader */
    private volatile ClassLoader loader;
    
    /** Verzeichnis der nach Klassen zu durchsuchenden Bibliotheken. */
    private volatile List libraries;

    /**
     * Konstruktor, richtet den Loader ein.
     * @param libraries Liste der nach Klassen zu durchsuchenden Bibliotheken
     */
    public Loader(List libraries) {
        this(Loader.class.getClassLoader(), libraries);
    }

    /**
     * Konstruktor, richtet den Loader ein.
     * @param loader    &uuml;bergeordneter ClassLoader
     * @param libraries Liste der nach Klassen zu durchsuchenden Bibliotheken
     */
    public Loader(ClassLoader loader, List libraries) {

        super(new URL[0], loader);

        this.classes   = new Hashtable();
        this.loader    = loader;
        this.libraries = libraries;
    }

    /**
     * Liefert einen InputStream, mit dem die durch name bezeichnete Ressource
     * ausgelesen werden kann. Falls die Ressource nicht gefunden werden konnte,
     * wird {@code null} zur&uuml;ckgegeben.
     * @param  name Name der Ressource
     * @return der InputStream zur Ressource, sonst {@code null} wenn diese
     *     nicht ermittelt werden kann
     */
    @Override
    public InputStream getResourceAsStream(String name) {

        name = (name == null) ? "" : name.trim();
        if (name.length() <= 0)
            return null;

        if (this.loader != null) {
            InputStream input = this.loader.getResourceAsStream(name);
            if (input != null)
                return input;
        }
        
        Iterator iterator = this.libraries.iterator();
        while (iterator.hasNext()) {

            ZipFile  store = null;
            ZipEntry entry = null;
            
            try {

                store = new ZipFile((File)iterator.next());
                entry = store.getEntry(name);
                
                if (entry != null) {

                    // der Datenpuffer wird eingerichtet
                    byte[] bytes = new byte[(int)entry.getSize()];

                    // der Datenstrom wird ausgelesen
                    new DataInputStream(store.getInputStream(entry)).readFully(bytes);

                    return new ByteArrayInputStream(bytes);
                }

            } catch (Throwable throwable) {
                // keine Fehlerbehandlung vorgesehen
            } finally {
                try {store.close();
                } catch (Throwable throwable) {
                    // keine Fehlerbehandlung vorgesehen
                }
            }
        }

        return null;
    }
    
    /**
     * R&uuml;ckgabe der per Namen angeforderten Ressource als URL. Kann diese
     * nicht ermittelt werden, wird {@code null} zur&uuml;ckgegeben.
     * @param  name Name der Ressource, to be used as is.
     * @return die URL zur angeforderten Ressource, sonst {@code null} wenn
     *     diese nicht ermittelt werden kann
     */
    @Override
    public URL getResource(String name) {

        name = (name == null) ? "" : name.trim();
        if (name.length() <= 0)
            return null;

        if (this.loader != null) {
            URL url = this.loader.getResource(name);
            if (url != null)
                return url;
        }

        Iterator iterator = this.libraries.iterator();
        while (iterator.hasNext()) {

            String  source = ((File)iterator.next()).getAbsolutePath();
            ZipFile store  = null;
            
            try {
                store = new ZipFile(source);
                if (store.getEntry(name) != null)
                    return new URL(("jar:file:").concat(source).concat("!/").concat(name));
            } catch (Throwable throwable) {
                // keine Fehlerbehandlung vorgesehen
            } finally {
                try {store.close();
                } catch (Throwable throwable) {
                    // keine Fehlerbehandlung vorgesehen
                }
            }
        }
        
        return null;
    }
    
    /**
     * L&auml;dt die per Name angegebene Klasse. sse. Im Namen kann entweder der
     * Punkt oder der Schr&auml;gstrich als Paket-Trennzeichen benutzt werden.
     * Mit der Option {@code resolve} kann entschieden werden, ob die
     * Aufl&ouml;sung von Abh&auml;ngigkeiten n&ouml;tig ist. Bei {@code true}
     * werden auch die von dieser Klasse ben&ouml;tigten Klassen geladen.
     * @param  name    Name der Klasse
     * @param  resolve Option {@code true} um Abh&auml;ngigkeitem zu laden
     * @return die geladene Klasse, ist diese nicht ermittelbar, f&uuml;hrt der
     *     Aufruf zur Ausnahme {@code ClassNotFoundException}
     * @throws ClassNotFoundException
     *     Wenn die Klass nicht gefunden werden kann.
     */
    @Override
    protected synchronized Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        name = (name == null) ? "" : name.trim();
        if (name.length() <= 0)
            return null;

        Class source = (Class)this.classes.get(name);
        if (source != null)
            return source;

        try {

            // das Paket wird ermittelt
            String packet = name.substring(0, Math.max(0, name.lastIndexOf('.')));
            
            // die Berechtigung zur Definition der Klasse wird geprueft, wenn
            // ein entsprechender SecurityManager vorliegt, ohne ist Definition
            // aller Klassen zulaessig
            SecurityManager security = System.getSecurityManager();

            // prueft die Berechtigung zum Laden der Klasse/Paket, liegt diese
            // Berechtigung nicht vor, fuehrt dies zur SecurityException
            if (security != null) security.checkPackageDefinition(packet);

            // das Package wird registriert
            if (super.getPackage(packet) == null)
                super.definePackage(packet, null, null, null, null, null, null, null);

        } catch (SecurityException exception) {
            if (this.loader == null)
                throw exception;
        }

        try {

            // ein Versuch die Klasse vom uebergeordneten ClassLoader zu laden 
            if (this.loader != null) {
                source = this.loader.loadClass(name);
                if (resolve)
                    super.resolveClass(source);
                return source;
            }
            
        } catch (SecurityException exception) {
            throw exception;
        } catch (Throwable throwable) {
            // keine Fehlerbehandlung vorgesehen
        }
        
        try {
            
            // der Klassenname wird vereinheitlicht
            // und der Datenstrom zur Resource etabliert
            InputStream input = this.getResourceAsStream(name.replace('.', '/').concat(".class"));
            if (input instanceof ByteArrayInputStream) {
                
                // der Datenpuffer wird eingerichtet
                byte[] bytes = new byte[((ByteArrayInputStream)input).available()];
                
                // der Datenpuffer wird komplett gelesen
                input.read(bytes);

                // die Klasse wird ueber den ClassLoader definiert
                source = super.defineClass(name, bytes, 0, bytes.length);

                // die Klasse wird als geladen registriert
                this.classes.put(name, source);

                // gegebenfalls werden die Abhaengigkeitem aufgeloest
                if (resolve) super.resolveClass(source);

                return source;
            }

        } catch (SecurityException exception) {
            throw exception;
        } catch (Throwable throwable) {
            // keine Fehlerbehandlung vorgesehen
        }

        throw new ClassNotFoundException(name);
    }
}