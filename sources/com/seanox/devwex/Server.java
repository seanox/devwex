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

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManagerFactory;

/**
 * Server provides as a physical host the access in the network for a special
 * address at a special port. With the start of {@link Service} all servers
 * which are defined in the configuration file will be started and their socket
 * will be accessed directly.
 *
 * @author  Seanox Software Solutions
 * @version 5.2.0 20220924
 */
public class Server implements Runnable {

    /** Configuration of the server */
    private final Initialize initialize;
    
    /** Socket of the server */
    private final ServerSocket socket;
    
    /** Context/name of the server */
    private final String context;

    /** Short description of the server */
    private final String caption;

    /** Register of Workers */
    private final Vector worker;

    /**
     * Constructor, establishes the server corresponding to the configuration.
     * @param  context    Server name
     * @param  initialize Configuration of the server
     * @throws Throwable
     *     In case of incorrect server configuration/setup.
     */
    public Server(String context, Initialize initialize)
            throws Throwable {

        if (context == null)
            context = "";
        this.context = context.trim();

        this.initialize = (Initialize)initialize.clone();

        // MEDIATYPES - The media types are rewritten for faster access. For
        // configuration, it is easier to use the media type as the key, but at
        // runtime it is easier and faster if the file extension is the key.

        // Mapping in a Dictionary, incomplete entries are ignored.
        Section section = new Section(true);
        Section mediatypes = this.initialize.get("mediatypes");
        if (mediatypes != null) {
            Enumeration enumeration = mediatypes.elements();
            while (enumeration.hasMoreElements()) {
                String string = (String)enumeration.nextElement();
                StringTokenizer tokenizer = new StringTokenizer(mediatypes.get(string));
                while (tokenizer.hasMoreTokens()) {
                    String buffer = tokenizer.nextToken().trim();
                    if (buffer.length() > 0)
                        section.set(buffer, string.toLowerCase());
                }
            }
            mediatypes.clear();
            mediatypes.merge(section);
        }

        // SERVER:INI - Loading the server configuration
        Section options = this.initialize.get(this.context.concat(":ini"));

        // SERVER:INI:ADDRESS - Host address of the server socket
        InetAddress address = null;
        String string = options.get("address").toLowerCase();
        if (!string.equals("auto"))
            address = InetAddress.getByName(string);

        // SERVER:INI:PORT - Port of the server socket
        int port = 0;
        try {port = Integer.parseInt(options.get("port"));
        } catch (Throwable throwable) {
        }

        // SERVER:INI:BACKLOG - Size of the backlog for pending connections
        int backlog = 0;
        try {backlog = Integer.parseInt(options.get("backlog"));
        } catch (Throwable throwable) {
        }

        // SERVER:SSL - If the SSL section is present, a secure socket is used.
        context = this.context.concat(":ssl");
        if (this.initialize.contains(context)) {

            // SERVER:SSL - Loading SSL configuration
            options = this.initialize.get(context);

            // SERVER:SSL:PASSWORD - Keystore and truststore are the same and
            // so only one password is used.
            String password = options.get("password");

            // SERVER:SSL:KEYSTORE / SERVER:SSL:TYPE (default: JKS)
            KeyStore keystore = KeyStore.getInstance(options.get("type", KeyStore.getDefaultType()));
            FileInputStream input = new FileInputStream(options.get("keystore"));
            try {keystore.load(input, password.toCharArray());
            } finally {
                input.close();
            }

            // SERVER:SSL:ALGORITHM - KeyManager (default: SunX509)
            KeyManagerFactory keymanager = KeyManagerFactory.getInstance(options.get("algorithm", KeyManagerFactory.getDefaultAlgorithm()));
            keymanager.init(keystore, password.toCharArray());

            // SERVER:SSL:ALGORITHM - TrustManager (default: SunX509)
            TrustManagerFactory trustmanager = TrustManagerFactory.getInstance(options.get("algorithm", TrustManagerFactory.getDefaultAlgorithm()));
            trustmanager.init(keystore);

            // SERVER:SSL:PROTOCOL - SSL Context (default: TLS)
            SSLContext secure = SSLContext.getInstance(options.get("protocol", "TLS"));
            secure.init(keymanager.getKeyManagers(), trustmanager.getTrustManagers(), null);

            // Establishment of secure socket (SSL/TLS)
            // IMPORTANT - Need and Want must be set independently
            this.socket = secure.getServerSocketFactory().createServerSocket(port, backlog, address);
            if (options.get("clientauth").toLowerCase().equals("on"))
                ((SSLServerSocket)this.socket).setNeedClientAuth(true);
            if (options.get("clientauth").toLowerCase().equals("auto"))
                ((SSLServerSocket)this.socket).setWantClientAuth(true);
            
        } else {

            // Establishment of non-secure socket
            this.socket = new ServerSocket(port, backlog, address);
        }
        
        // SERVER:INI:TIMEOUT - timeout to establish a connection in milliseconds
        int timeout = 0;
        try {timeout = Integer.parseInt(options.get("timeout"));
        } catch (Throwable throwable) {
        }
        this.socket.setSoTimeout(timeout);

        // Server short description is composed
        this.caption = ("TCP ").concat(this.socket.getInetAddress().getHostAddress())
                .concat(":").concat(String.valueOf(port));

        // The register of workers is established.
        this.worker = new Vector(256, 256);
    }

