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
 * Server stellt als physischer Host, den Zugriff im Netzwerk f&uuml;r eine
 * spezielle Adresse an einem speziellen Port zur Verf&uuml;gung. Mit dem Start
 * von Devwex werden alle in der Konfigurationsdatei angegebenen Server
 * gestartet. Auf die gestarteten Server wird immer direkt zugegriffen.<br>
 * <br>
 * Server 5.1.0 20200202<br>
 * Copyright (C) 2018 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 5.1.0 20200202
 */
public class Server implements Runnable {

    /** Konfiguration des Servers */
    private volatile Initialize initialize;
    
    /** Socket des Servers */
    private volatile ServerSocket socket;
    
    /** Context/Name des Servers */
    private volatile String context;

    /** Kennung und Bezeichnung des Servers */
    private volatile String caption;

    /** Worker der eingerichteten Verbindungen */
    private volatile Vector worker;

    /**
     * Konstruktor, richtet den Server entsprechenden der Konfiguration ein.
     * @param  context Name des Servers
     * @param  data    Konfigurationsdaten des Servers
     * @throws Throwable
     *     Bei fehlerhafter Einrichtung des Servers.
     */
    public Server(String context, Object data) throws Throwable {

        TrustManagerFactory trustmanager;
        SSLContext          secure;
        String              buffer;
        String              string;
        StringTokenizer     tokenizer;
        Section             options;
        Section             section;
        KeyStore            keystore;
        KeyManagerFactory   keymanager;
        FileInputStream     input;
        InetAddress         address;
        Enumeration         enumeration;

        int                 volume;
        int                 port;
        int                 isolation;
        
        // der Servername wird uebernommen
        this.context = context == null ? "" : context.trim();

        // die Konfiguration wird eingerichtet
        this.initialize = (Initialize)((Initialize)data).clone();

        // Die Mediatypes werden fuer einen schnelleren Zugriff umgeschrieben.
        // Dazu bilden die Dateiendungen den Schluessel und der Mediatype den
        // Wert. Zur Konfiguration ist der Mediatype als Schluessel einfacher.
        
        // die Mediatypes werden eingerichtet
        // die Mediatypes werden entsprechend den Dateiendungen aufgebaut
        // unvollstaendige Eintraege werden nicht beruecksichtig
        section = new Section(true);
        options = this.initialize.get("mediatypes");
        if (options != null) {
            enumeration = options.elements();
            while (enumeration.hasMoreElements()) {
                string = (String)enumeration.nextElement();
                tokenizer = new StringTokenizer(options.get(string));
                while (tokenizer.hasMoreTokens()) {
                    buffer = tokenizer.nextToken().trim();
                    if (buffer.length() > 0)
                        section.set(buffer, string.toLowerCase());
                }
            }
            options.clear();
            options.merge(section);
        }
        
        // die Serverkonfiguration wird ermittelt
        options = this.initialize.get(this.context);

        // die Hostadresse des Servers wird ermittelt
        context = options.get("address").toLowerCase();
        address = context.equals("auto") ? null : InetAddress.getByName(context);

        // der Port des Servers wird ermittelt
        try {port = Integer.parseInt(options.get("port"));
        } catch (Throwable throwable) {
            port = 0;
        }

        // die maximale Leerlaufzeit fuer den Verbindungsaufbau
        try {isolation = Integer.parseInt(options.get("isolation"));
        } catch (Throwable throwable) {
            isolation = 0;
        }
        
        // die Anzahl zurueckzustellender Verbindungen wird ermittelt
        try {volume = Integer.parseInt(options.get("backlog"));
        } catch (Throwable throwable) {
            volume = 0;
        }
        
        context = this.context.replaceAll("(?i):[a-z]+$", ":ssl");

        // entsprechend dem Serverschema wird der ServerSocket eingerichtet
        if (this.initialize.contains(context)) {

            // die SSL Konfiguration wird ermittelt
            options = this.initialize.get(context);

            // der Typ des KeyStores wird ermittelt, Standard ist JKS
            keystore = KeyStore.getInstance(options.get("type", KeyStore.getDefaultType()));
            
            // das Passwort des KeyStores wird ermittelt
            string = options.get("password");

            // der KeyStore wird geladen
            input = new FileInputStream(options.get("keystore"));
            try {keystore.load(input, string.toCharArray());
            } finally {
                input.close();
            }
            
            // der KeyManager wird eingerichtet, Standard ist SunX509
            keymanager = KeyManagerFactory.getInstance(options.get("algorithm", KeyManagerFactory.getDefaultAlgorithm()));            
            
            // der KeyManager wird initialisiert
            keymanager.init(keystore, string.toCharArray());
            
            // der TrustManager wird eingerichtet, Standard ist SunX509
            trustmanager = TrustManagerFactory.getInstance(options.get("algorithm", TrustManagerFactory.getDefaultAlgorithm()));
            
            // der TrustManager wird initialisiert
            trustmanager.init(keystore);

            // das SSL Protokoll wird ermittelt, Standard ist TLS
            secure = SSLContext.getInstance(options.get("protocol", "TLS"));

            // der SecureContext wird mit Key- und TrustManager(n) initialisiert
            secure.init(keymanager.getKeyManagers(), trustmanager.getTrustManagers(), null);

            // der Socket wird mit Adresse bzw. automatisch eingerichtet
            this.socket = secure.getServerSocketFactory().createServerSocket(port, volume, address);
            
            // WICHTIG - Need und Want muessen unabhaenig gesetzt werden
            if (options.get("clientauth").toLowerCase().equals("on"))
                ((SSLServerSocket)this.socket).setNeedClientAuth(true);
            if (options.get("clientauth").toLowerCase().equals("auto"))
                ((SSLServerSocket)this.socket).setWantClientAuth(true);
            
        } else {

            // der Socket wird mit Adresse bzw. automatisch eingerichtet
            this.socket = new ServerSocket(port, volume, address);
        }

        // das Timeout fuer den Socket wird gesetzt
        this.socket.setSoTimeout(isolation <= 0 ? 250 : isolation);

        // die Serverkennung wird zusammengestellt
        this.caption = ("TCP ").concat(this.socket.getInetAddress().getHostAddress()).concat(":").concat(String.valueOf(port));
    }

