/**
 * Devwex, Experimental Server Engine
 * Copyright (C) 2025 Seanox Software Solutions
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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Service is a container with the required mechanisms, APIs and sequences to
 * manage and run servers and modules.
 * 
 * <h2>Terms</h2>
 * 
 * <h3>Server</h3>
 * Servers provide physical access to the network for a protocol at a address
 * and one port. Seanox Devwex integrates servers via the Server API (SAPI),
 * which can also be used to modify or deploy existing server and network
 * functionalities.
 * 
 * <h3>Server (SAPI)</h3>
 * SAPI is based on the implementation of {@link Runnable}. The initialization
 * takes place via the constructor {@code Server(String name, Settings
 *     settings)} or {@code Server(String name, Object settings)}. The order
 * of initialization is defined by the order of the server configurations in the
 * configuration file. The created server instances are started by the runtime
 * container (Service) with the method {@code Server.run()} and are triggerted
 * to terminate by {@code Service.destroy()}. Both methods are called
 * asynchronously. If the termination is delayed, the runtime container waits
 * for all registered server instances to finish. Optionally, the implementation
 * of the {@code Server.expose()} method to get general information, such as
 * protocol and network connection, is supported.
 * 
 * <h3>Modules</h3>
 * The extensions intended for background activities do not provide any direct
 * external functions; they are identified by the class and loaded, initialized,
 * and configured when starting or restarting from the service or at runtime in
 * servers and modules via the Application-ClassLoader. Modules are global
 * extensions that are provided once instantiated to all components. Optionally,
 * it is possible to use the Seanox Devwex SDK, so that modules can also be used
 * in several independent instances with their own Application-ClassLoader.
 * 
 * <h3>Module API (XAPI)</h3>
 * The Module API only includes the initialization and termination of modules.
 * The initialization is carried out via the constructor initially with the
 * start of the runtime container (Service), whereby the sequence in the
 * configuration file is determined with the sequence of the modules within the
 * {@code INITIALIZE} section, or the initialization is carried out with the
 * first request of a module at runtime. The configuration is only passed to a
 * module if it is initially loaded via the {@code INITIALIZE} section.<br>
 * <br>
 * Modules are triggered for termination via {@code Module.destroy()}. The
 * runtime container does not monitor the termination and discards the modules
 * by unloading them from the ClassLoader. Optionally, the implementation of the
 * {@code Server.expose()} method to get general information, such as
 * manufacturer and version, is supported.
 * 
 * <h3>HTTP Modules</h3>
 * HTTP Modules are an extension of the Seanox Devwex Module API specifically
 * designed for filtering, accepting and processing HTTP requests that run in
 * the context of the server. HTTP modules allow you to implement filter and
 * service functionalities, which is described in detail below, based on 
 * individual implementations that can also be combined in an HTTP module.
 * 
 * <h3>Filters</h3>
 * Access to the physical and virtual hosts can be controlled by specially
 * defined rules: In addition to forwarding and responding to requests with a
 * defined server status, processing can also be passed to a filter. Filters are
 * intended for checking specific requests and, if necessary, manipulating
 * incoming requests; they should only have access to the request header if the
 * request is not completely processed by the filter.
 * 
 * <h3>API (XAPI+)</h3>
 * The API corresponds to the Module API, which is extended by method
 * {@code Module.filter(Worker worker, String options)} worker is the request
 * processing instance and has access to the server, socket, request, response,
 * configuration and environment. With the configuration you can set further
 * parameters and options for a filter, which are then optionally passed as
 * string.
 * 
 * <h3>Service</h3>
 * The target of an HTTP request these are directories, files and dynamic
 * content from external CGI and XCGI applications that are requested via 
 * virtual paths. Services are another form of dynamic content. These take over
 * the complete processing of incoming requests and have unrestricted access.
 * 
 * <h3>API (XAPI+)</h3>
 * The API corresponds to the Module API, which is extended by
 * {@code Module.service(Worker worker, String options)} The passed worker is
 * the request processing instance and has access to the server, socket,
 * request, response, configuration and environment. With the configuration,
 * further parameters and options can be set for a module, which are then
 * optionally passed as string.
 * 
 * <h3>Function</h3>
 * For initialization and runtime required data is read from the configuration
 * file {@code devwex.ini}, which is loaded from the current working directory.
 * At start, restart and stop, fixed sequences for loading and unloading servers
 * and modules are run. During runtime, the service monitors configuration,
 * servers as well as modules and controls the garbage collector for faster
 * release of resources.
 * 
 * <h2>Sequences</h2>
 * Starting, restarting and shutting down the servers as well as loading and
 * unloading modules are fixed processes of the runtime container.
 * 
 * <h3>Start</h3>
 * <ul>
 *   <li>
 *     The class path is extended to include all files of the directories
 *     specified with the VM argument {@code -Dlibraries}.
 *   </li>
 *   <li>
 *     All modules from section {@code INITIALIZE} are loaded and initialized
 *     and registered via constructor {@code Module(String options)}.
 *   </li>
 *   <li>
 *     All servers are determined by searching for sections that end in
 *     {@code INI} and to which an implementation can be found in the class
 *     path, which are loaded, registered, and initialized via constructor
 *     {@code Server(String name, Settings settings)} or {@code
 *         Server(String name, Object settings)}. To do this, each server is
 *     given the name of the detected section and a complete copy of the central
 *     configuration as a Settings object. After successful initialization, the
 *     server is started as a thread and can start its work in method {@code
 *         Server.run()}.
 *   </li>
 * </ul>
 * 
 * <h3>Module Call</h3>
 * <ul>
 *   <li>
 *     If a module is not yet loaded, this is determined from the current class
 *     path, initialized and registered via {@code Module(String options)}. A
 *     configuration is not passed, since only one central configuration is
 *     provided for modules in section {@code INITIALIZE}.
 *   </li>
 *   <li>
 *     If the module is already loaded, the current instance is used.
 *   </li>
 * </ul> 
 * 
 * <h3>Restart</h3>
 * The sequence corresponds to the combination of <i>Stop</i> and <i>Start</i>.
 * <ul>
 *   <li>
 *     All registered server instances are prompted to quit using the method
 *     {@code Server.destroy()}.
 *   </li>
 *   <li>
 *     All registered modules are prompted to quit by method {@code
 *         Module.destroy()}.
 *   </li>
 *   <li>
 *     The runtime container waits for the end of all registered servers.
 *   </li>
 *   <li>
 *     All modules and servers are unloaded by discarding the currently used
 *     ClassLoader.
 *   </li>
 *   <li>
 *     The class path is extended to include all files of the directories
 *     specified with the VM argument {@code -Dlibraries}.
 *   </li>
 *   <li>
 *     All modules from section {@code INITIALIZE} are loaded and initialized
 *     and registered via constructor {@code Module(String options)}.
 *   </li>
 *   <li>
 *     All servers are determined by searching for sections that end in
 *     {@code INI} and to which an implementation can be found in the class
 *     path, which are loaded, registered, and initialized via constructor
 *     {@code Server(String name, Settings settings)} or {@code
 *         Server(String name, Object settings)}. To do this, each server is
 *     given the name of the detected section and a complete copy of the central
 *     configuration as a Settings object. After successful initialization, the
 *     server is started as a thread and can start its work in method {@code
 *         Server.run()}.
 *   </li>
 * </ul>
 * 
 * <h3>Termination</h3>
 * <ul>
 *   <li>
 *     All registered server instances are prompted to quit using the method
 *     {@code Server.destroy()}.
 *   </li>
 *   <li>
 *     All registered modules are prompted to quit by method {@code
 *         Module.destroy()}.
 *   </li>
 *   <li>
 *     The runtime container waits for the end of all registered servers.
 *   </li>
 *   <li>
 *     All modules and servers are unloaded by discarding the currently used
 *     ClassLoader.
 *   </li>
 * </ul>
 */