    /**
     * Returns the server short description.
     * @return server short description
     */
    public String explain() {
        return this.caption;
    }

    /** Terminates the server as thread. */
    public void destroy() {
        try {this.socket.close();
        } catch (Throwable throwable) {
        }
    }

    @Override
    public void run() {
        
        // NOTICE - The server uses a simple dynamic pool mechanism. 
        // A contingent of workers is provided to accept requests. Depending on
        // the current workload, the contingent is reduced or increased. 

        // The server short description is output with the initialization.
        Service.print(("SERVER ").concat(this.caption).concat(" READY"));

        // The server configuration is loaded.
        Section options = this.initialize.get(this.context.concat(":ini"));

        // SERVER:INI:MAXACCESS - max. number of concurrent connections
        int maxaccess = 0;
        try {maxaccess = Integer.parseInt(options.get("maxaccess"));
        } catch (Throwable throwable) {
        }

        try {

            // The number of additional workers is permanently recalculated.
            for (int count = 0; !this.socket.isClosed();) {

                boolean control = false;

                // Registered workers are monitored
                // - expired workers are removed
                // - unused / excess workers are isolated and then terminated
                // - workers without running thread are removed

                Enumeration enumeration = ((Vector)this.worker.clone()).elements();
                while (enumeration.hasMoreElements()
                        && !this.socket.isClosed()) {
                    Object[] objects = (Object[])enumeration.nextElement();
                    Thread thread = (Thread)objects[1];
                    if (!thread.isAlive()
                            && control)
                        this.worker.remove(objects);
                    Worker worker = (Worker)objects[0];
                    if (worker.available()
                            && control)
                        worker.isolate();
                    if (worker.available()
                            && thread.isAlive())
                        control = true;
                }

                // The number of workers to be established subsequently is
                // based on the last number.
                count = control ? 0 : maxaccess <= 0 ? 1 : count +count +1;

                // If there is no worker, new ones are created, the number is
                // limited by MAXACCESS, further requests parked in the backlog.
                for (int loop = count; !this.socket.isClosed() && loop > 0; loop--) {

                    if (this.worker.size() >= maxaccess
                            && maxaccess > 0)
                        break;

                    // The worker is established, registered
                    // and started as a daemon thread.

                    Worker worker = new Worker(this.context, this.socket, (Initialize)this.initialize.clone());
                    Thread thread = new Thread(worker);
                    this.worker.add(new Object[] {worker, thread});
                    thread.start();
                }

                try {Thread.sleep(25);
                } catch (Throwable throwable) {
                    this.destroy();
                }
            }
        } catch (Throwable throwable) {
            Service.print(throwable);
        }

        // Termination from the server is initiated,
        // all workers are forcibly terminated
        // and the termination is output.

        this.destroy();
        Enumeration enumeration = this.worker.elements();
        while (enumeration.hasMoreElements())
            ((Worker)((Object[])enumeration.nextElement())[0]).destroy();
        Service.print(("SERVER ").concat(this.caption).concat(" STOPPED"));
    }
}