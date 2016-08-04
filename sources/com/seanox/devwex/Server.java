/**
 *  LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 *  im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 *  Devwex, Advanced Server Developing
 *  Copyright (C) 2013 Seanox Software Solutions
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

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
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
 *  Server 1.2013.0606<br>
 *  Copyright (C) 2013 Seanox Software Solutions<br>
 *  Alle Rechte vorbehalten.
 *
 *  @author  Seanox Software Solutions
 *  @version 1.2013.0606
 */
public class Server implements Runnable {

    /** Konfiguration des Servers */
    public volatile Initialize initialize;
    
    /** Socket des Servers */
    private volatile ServerSocket socket;
    
    /** Context/Name des Servers */
    private volatile String context;

    /** Kennung und Bezeichnung des Servers */
    private volatile String caption;

    /** Session Objekte der eingerichteten Verbindungen */
    private volatile Vector listener;

    /**
     *  Konstruktor, richtet den Server entsprechenden der Konfiguration ein.
     *  @param  name Name des Servers
     *  @param  data Konfigurationsdaten des Servers
     *  @throws Throwable bei fehlerhafter Einrichtung des Servers
     */
    public Server(String name, Object data) throws Throwable {

        Enumeration       enumeration;
        InetAddress       address;
        KeyManagerFactory manager;
        KeyStore          store;
        SSLContext        context;
        String            buffer;
        String            string;
        StringTokenizer   tokenizer;
        Section           options;
        Section           section;
        
        byte[]            bytes;

        int               port;
        int               isolation;
        int               volume;
        
        //der Servername wird uebernommen
        this.context = name == null ? "" : name.trim();

        //die Konfiguration wird eingerichtet
        this.initialize = (Initialize)((Initialize)data).clone();

        //Die Mimetypes werden fuer einen schnelleren Zugriff umgeschrieben.
        //Dazu bilden die Dateiendungen den Schluessel und der Mimetype den
        //Wert. Zur Konfiguration ist der Mimetype als Schluessel einfacher.
        
        //die Mimetypes werden eingerichtet
        //die Mimetypes werden eingerichtet
        section = new Section(true);

        //die Mimetypes werden ermittelt
        options = this.initialize.get("mimetypes");

        if (options != null) {

            //die Contenttypes werden ermittelt
            enumeration = options.elements();
    
            //die Mimetypes werden entsprechend den Dateiendungen aufgebaut
            //unvollstaendige Eintraege werden nicht beruecksichtig
            while (enumeration.hasMoreElements()) {
    
                //der Mimetype wird ermittelt
                string = (String)enumeration.nextElement();
    
                //die Dateiendungen werden ermittelt
                tokenizer = new StringTokenizer(options.get(string));
                while (tokenizer.hasMoreTokens()) {
                    buffer = tokenizer.nextToken().trim();
                    if (buffer.length() > 0)
                        section.set(buffer, string.toLowerCase());
                }
            }
    
            //die Mimetypes werden uberschrieben
            options.clear();
            options.merge(section);
        }
        
        //die Serverkonfiguration wird ermittelt
        options = this.initialize.get(this.context.concat(":bas"));

        //die Hostadresse des Servers wird ermittelt
        string  = options.get("address").toLowerCase();
        address = string.equals("auto") ? null : InetAddress.getByName(string);

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
        if (this.initialize.contains(this.context.concat("ssl"))) {

            //die SSL Konfiguration wird ermittelt
            options = this.initialize.get(this.context.concat("ssl"));

            //der Typ des KeyStores wird ermittelt, Standard ist JKS
            store = KeyStore.getInstance(options.get("type"), "JKS");
            
            //der SSL Algorithmus wird ermittelt, Standard ist SunX509
            manager = KeyManagerFactory.getInstance(options.get("algorithm", "SunX509"));

            //das Passwort des KeyStores wird ermittelt
            string = options.get("password");

            //der KeyStore Datenstrom wird ermittelt und eingerichtet
            bytes = Files.readAllBytes(Paths.get(options.get("store"), new String[0]));
            
            //der KeyStore wird geladen
            store.load(new ByteArrayInputStream(bytes), string.toCharArray());

            //der KeyStore wird initialisiert
            manager.init(store, string.toCharArray());

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
        this.caption = ("TCP ").concat(address.getHostAddress()).concat(":").concat(String.valueOf(port));
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

        Listener    session;
        Section     options;
        Thread      thread;
        Enumeration enumeration;

        Object[]    objects;

        boolean     control;

        int         count;
        int         loop;
        int         volume;

        //die Sessions werden eingerichtet
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

        //die initiale Anzahl zusaetzlicher Sessions wird angelegt
        count = 0;

        try {

            while (!this.socket.isClosed()) {

                control = false;
                
                enumeration = ((Vector)this.listener.clone()).elements();
                while (enumeration.hasMoreElements() && !this.socket.isClosed()) {

                    //objects = (Object[])this.sessions.get(loop);
                    objects = (Object[])enumeration.nextElement();

                    //der Thread wird ermittelt
                    thread = (Thread)objects[1];

                    //ausgelaufene Sessions werden entfernt
                    if (control && !thread.isAlive())
                        this.listener.remove(objects);

                    //die Session wird ermittelt
                    session = (Listener)objects[0];

                    //ueberzaehlige Sessions werden beendet
                    if (control && session.available())
                        session.isolate();

                    //laeuft der Thread nicht, wird die Session entfernt
                    if (session.available() && thread.isAlive())
                        control = true;
                }

                //die Anzahl der nachtraeglich einzurichtenden Sessions auf
                //Basis der letzten Anzahl ermittelt
                count = control ? 0 : volume <= 0 ? 1 : count +count +1;

                //liegt keine freie Session vor, werden neue eingerichtet, die
                //Anzahl ist durch die Angabe vom MAXACCESS begrenzt, weitere
                //Anfragen werden sonst im Backlog geparkt
                for (loop = count; !this.socket.isClosed() && loop > 0; loop--) {
                    
                    if (this.listener.size() >= volume && volume > 0)
                        break;

                    //die Session wird eingerichtet
                    session = new Listener(this.context, this.socket, (Initialize)this.initialize.clone());

                    //der Thread der Session wird eingerichet, ueber den
                    //Service wird dieser automatisch als Daemon verwendet
                    thread = new Thread(session);

                    //die Session wird mit Thread registriert
                    this.listener.add(new Object[] {session, thread});
                    
                    //die Session wird als Thread gestartet
                    thread.start();
                }

                Service.sleep(25);
            }

        } catch (Throwable throwable) {
            Service.print(throwable);
        }
        
        //das Beenden vom Server wird eingeleitet
        this.destroy();
        
        //alle Sessions werden zwangsweise beendet
        enumeration = this.listener.elements();
        while (enumeration.hasMoreElements())
            ((Listener)((Object[])enumeration.nextElement())[0]).destroy();

        //die Terminierung wird ausgegeben
        Service.print(("SERVER ").concat(this.caption).concat(" STOPPED"));
    }
}