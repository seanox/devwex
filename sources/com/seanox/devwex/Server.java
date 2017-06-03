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

/**
 *  Server stellt als physischer Host, den Zugriff im Netzwerk f&uuml;r eine
 *  spezielle Adresse an einem speziellen Port zur Verf&uuml;gung. Mit dem Start
 *  von Devwex werden alle in der Konfigurationsdatei angegebenen Server
 *  gestartet. Auf die gestarteten Server wird immer direkt zugegriffen.<br>
 *  <br>
 *  Server 5.0 20170528<br>
 *  Copyright (C) 2017 Seanox Software Solutions<br>
 *  Alle Rechte vorbehalten.
 *
 *  @author  Seanox Software Solutions
 *  @version 5.0 20170528
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

    /** Listener der eingerichteten Verbindungen */
    private volatile Vector listener;

    /**
     *  Konstruktor, richtet den Server entsprechenden der Konfiguration ein.
     *  @param  server Name des Servers
     *  @param  data   Konfigurationsdaten des Servers
     *  @throws Throwable bei fehlerhafter Einrichtung des Servers
     */
    public Server(String server, Object data) throws Throwable {

        SSLContext        context;
        String            buffer;
        String            string;
        StringTokenizer   tokenizer;
        Section           options;
        Section           section;
        KeyStore          keystore;
        KeyManagerFactory manager;
        FileInputStream   filestream;
        InetAddress       address;
        Enumeration       enumeration;

        int               isolation;
        int               port;
        int               volume;
        
        //der Servername wird uebernommen
        this.context = server == null ? "" : server.trim();

        //die Konfiguration wird eingerichtet
        this.initialize = (Initialize)((Initialize)data).clone();

        //Die Mediatypes werden fuer einen schnelleren Zugriff umgeschrieben.
        //Dazu bilden die Dateiendungen den Schluessel und der Mediatype den
        //Wert. Zur Konfiguration ist der Mediatype als Schluessel einfacher.
        
        //die Mediatypes werden eingerichtet
        section = new Section(true);

        //die Mediatypes werden ermittelt
        options = this.initialize.get("mediatypes");

        if (options != null) {

            //die Contenttypes werden ermittelt
            enumeration = options.elements();
    
            //die Mediatypes werden entsprechend den Dateiendungen aufgebaut
            //unvollstaendige Eintraege werden nicht beruecksichtig
            while (enumeration.hasMoreElements()) {
    
                //der Mediatype wird ermittelt
                string = (String)enumeration.nextElement();
    
                //die Dateiendungen werden ermittelt
                tokenizer = new StringTokenizer(options.get(string));
                while (tokenizer.hasMoreTokens()) {
                    buffer = tokenizer.nextToken().trim();
                    if (buffer.length() > 0)
                        section.set(buffer, string.toLowerCase());
                }
            }
    
            //die Mediatypes werden uberschrieben
            options.clear();
            options.merge(section);
        }
        
        //die Serverkonfiguration wird ermittelt
        options = this.initialize.get(this.context.concat(":bas"));

        //die Hostadresse des Servers wird ermittelt
        server  = options.get("address").toLowerCase();
        address = server.equals("auto") ? null : InetAddress.getByName(server);

        //der Port des Servers wird ermittelt
        try {port = Integer.parseInt(options.get("port"));
        } catch (Throwable throwable) {
            port = 0;
        }

        //die maximale Leerlaufzeit fuer den Verbindungsaufbau
        try {isolation = Integer.parseInt(options.get("isolation"));
        } catch (Throwable throwable) {
            isolation = 0;
        }
        
        //die Anzahl zurueckzustellender Verbindungen wird ermittelt
        try {volume = Integer.parseInt(options.get("backlog"));
        } catch (Throwable throwable) {
            volume = 0;
        }

        //entsprechend dem Serverschema wird der ServerSocket eingerichtet
        if (this.initialize.contains(this.context.concat(":ssl"))) {

            //die SSL Konfiguration wird ermittelt
            options = this.initialize.get(this.context.concat(":ssl"));

            //der Typ des KeyStores wird ermittelt, Standard ist JKS
            keystore = KeyStore.getInstance(options.get("type", KeyStore.getDefaultType()));
            
            //der SSL Algorithmus wird ermittelt, Standard ist SunX509
            manager = KeyManagerFactory.getInstance(options.get("algorithm", KeyManagerFactory.getDefaultAlgorithm()));
            
            //das Passwort des KeyStores wird ermittelt
            string = options.get("password");

            //der KeyStore wird geladen
            filestream = new FileInputStream(options.get("keystore"));
            try {keystore.load(filestream, string.toCharArray());
            } finally {
                filestream.close();
            }
            
            //der KeyStore wird initialisiert
            manager.init(keystore, string.toCharArray());

            //das SSL Protokoll wird ermittelt, Standard ist TLS
            context = SSLContext.getInstance(options.get("protocol", "TLS"));

            //der SecureContext wird mit KeyManager(n) initialisiert
            context.init(manager.getKeyManagers(), null, null);

            //der Socket wird mit Adresse bzw. automatisch eingerichtet
            this.socket = context.getServerSocketFactory().createServerSocket(port, volume, address);

            ((SSLServerSocket)this.socket).setNeedClientAuth(options.get("clientauth").toLowerCase().equals("on"));

        } else {

            //der Socket wird mit Adresse bzw. automatisch eingerichtet
            this.socket = new ServerSocket(port, volume, address);
        }

        //das Timeout fuer den Socket wird gesetzt
        this.socket.setSoTimeout(isolation <= 0 ? 250 : isolation);

        //die Serverkennung wird zusammengestellt
        this.caption = ("TCP ").concat(this.socket.getInetAddress().getHostAddress()).concat(":").concat(String.valueOf(port));
    }

    /**
     *  R&uuml;ckgabe der Serverkennung.
     *  @return die Serverkennung
     */
    public String getCaption() {
        return this.caption;
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

        Listener    listener;
        Section     options;
        Thread      thread;
        Enumeration enumeration;

        Object[]    objects;

        boolean     control;

        int         count;
        int         loop;
        int         volume;

        //die Listener werden eingerichtet
        this.listener = new Vector(256, 256);

        //Initialisierung wird als Information ausgegeben
        Service.print(("SERVER ").concat(this.caption).concat(" READY"));

        //die Serverkonfiguration wird ermittelt
        options = this.initialize.get(this.context.concat(":bas"));

        //MAXACCESS - die Anzahl max. gleichzeitiger Verbindungen wird ermittelt
        try {volume = Integer.parseInt(options.get("maxaccess"));
        } catch (Throwable throwable) {
            volume = 0;
        }

        //die initiale Anzahl zusaetzlicher Listener wird angelegt
        count = 0;

        try {

            while (!this.socket.isClosed()) {

                control = false;
                
                enumeration = ((Vector)this.listener.clone()).elements();
                while (enumeration.hasMoreElements() && !this.socket.isClosed()) {

                    objects = (Object[])enumeration.nextElement();

                    //der Thread wird ermittelt
                    thread = (Thread)objects[1];

                    //ausgelaufene Listener werden entfernt
                    if (control && !thread.isAlive())
                        this.listener.remove(objects);

                    //der Listener wird ermittelt
                    listener = (Listener)objects[0];

                    //ueberzaehlige Listener werden beendet
                    if (control && listener.available())
                        listener.isolate();

                    //laeuft der Thread nicht, wird der Listener entfernt
                    if (listener.available() && thread.isAlive())
                        control = true;
                }

                //die Anzahl der nachtraeglich einzurichtenden Listener wird auf
                //Basis der letzten Anzahl ermittelt
                count = control ? 0 : volume <= 0 ? 1 : count +count +1;

                //liegt kein freier Listener vor, werden neue eingerichtet, die
                //Anzahl ist durch die Angabe vom MAXACCESS begrenzt, weitere
                //Anfragen werden sonst im Backlog geparkt
                for (loop = count; !this.socket.isClosed() && loop > 0; loop--) {
                    
                    if (this.listener.size() >= volume && volume > 0)
                        break;

                    //der Listener wird eingerichtet
                    listener = new Listener(this.context, this.socket, (Initialize)this.initialize.clone());

                    //der Thread der Listener wird eingerichet, ueber den
                    //Service wird dieser automatisch als Daemon verwendet
                    thread = new Thread(listener);

                    //der Listener wird mit Thread registriert
                    this.listener.add(new Object[] {listener, thread});
                    
                    //der Listener wird als Thread gestartet
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
        
        //das Beenden vom Server wird eingeleitet
        this.destroy();
        
        //alle Listener werden zwangsweise beendet
        enumeration = this.listener.elements();
        while (enumeration.hasMoreElements())
            ((Listener)((Object[])enumeration.nextElement())[0]).destroy();

        //die Terminierung wird ausgegeben
        Service.print(("SERVER ").concat(this.caption).concat(" STOPPED"));
    }
}