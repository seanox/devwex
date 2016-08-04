/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 *  Devwex, Advanced Server Developing
 *  Copyright (C) 2016 Seanox Software Solutions
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *  Remote, stellt Client- und Server- Funktionalit&auml;ten f&uuml;r die auf
 *  Telnet basierenden Fern&uuml;berwachung vom Service (Container) f&uuml;r
 *  Statusabfragen, Restart und Stop, zur Verf&uuml;gung.<br>
 *  <br>
 *  Remote 5.0 20160804<br>
 *  Copyright (C) 2016 Seanox Software Solutions<br>
 *  Alle Rechte vorbehalten.
 *
 *  @author  Seanox Software Solutions
 *  @version 5.0 20160804
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
     *  @param  name Name des Servers
     *  @param  data Konfigurationsdaten des Servers
     *  @throws Throwable bei fehlerhafter Einrichtung des Servers
     */
    public Remote(String name, Object data) throws Throwable {

        InetAddress address;
        String      string;
        Section     options;

        int         port;

        //der Servername wird uebernommen
        name = (name == null) ? "" : name.trim();
        
        //die Konfiguration wird ermittelt
        options = ((Initialize)data).get(name.concat(":bas"));
                
        //der Port des Servers wird ermittelt
        try {port = Integer.parseInt(options.get("port"));
        } catch (Throwable throwable) {
            port = 0;
        }

        //die Hostadresse des Servers wird ermittelt
        string  = options.get("address", "auto").toLowerCase();
        address = string.equals("auto") ? null : InetAddress.getByName(string);

        //der ServerSocket wird eingerichtet
        this.socket = new ServerSocket(port, 0, address);

        //das Timeout fuer den Socket wird gesetzt
        this.socket.setSoTimeout(1000);

        //die Serverkennung wird zusammengestellt
        this.caption = ("TCP ").concat(address.getHostAddress()).concat(":").concat(String.valueOf(port));
    }

    /**
     *  Sendet den Befehl per Telnet an den Server. R&uuml;ckgabe die Antwort
     *  des Servers als ByteArray, im Fehlerfall ein leeres ByteArray.
     *  @param  address Servername oder IP Adresse
     *  @param  port    Serverport
     *  @param  request Remoterequest
     *  @return der Response als ByteArray
     *  @throws IOException beim fehlerhaftem Datenzugriff
     *  @throws UnknownHostException bei fehlerhaften Verbindungsdaten
     */
    public static byte[] call(String address, int port, String request) throws IOException {

        ByteArrayOutputStream buffer;
        Socket                socket;
        InputStream           input;

        byte[]                bytes;

        int                   length;

        if (request == null) return new byte[0];

        //die Datenpuffer werden eingerichtet
        buffer = new ByteArrayOutputStream();
        bytes  = new byte[65535];

        //initiale Einrichtung vom Socket
        socket = null;

        try {

            //der Remotesocket wird eingerichtet
            socket = new Socket(address, port);

            //der Datenstrom wird eingerichtet
            input = socket.getInputStream();

            //der Request wird ausgegeben
            socket.getOutputStream().write(request.concat("\r\n").getBytes());

            //der Response wird gelesen
            while ((length = input.read(bytes)) >= 0) {
                buffer.write(bytes, 0, length);
                Service.sleep(25);
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
            string = buffer.toString().trim().toLowerCase();            
            
            //STATE - die Serverliste wird zusammengestellt
            if (string.equals("state")) {

                string = Service.details();

            //RESTART - starte die Server neu
            } else if (string.equals("restart")) {

                string = ("INFO: SERVICE RESTART").concat(!Service.restart() ? " FAILED" : "ED");

            //STOP - Ausgabe der Information
            } else if (string.equals("stop")) {

                string = ("INFO: SERVICE STOP").concat(!Service.destroy() ? " FAILED" : "PED");

            } else string = null;

            //der Response wird ueberprueft, kann keine Information ermittelt
            //werden wird eine Standardinformation gesetzt
            if (string == null || string.length() == 0)
                string = "INFO: UNKNOWN COMMAND";

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

    /**
     *  R&uuml;ckgabe der Serverkennung.
     *  @return die Serverkennung
     */
    public String getCaption() {
        return this.caption;
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

                    //der Socket wird fuer die Session eingerichtet
                    this.accept = this.socket.accept();

                    //der Thread fuer die Session wird eingerichet, ueber den
                    //Service wird dieser automatisch als Daemon verwendet
                    thread = new Thread(this);

                    //die Session wird als Thread gestartet
                    thread.start();
                }

                Service.sleep(250);

            } catch (SocketException throwable) {

                this.destroy();

            } catch (InterruptedIOException throwable) {

                continue;

            } catch (Throwable throwable) {

                Service.print(throwable);

                //der Socket wird gegebenfalls geschlossen
                try {this.accept.close();
                } catch (Throwable exception) {

                    //keine Fehlerbehandlung erforderlich
                }

                Service.sleep(25);
            }
        }

        //die Terminierung wird ausgegeben
        Service.print(("SERVER ").concat(this.caption).concat(" STOPPED"));
    }
}