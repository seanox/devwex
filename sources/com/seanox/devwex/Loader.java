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
 * Loader, provides functions for loading libraries and classes. The files of
 * the loaded libraries are not locked and can be changed at runtime.
 *
 * @author  Seanox Software Solutions
 * @version 5.0.1 20220823
 */
public class Loader extends URLClassLoader {
    
    /** Registration of loaded classes (name, class) */
    private volatile Hashtable classes;

    /** Parent ClassLoader */
    private volatile ClassLoader loader;
    
    /** List of libraries to be searched. */
    private volatile List libraries;

    /**
     * Constructor, creates the loader.
     * @param libraries List of libraries to be searched
     */
    public Loader(List libraries) {
        this(Loader.class.getClassLoader(), libraries);
    }

    /**
     * Constructor, creates the loader.
     * @param loader    Parent ClassLoader
     * @param libraries List of libraries to be searched
     */
    public Loader(ClassLoader loader, List libraries) {

        super(new URL[0], loader);

        this.classes   = new Hashtable();
        this.loader    = loader;
        this.libraries = libraries;
    }

    @Override
    public InputStream getResourceAsStream(String name) {

        if (name == null)
            return null;
        name = name.trim();
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
                    byte[] bytes = new byte[(int)entry.getSize()];
                    new DataInputStream(store.getInputStream(entry)).readFully(bytes);
                    return new ByteArrayInputStream(bytes);
                }

            } catch (Throwable throwable) {
            } finally {
                try {store.close();
                } catch (Throwable throwable) {
                }
            }
        }

        return null;
    }
    
    @Override
    public URL getResource(String name) {

        if (name == null)
            return null;
        name = name.trim();
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
            } finally {
                try {store.close();
                } catch (Throwable throwable) {
                }
            }
        }
        
        return null;
    }
    
    @Override
    protected synchronized Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        if (name == null)
            return null;
        name = name.trim();
        if (name.length() <= 0)
            return null;

        Class source = (Class)this.classes.get(name);
        if (source != null)
            return source;

        try {

            // determination from the package
            String packet = name.substring(0, Math.max(0, name.lastIndexOf('.')));
            
            // the permission to define the class is checked if there is a
            // corresponding SecurityManager, without it the definition of all
            // classes is allowed
            SecurityManager security = System.getSecurityManager();

            // checks the permission to load the class/package, if this
            // permission is not present, this causes the SecurityException
            if (security != null)
                security.checkPackageDefinition(packet);

            // registration of the package
            if (super.getPackage(packet) == null)
                super.definePackage(packet, null, null, null, null, null, null, null);

        } catch (SecurityException exception) {
            if (this.loader == null)
                throw exception;
        }

        // Attempts to load the class from the parent ClassLoader.

        try {
            if (this.loader != null) {
                source = this.loader.loadClass(name);
                if (resolve)
                    super.resolveClass(source);
                return source;
            }
        } catch (SecurityException exception) {
            throw exception;
        } catch (Throwable throwable) {
        }

        // The class name is normalized (dot to slash).
        // Then the class can be loaded as a resource and defined as a class.
        // The class is then registered as loaded.
        // If needed, the class can be linked if it has been loaded and linked.

        try {
            InputStream input = this.getResourceAsStream(name.replace('.', '/').concat(".class"));
            if (input instanceof ByteArrayInputStream) {
                byte[] bytes = new byte[((ByteArrayInputStream)input).available()];
                input.read(bytes);
                source = super.defineClass(name, bytes, 0, bytes.length);
                this.classes.put(name, source);
                if (resolve)
                    super.resolveClass(source);
                return source;
            }
        } catch (SecurityException exception) {
            throw exception;
        } catch (Throwable throwable) {
        }

        throw new ClassNotFoundException(name);
    }
}