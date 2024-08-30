/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * Devwex, Experimental Server Engine
 * Copyright (C) 2023 Seanox Software Solutions
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Remote, provides Telnet based client and server functionality for remote
 * control of the service for status requests, restart and stop.
 */
public class Remote implements Runnable {

    /** Socket of the server */
    private final ServerSocket socket;

    /** Short description of the server */
    private final String caption;

    /** Connection listener */
    private volatile Socket accept;
    
    /**
     * Constructor, establishes the server corresponding to the configuration.
     * @param  context    Server name
     * @param  initialize Configuration of the server
     * @throws Throwable
     *     In case of incorrect server configuration/setup.
     */
    public Remote(String context, Initialize initialize)
            throws Throwable {

        if (context == null)
            context = "";
        context = context.trim();
        
        // REMOTE:INI - Loading the server configuration
        Section options = initialize.get(context.concat(":ini"));
        
        // REMOTE:INI:ADDRESS - Host address of the server socket
        InetAddress address = null;
        String string = options.get("address").toLowerCase();
        if (!string.equals("auto"))
            address = InetAddress.getByName(string);

        // REMOTE:INI:PORT - Port of the server socket
        int port = 0;
        try {port = Integer.parseInt(options.get("port"));
        } catch (Throwable throwable) {
        }
        
        // Establishment of socket
        this.socket = new ServerSocket(port, 0, address);

        // Setting the timeout for the socket
        this.socket.setSoTimeout(10000);

        // Server short description is composed
        this.caption = ("TCP ").concat(this.socket.getInetAddress().getHostAddress())
                .concat(":").concat(String.valueOf(port));
    }
    
    /**
     * Returns the server short description.
     * @return server short description
     */
    public String expose() {
        return this.caption;
    }

    /**
     * Sends the command to the server via telnet. Returns the server response
     * as ByteArray, in case of error it is empty.
     * @param  address Server name or IP address
     * @param  port    Server port
     * @param  request Remote request
     * @return the response as ByteArray
     * @throws Exception
     *     In case of incorrect data access.
     */
    public static byte[] call(String address, int port, String request)
            throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Socket socket = new Socket(address, port);
        try {
            request = request == null ? "" : request.trim();
            socket.getOutputStream().write(request.concat("\r\n").getBytes());
            byte[] bytes = new byte[65535];
            InputStream input = socket.getInputStream();
            for (int size; (size = input.read(bytes)) >= 0;) {
                buffer.write(bytes, 0, size);
                Thread.sleep(25);
            }
        } finally {
            try {socket.close();
            } catch (Throwable throwable) {
            }
        }
        return buffer.toByteArray();
    }

    /**
     * Reads the incoming request, executes the corresponding command and
     * returns the resulting response.
     * @param socket established socket with the request
     */
    private static void service(Socket socket) {

        try {

            // SO timeout is set for the server socket
            socket.setSoTimeout(10000);

            // The request is limited by line break or by max. 65535 bytes.
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream input = socket.getInputStream();
            for (int value; (value = input.read()) >= 0;) {
                buffer.write(value);
                if (buffer.size() >= 65535
                        || value == 10
                        || value == 13)
                    break;
            }

            String string = buffer.toString().toLowerCase().trim();
            if (("status").equals(string))
                string = Service.details();
            else if (("restart").equals(string))
                string = ("SERVICE RESTART").concat(!Service.restart() ? " FAILED" : "ED");
            else if (("stop").equals(string))
                string = ("SERVICE STOP").concat(!Service.destroy() ? " FAILED" : "PED");
            else string = "UNKNOWN COMMAND";

            socket.getOutputStream().write(string.concat("\r\n").getBytes());

        } catch (Throwable throwable) {
        }

        try {socket.close();
        } catch (Throwable throwable) {
        }
    }

    /** Terminates the server as thread. */
    public void destroy() {
        try {this.socket.close();
        } catch (Throwable throwable) {
        }
    }

    @Override
    public void run() {

        // NOTICE - To provide a restart at runtime, the request accept is
        // implemented as concurrent/non-blocking as thread.

        if (this.accept != null) {
            Remote.service(this.accept);
            return;
        }

        Service.print(("SERVER ").concat(this.caption).concat(" READY"));

        Thread thread = null;
        while (!this.socket.isClosed()) {
            try {
                if (thread == null
                        || !thread.isAlive()) {
                    this.accept = this.socket.accept();
                    thread = new Thread(this);
                    thread.start();
                }
                try {Thread.sleep(250);
                } catch (Throwable throwable) {
                    this.destroy();
                }                
            } catch (SocketException exception) {
                this.destroy();
            } catch (InterruptedIOException exception) {
                continue;
            } catch (Throwable throwable) {
                Service.print(throwable);
                try {this.accept.close();
                } catch (Throwable exception) {
                    this.destroy();
                }
            }
        }

        Service.print(("SERVER ").concat(this.caption).concat(" STOPPED"));
    }
}