public class Service implements Runnable, UncaughtExceptionHandler {

    // Configuration file
    private volatile File configuration;

    /** ClassLoader for loaded resources */
    private volatile ClassLoader loader;

    /** Configuration of the service */
    private volatile Settings settings;

    /** Operating status of the service */
    private volatile int status;

    /** Start time of the service */
    private volatile long timing;

    /** List of established servers */
    private final Vector servers;

    /** List of initialized modules */
    private final Hashtable modules;

    /** Reference of the service */
    private static volatile Service service;

    /** Constant with the current version of the service */
    public static final String VERSION = "0.0.0 00000000";

    /** Constant for the operating status Unknown */
    public static final int UNKNOWN = 0;

    /** Constant for the operating status Start */
    public static final int START = 1;

    /** Constant for the operating status Run */
    public static final int RUN = 2;

    /** Constant for the operating status Restart */
    public static final int RESTART = 3;

    /** Constant for the operating status Stop */
    public static final int STOP = 4;

    /** Constructor, creates the service. */
    private Service() {
        this.settings = new Settings(true);
        this.servers    = new Vector();
        this.modules    = new Hashtable();
    }

    /**
     * Loads the named class via the ClassLoader of the server. If this is not
     * yet established {@code null} is returned. If the class cannot be loaded,
     * this will cause {@link ClassNotFoundException}.
     * @param  name Name of class
     * @return the class determined by the ClassLoader of the server, if this is
     *     not yet available or established, {@code null} isreturned.
     * @throws ClassNotFoundException
     *     If the class cannot be loaded.
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
     * Requests a module. If this is not yet registered, the service will
     * establish it with the optionally passed data if it is a module context,
     * otherwise only the class will be registered as a reference. If errors
     * occur during initialization, these are not handled and passed on.
     * @param  module  module class
     * @param  options optinal data for the setup
     * @return the instance to the class, otherwise {@code null}
     * @throws Exception
     *     In case of errors related to initialization.
     */
    public static Object load(Class module, String options)
            throws Exception {

        synchronized (Service.class) {

            Service service = Service.service;
            if (service == null
                    || module == null)
                return null;            
            
            // Module is determined from the module list, if this is not yet
            // registered, it is (re)initialized and added. If modules cannot be
            // terminated, they remain also and use then the old ClassLoader.
            // The behavior is wanted, so that no zombies are created.
            
            Object object = service.modules.get(module);
            if (object != null)
                return object;
            
            Service.print(("SERVICE INITIATE MODULE ").concat(module.getName()));
            
            // Minimum implementation of the Module API is verified via
            // reflections and expects a suitable constructor as well as a
            // destroy method. An interface is not available because of the
            // restricted size of the binary.
            
            try {
                module.getMethod("destroy");
                object = module.getConstructor(String.class);
            } catch (NoSuchMethodException exception) {
                throw new NoSuchMethodException("Invalid interface");
            }

            // instance of the module is created and registered 
            object = ((Constructor)object).newInstance(options);
            service.modules.put(module, object);
         
            return object;
        }
    }

