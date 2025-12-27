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

import com.seanox.devwex.Initialize;
import com.seanox.devwex.Section;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Acceptance_02 implements Runnable {

    private volatile ServerSocket socket;

    private volatile Socket accept;

    private volatile String caption;

    public Acceptance_02(String context, final Object data)
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
        throw new RuntimeException(this.getClass().getName());
    }
}