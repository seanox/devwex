/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 *  Devwex, Advanced Server Development
 *  Copyright (C) 2017 Seanox Software Solutions
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 *  Remote, stellt Client- und Server- Funktionalit&auml;ten f&uuml;r die auf
 *  Telnet basierenden Fern&uuml;berwachung vom Service (Container) f&uuml;r
 *  Statusabfragen, Restart und Stop, zur Verf&uuml;gung.<br>
 *  <br>
 *  Remote 5.0 20170828<br>
 *  Copyright (C) 2017 Seanox Software Solutions<br>
 *  Alle Rechte vorbehalten.
 *
 *  @author  Seanox Software Solutions
 *  @version 5.0 20170828
 */
public class Remote implements Runnable {

    /** Socket des Servers */
    private volatile ServerSocket socket;

    /** Socket des Servers */
    private volatile Socket accept;

    /** Kennung und Bezeichnung des Servers */
    private volatile String caption;

    /**
     *  Konstruktor, richtet den Server entsprechenden der Konfiguration ein.
     *  @param  server Name des Servers
     *  @param  data   Konfigurationsdaten des Servers
     *  @throws Throwable
     *      Bei fehlerhafter Einrichtung des Servers.
     */
    public Remote(String server, Object data) throws Throwable {

        InetAddress address;
        Section     options;

        int         port;

        //der Servername wird uebernommen
        server = server == null ? "" : server.trim();
        
        //die Konfiguration wird ermittelt
        options = ((Initialize)data).get(server.concat(":bas"));
                
        //der Port des Servers wird ermittelt
        try {port = Integer.parseInt(options.get("port"));
        } catch (Throwable throwable) {
            port = 0;
        }

        //die Hostadresse des Servers wird ermittelt
        server  = options.get("address", "auto").toLowerCase();
        address = server.equals("auto") ? null : InetAddress.getByName(server);

        //der ServerSocket wird eingerichtet
        this.socket = new ServerSocket(port, 0, address);

        //das Timeout fuer den Socket wird gesetzt
        this.socket.setSoTimeout(1000);

        //die Serverkennung wird zusammengestellt
        this.caption = ("TCP ").concat(this.socket.getInetAddress().getHostAddress()).concat(":").concat(String.valueOf(port));
    }
    
    /**
     *  R&uuml;ckgabe der Serverkennung.
     *  @return die Serverkennung
     */
    public String explain() {
        return this.caption;
    }

    /**
     *  Sendet den Befehl per Telnet an den Server. R&uuml;ckgabe die Antwort
     *  des Servers als ByteArray, im Fehlerfall ein leeres ByteArray.
     *  @param  address Servername oder IP Adresse
     *  @param  port    Serverport
     *  @param  request Remoterequest
     *  @return der Response als ByteArray
     *  @throws Exception
     *      Bei fehlerhaftem Datenzugriff.
     */
    public static byte[] call(String address, int port, String request) throws Exception {

        ByteArrayOutputStream buffer;
        Socket                socket;
        InputStream           input;

        byte[]                bytes;

        int                   length;

        //der Request wird vorbereitet
        request = request == null ? "" : request.trim();

        //die Datenpuffer werden eingerichtet
        buffer = new ByteArrayOutputStream();
        bytes  = new byte[65535];
        
        //der Remotesocket wird eingerichtet
        socket = new Socket(address, port);
       
        try {

            //der Datenstrom wird eingerichtet
            input = socket.getInputStream();
            
            //der Request wird ausgegeben
            socket.getOutputStream().write(request.concat("\r\n").getBytes());

            //der Response wird gelesen
            while ((length = input.read(bytes)) >= 0) {
                buffer.write(bytes, 0, length);
                Thread.sleep(25);
            }

        } finally {

            //der Socket wird geschlossen
            try {socket.close();
            } catch (Throwable throwable) {

                //keine Fehlerbehandlung erforderlich
            }
        }

        return buffer.toByteArray();
    }

    /**
     *  Liest den eingehenden Request, f&uuml;hrt das entsprechende Kommando aus
     *  und gibt den resultierenden Response zur&uuml;ck.
     *  @param socket eingerichteter Socket mit dem Request
     */
    private static void service(Socket socket) {

        ByteArrayOutputStream buffer;
        InputStream           input;
        String                string;

        int                   value;

        //der Datenbuffer wird eingerichtet
        buffer = new ByteArrayOutputStream();

        try {

            //das SO Timeout wird fuer den Serversocket gesetzt
            socket.setSoTimeout(10000);

            //der Request wird gelesen und ausgewertet
            //die Datenstroeme werden eingerichtet
            input = socket.getInputStream();

            //die Daten werden aus dem Datenstrom gelesen
            while ((value = input.read()) >= 0) {

                //die Daten werden gespeichert
                buffer.write(value);

                //ist der Request komplett oder wurde die maximal Laenge von
                //65535 Bytes erreicht wird das Lesen beendet
                if (value == 10 || value == 13 || buffer.size() >= 65535) break;
            }

            //der Request wird ausgewertet
            string = buffer.toString().toLowerCase().trim();    
            
            //STATUS - die Serverliste wird zusammengestellt
            if (string.equals("status")) {

                string = Service.details();

            //RESTART - starte die Server neu
            } else if (string.equals("restart")) {

                string = ("SERVICE RESTART").concat(!Service.restart() ? " FAILED" : "ED");

            //STOP - Ausgabe der Information
            } else if (string.equals("stop")) {

                string = ("SERVICE STOP").concat(!Service.destroy() ? " FAILED" : "PED");
                
            } else string = "UNKNOWN COMMAND";

            //der Response wird ausgegeben
            socket.getOutputStream().write(string.concat("\r\n").getBytes());

        } catch (Throwable throwable) {

            //keine Fehlerbehandlung erforderlich
        }

        //der Socket wird geschlossen
        try {socket.close();
        } catch (Throwable throwable) {

            //keine Fehlerbehandlung erforderlich
        }
    }

    /** Beendet den Server als Thread */
    public void destroy() {

        //der Socket wird geschlossen
        try {this.socket.close();
        } catch (Throwable throwable) {

            //keine Fehlerbehandlung erforderlich
        }
    }

    /** Stellt den Einsprung in den Thread zur Verf&uuml;gung. */
    public void run() {

        Thread thread;

        //Hinweis - damit auch der Restart vom Remote moeglich ist, muss die
        //Verarbeitung in einem seperaten Thread ausgelagert werden
        if (this.accept != null) {Remote.service(this.accept); return;}

        //Initialisierung wird als Information ausgegeben
        Service.print(("SERVER ").concat(this.caption).concat(" READY"));

        for (thread = null; !this.socket.isClosed();) {

            try {

                if (thread == null || !thread.isAlive()) {

                    //der Socket wird fuer den Worker eingerichtet
                    this.accept = this.socket.accept();

                    //der Thread fuer den Worker wird eingerichet, ueber den
                    //Service wird dieser automatisch als Daemon verwendet
                    thread = new Thread(this);

                    //der Worker wird als Thread gestartet
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

                //der Socket wird gegebenfalls geschlossen
                try {this.accept.close();
                } catch (Throwable exception) {

                    //keine Fehlerbehandlung erforderlich
                }

                try {Thread.sleep(25);
                } catch (Throwable throwable1) {
                    this.destroy();
                }
            }
        }

        //die Terminierung wird ausgegeben
        Service.print(("SERVER ").concat(this.caption).concat(" STOPPED"));
    }
}