    /**
     * Initiates a sequence at the server.
     * Following sequences Sequences are supported:<br>
     *     <dir>START</dir>
     * If the serivce is not yet established, it is initialized, extends the
     * ClassPath, loads the modules, determines and starts the servers and
     * starts the service.<br>
     *     <dir>RESTART</dir>
     * The running service is kept, all modules and servers are terminated, the
     * ClassPath is extended, the modules are loaded (if they could be
     * terminated) and the servers are reinitialized.<br>
     *     <dir>STOP</dir>
     * The running service is kept, all modules and servers are terminated. If
     * this fails, a normal restart is performed to bring about a stable and
     * controllable operating state. In addition on basis of the last executable
     * configuration the class path is extended, the modules are loaded (if
     * these could be terminated) and the servers are reinitialized. In this
     * case the operational state is set to READY and DESTROY is discarded.
     * @param  mode operating mode
     * @return {@code true} when successful
     */
    public static boolean initiate(int mode) {
        return Service.initiate(mode, null);
    }
        
    /**
     * Initiates a sequence at the server.
     * Following sequences Sequences are supported:<br>
     *     <dir>START</dir>
     * If the serivce is not yet established, it is initialized, extends the
     * ClassPath, loads the modules, determines and starts the servers and
     * starts the service.<br>
     *     <dir>RESTART</dir>
     * The running service is kept, all modules and servers are terminated, the
     * ClassPath is extended, the modules are loaded (if they could be
     * terminated) and the servers are reinitialized.<br>
     *     <dir>STOP</dir>
     * The running service is kept, all modules and servers are terminated. If
     * this fails, a normal restart is performed to bring about a stable and
     * controllable operating state. In addition on basis of the last executable
     * configuration the class path is extended, the modules are loaded (if
     * these could be terminated) and the servers are reinitialized. In this
     * case the operational state is set to READY and DESTROY is discarded.
     * @param  mode operating mode
     * @param  file optional configuration file
     * @return {@code true} when successful
     */
    public static boolean initiate(int mode, String file) {

        Class  source;
        Object object;
        String context;
        String scope;
        String string;

        synchronized (Service.class) {
            
            // time of initialization is determined
            long timing = System.currentTimeMillis();

            // if not initialized yet, the service is established as a singleton
            // with global exception handler
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
                
                // all registered modules are triggered to terminate
                Enumeration enumeration = service.servers.elements();
                while (enumeration.hasMoreElements()) {
                    object = ((Object[])enumeration.nextElement())[0];
                    try {object.getClass().getMethod("destroy").invoke(object);
                    } catch (Throwable throwable) {
                        Service.print(throwable);
                    }
                }

                // all registered modules are triggered to terminate,
                // removing all modules from the module list
                enumeration = service.modules.keys();
                while (enumeration.hasMoreElements()) {
                    object = service.modules.remove(enumeration.nextElement());
                    try {object.getClass().getMethod("destroy").invoke(object);
                    } catch (Throwable throwable) {
                        Service.print(throwable);
                    }
                }
                
                while (service.servers.size() > 0) {
                    
                    // All active servers are detected and searched.
                    // Servers that are still running will wait until they end.
                    // Inactive servers are removed from the server list.
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
                        string = string.concat("\r\n- ").concat(files[loop].getPath());
                    }
                }
                
                Service.print(string);
                
                // ClassLoader is determined and (re) established
                ClassLoader loader = Service.class.getClassLoader();
                service.loader = new Loader(loader, libraries);
                loader = service.loader;
                
                // current configuration is loaded
                try {service.settings = Settings.parse(new String(Files.readAllBytes(service.configuration.toPath())), true);
                } catch (Throwable throwable) {
                    Service.print("SERVICE CONFIGURATION FAILED");
                    Service.print(throwable);
                }
                
                Service.print("SERVICE INITIATE MODULES");                

                // base options are determined
                Section section = service.settings.get("initialize");
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
                Enumeration enumeration = service.settings.elements();
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
                        scope = service.settings.get(context.concat(":ini")).get("scope", "com.seanox.devwex");
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
                        // because of the restricted size of the binary.

                        source.getMethod("destroy");
                        try {object = source.getConstructor(String.class, Settings.class);
                        } catch (NoSuchMethodException exception) {
                            object = source.getConstructor(String.class, Object.class);
                        }
                            
                        object = ((Constructor)object).newInstance(context, service.settings.clone());
                        
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
                    try {caption = object.getClass().getMethod("expose").invoke(object);
                    } catch (Throwable throwable) {
                        caption = null;
                    }
                    if (caption == null)
                        caption = object.getClass().getName();
                    result = result.concat(String.format("XAPI: %s\r\n", caption));
                }
                