    /**
     * R&uuml;ckgabe der Serverkennung.
     * @return die Serverkennung
     */
    public String explain() {
        return this.caption;
    }

    /** Beendet den Server als Thread */
    public void destroy() {

        // der Socket wird geschlossen
        try {this.socket.close();
        } catch (Throwable throwable) {

            // keine Fehlerbehandlung erforderlich
        }
    }

    /** Stellt den Einsprung in den Thread zur Verf&uuml;gung. */
    @Override
    public void run() {

        Worker      worker;
        Section     options;
        Thread      thread;
        Enumeration enumeration;

        Object[]    objects;

        boolean     control;

        int         count;
        int         loop;
        int         volume;

        // die Worker werden eingerichtet
        this.worker = new Vector(256, 256);

        // Initialisierung wird als Information ausgegeben
        Service.print(("SERVER ").concat(this.caption).concat(" READY"));

        // die Serverkonfiguration wird ermittelt
        options = this.initialize.get(this.context);

        // MAXACCESS - die max. Anzahl gleichzeitiger Verbindungen wird ermittelt
        try {volume = Integer.parseInt(options.get("maxaccess"));
        } catch (Throwable throwable) {
            volume = 0;
        }

        // die initiale Anzahl zusaetzlicher Worker wird angelegt
        count = 0;

        try {

            while (!this.socket.isClosed()) {

                control = false;
                
                enumeration = ((Vector)this.worker.clone()).elements();
                while (enumeration.hasMoreElements() && !this.socket.isClosed()) {

                    objects = (Object[])enumeration.nextElement();

                    // der Thread wird ermittelt
                    thread = (Thread)objects[1];

                    // ausgelaufene Worker werden entfernt
                    if (control && !thread.isAlive())
                        this.worker.remove(objects);

                    // der Worker wird ermittelt
                    worker = (Worker)objects[0];

                    // ueberzaehlige Worker werden beendet
                    if (control && worker.available())
                        worker.isolate();

                    // laeuft der Thread nicht, wird der Worker entfernt
                    if (worker.available() && thread.isAlive())
                        control = true;
                }

                // die Anzahl der nachtraeglich einzurichtenden Worker wird auf
                // Basis der letzten Anzahl ermittelt
                count = control ? 0 : volume <= 0 ? 1 : count +count +1;

                // liegt kein freier Worker vor, werden neue eingerichtet, die
                // Anzahl ist durch die Angabe vom MAXACCESS begrenzt, weitere
                // Anfragen werden sonst im Backlog geparkt
                for (loop = count; !this.socket.isClosed() && loop > 0; loop--) {
                    
                    if (this.worker.size() >= volume && volume > 0)
                        break;

                    // der Worker wird eingerichtet
                    worker = new Worker(this.context, this.socket, (Initialize)this.initialize.clone());

                    // der Thread der Worker wird eingerichet, ueber den
                    // Service wird dieser automatisch als Daemon verwendet
                    thread = new Thread(worker);

                    // der Worker wird mit Thread registriert
                    this.worker.add(new Object[] {worker, thread});
                    
                    // der Worker wird als Thread gestartet
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
        
        // das Beenden vom Server wird eingeleitet
        this.destroy();
        
        // alle Worker werden zwangsweise beendet
        enumeration = this.worker.elements();
        while (enumeration.hasMoreElements())
            ((Worker)((Object[])enumeration.nextElement())[0]).destroy();

        // die Terminierung wird ausgegeben
        Service.print(("SERVER ").concat(this.caption).concat(" STOPPED"));
    }
}