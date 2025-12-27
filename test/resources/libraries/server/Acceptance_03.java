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
package server;

import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.seanox.devwex.Initialize;
import com.seanox.devwex.Section;
import com.seanox.devwex.Service;

public class Acceptance_03 implements Runnable {

    private volatile ServerSocket socket;

    private volatile Socket accept;

    private volatile String caption;

    public Acceptance_03(String context, Object data)
            throws Throwable {

        if (context == null)
            context = "";
        context = context.trim();

        Section options = ((Initialize)data).get(context + ":ini");

        int port = 0;
        try {port = Integer.parseInt(options.get("port"));
        } catch (Throwable throwable) {
        }

        context = options.get("address", "auto").toLowerCase();
        InetAddress address = null;
        if (!context.equals("auto"))
            address = InetAddress.getByName(context);
        
        this.socket = new ServerSocket(port, 0, address);
        this.socket.setSoTimeout(1000);
        this.caption = ("TCP ").concat(this.socket.getInetAddress().getHostAddress()).concat(":").concat(String.valueOf(port));
    }
    
    public String expose() {
        return this.caption;
    }

    public void destroy() {
        try {this.socket.close();
        } catch (Throwable throwable) {
        }
    }

    public void run() {

        Service.print(("SERVER ").concat(this.caption).concat(" READY"));
        
        Thread thread = null;
        while (!this.socket.isClosed()) {
            try {
                if (thread == null
                        || !thread.isAlive()) {
                    this.accept = this.socket.accept();
                    thread = new Thread() {
                        @Override
                        public void run() {
                            throw new RuntimeException(this.getClass().getName());
                        }
                    };
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
                }
                try {Thread.sleep(25);
                } catch (Throwable throwable1) {
                    this.destroy();
                }
            }
        }
        
        Service.print(("SERVER ").concat(this.caption).concat(" STOPPED"));
    }
}