                // all registered servers are detected
                // server identifier is determined via expose
                enumeration = service.servers.elements();
                while (enumeration.hasMoreElements()) {
                    Object object = ((Object[])enumeration.nextElement())[0];
                    try {caption = object.getClass().getMethod("expose").invoke(object);
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

        // In relation to RFC 20, RFC 1345, RFC 2616, RFC 7230: 7-bit US-ASCII
        // and in extension ISO-8859-1 are supported. DefaultCharset is set via
        // reflections to overwrite the VM argument -Dfile.encoding if
        // necessary.
        
        // Starting with Java 18, internal setting no longer works and must be
        // set as a VM argument. Setting via reflections is also no longer
        // permitted. Complete conversion to UTF-8 is possible. However, there
        // are numerous special features with regard to modules. It should
        // always be noted that network communication uses ISO-8859-1 and the
        // module logic, as well as the API, uses UTF-8. It becomes complicated
        // if, for example, URL-encoded strings contain ISO-8859-1 and UTF-8 in
        // the. Therefore the decision: ISO-8859-1 for everything! 
        
        System.setProperty("file.encoding", "ISO-8859-1");

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
        Service.print("Experimental Server Engine", true);
        Service.print("\r\n", true);
        
        // unknown commands output the command list
        if (!string.matches("^start|restart|status|stop$")) {
            Service.print(("usage: devwex [start] [file]\r\n")
                    .concat("       devwex [restart|status|stop] [address:port]"), true);
            return;
        }

        // In relation to RFC 20, RFC 1345, RFC 2616, RFC 7230: 7-bit US-ASCII
        // and in extension ISO-8859-1/Windows-1252/CP-1252 are supported.
        // Therefore DefaultCharset must be ISO-8859-1 compatible! Windows-1252
        // and CP-1252 are only required for development. A clean solution is to
        // decouple the web server logic and the network communication.
        if (!Charset.defaultCharset().name().matches("(?i)^((Windows|CP)-?)1252|ISO-8859-1$")) {
            Service.print("ISO-8859-1 is required as standard encoding.", true);
            return;
        }
        
        // START - start the server services
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
     * Output of the string value of an object line by line, prefixed with a
     * timestamp, to the standard IO. The content of the output is determined by
     * {@code Object.toString()}. For error objects the StackTrace is logged.
     * Line breaks in the content are summarized.
     * @param object Object that is to be output
     */
    public static void print(Object object) {
        Service.print(object, false);
    }
    
    /**
     * Output of the string value of an object line by line, prefixed with a
     * timestamp, to the standard IO. The content of the output is determined by
     * {@code Object.toString()}. For error objects the StackTrace is logged.
     * Line breaks in the content are summarized.
     * @param object Object that is to be output
     * @param plain {@code false} Suppresses the output of the timestamp, as
     *     well as the optimization.
     */
    public static void print(Object object, boolean plain) {

        if (object == null)
            return;

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
            if (!string.matches("\\s*\\R\\s*")
                    && string.trim().length() <= 0)
                return;
            string = string.trim();
            if (!plain) {
                string = String.format("%tF %<tT %s", new Date(), string);
                string = string.replaceAll("\\s*(\\R)", System.lineSeparator());
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

            Section section = this.settings.get("common");

            if (section.get("cleanup").toLowerCase().equals("on")) {
                count = Thread.activeCount();
                if (count == total) {
                    if (++delta >= 50)
                        delta = 1;
                } else delta = -1;
                total = count;
                if (delta == 49)
                    System.gc();
            }

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