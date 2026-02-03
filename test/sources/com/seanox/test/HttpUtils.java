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
package com.seanox.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.LinkedList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/** Utilities for HTTP(S) connections. */
public class HttpUtils {
    
    /** Constructor, creates a new HttpUtils object. */
    private HttpUtils() {
    }    
    
    /** Selection of header fields */
    public static class HeaderField {
        
        /** constant for Last-Modified */
        public static final String LAST_MODIFIED = "Last-Modified";

        /** constant for Content-Length */
        public static final String CONTENT_LENGTH = "Content-Length";

        /** constant for Content-Type */
        public static final String CONTENT_TYPE = "Content-Type";

        /** constant for WWW-Authenticate */
        public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    }
    
    public static String[] getResponseHeader(String response, String... fields) {

        String pattern = "";
        if (fields == null)
            fields = new String[0];
        for (String field : fields)
            pattern += "|\\Q" + field.trim() + "\\E";
        if (!pattern.isEmpty())
            pattern = pattern.substring(1);

        final String[] headerLines = response.replaceAll("(?s)^[\r\n]+(.*?)\r\n\r\n.*$", "$1").split("[\r\n]+");
        final LinkedList<String> headerList = new LinkedList<>();
        for (final String header : headerLines) {
            if (!pattern.isEmpty()
                    && !header.matches("(?i)^(" + pattern + ").*$"))
                continue;
            headerList.add(header);
        }
        
        return headerList.toArray(new String[0]);
    }

    /**
     * Returns the value of a header field.
     * If the field does not exist, these methods return {@code null}.
     * @param  response response
     * @param  field    field
     * @return the value of a header field, otherwise {@code null}
     */
    public static String getResponseHeaderValue(String response, String field) {
        final String[] fields = HttpUtils.getResponseHeader(response, field);
        if (fields.length <= 0)
            return null;
        return fields[0].replaceAll("^[^:]+:\\s*(.*)\\s*$", "$1");
    }
    
    /**
     * Checks exists one or a set of response header fields.
     * @param  response response
     * @param  fields   fields
     * @return {@code true} if all passed fields exists
     */
    public static boolean exitsResponseHeader(String response, String... fields) {
        if (fields == null)
            fields = new String[0];
        if (fields.length <= 0)
            return false;
        for (final String field : fields)
            if (HttpUtils.getResponseHeader(response, field).length <= 0)
                return false;
        return true;
    }
    
    /** Interface to implement a keystore for SSL connections. */
    public interface Keystore {
        
        /**
         * Returns the keystore file.
         * @return the keystore file
         */
        File getFile();

        /**
         * Returns the keystore password.
         * @return the keystore password
         */
        String getPassword();
    }
    
    /**
     * Creates a socket for an HTTP(S) connection.
     * The decision whether HTTP or HTTPS is made by specifying a keystore.
     * HTTPS requires a keystore.
     * @param  address  address
     * @param  keystore keystore
     * @return the created socket
     * @throws IOException
     * @throws GeneralSecurityException 
     */
    private static Socket createSocket(String address, Keystore keystore, int timeout)
            throws IOException, GeneralSecurityException {

        if (keystore == null) {
            final Socket socket = new Socket(address.replaceAll(Pattern.NETWORK_CONNECTION, "$1"),
                    Integer.parseInt(address.replaceAll(Pattern.NETWORK_CONNECTION, "$2")));
            socket.setSoTimeout(timeout);
            socket.setSoLinger(true, timeout);
            return socket;
        }

        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(keystore.getFile()), keystore.getPassword().toCharArray());

        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystore.getPassword().toCharArray());

        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);

        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        final SSLSocket sslSocket = (SSLSocket)sslSocketFactory.createSocket(address.replaceAll(Pattern.NETWORK_CONNECTION, "$1"),
                Integer.parseInt(address.replaceAll(Pattern.NETWORK_CONNECTION, "$2")));
        sslSocket.startHandshake();
        sslSocket.setSoTimeout(timeout);
        sslSocket.setSoLinger(true, timeout);
        
        return sslSocket;
    }

    /**
     * Sends an HTTP request to a server.
     * @param  address address
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */    
    public static byte[] sendRequest(String address)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, null, (Keystore)null);
    }

    /**
     * Sends an HTTP request to a server.
     * @param  address address
     * @param  timeout timeout
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */    
    public static byte[] sendRequest(String address, int timeout)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, null, (Keystore)null, timeout);
    }

    /**
     * Sends an HTTP request to a server.
     * @param  address  address
     * @param  keystore keystore
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, Keystore keystore)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, null, keystore);
    }

    /**
     * Sends an HTTP request to a server.
     * @param  address  address
     * @param  keystore keystore
     * @param  timeout  timeout
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, Keystore keystore, int timeout)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, null, keystore, timeout);
    }

    /**
     * Sends an HTTP request to a server.
     * @param  address address
     * @param  request request
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, request, (Keystore)null);
    }
    
    /**
     * Sends an HTTP request to a server.
     * @param  address address
     * @param  request request
     * @param  timeout timeout
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, int timeout)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, request, (Keystore)null, timeout);
    }    
    
    /**
     * Sends an HTTP request to a server.
     * @param  address address
     * @param  request request
     * @param  data    data
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, InputStream data)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, request, data, null);
    }  
    
    /**
     * Sends an HTTP request to a server.
     * @param  address address
     * @param  request request
     * @param  data    data
     * @param  timeout timeout
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, InputStream data, int timeout)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, request, data, null, timeout);
    }     

    /**
     * Sends an HTTP request to a server.
     * @param  address  address
     * @param  request  request
     * @param  keystore keystore
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, Keystore keystore)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, request, (InputStream)null, keystore);
    }
    
    /**
     * Sends an HTTP request to a server.
     * @param  address  address
     * @param  request  request
     * @param  keystore keystore
     * @param  timeout  timeout
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, Keystore keystore, int timeout)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, request, null, keystore, timeout);
    }    
    
    /**
     * Sends an HTTP request to a server.
     * @param  address  address
     * @param  request  request
     * @param  data     data
     * @param  keystore keystore
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, InputStream data, Keystore keystore)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, request, data, keystore, 65535);
    }
    
    /**
     * Sends an HTTP request to a server.
     * @param  address  address
     * @param  request  request
     * @param  data     data
     * @param  keystore keystore  
     * @param  timeout  timeout
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, InputStream data, Keystore keystore, int timeout)
            throws IOException, GeneralSecurityException {
        
        if (!address.matches(Pattern.NETWORK_CONNECTION))
            throw new IllegalArgumentException("Invalid connection string: " + address + ", expected <host>:<port>");
    
        try (final Socket socket = HttpUtils.createSocket(address, keystore, timeout)) {
            if (request != null) {
                final OutputStream output = socket.getOutputStream();
                output.write(request.getBytes());
                output.flush();
                if (data != null)
                    StreamUtils.forward(data, output);
            }
            return StreamUtils.read(socket.getInputStream());   
        }
    }
    
    /** Interface to implements RequestEvent. */
    public interface RequestEvent {
        
        /**
         * The methods are called when the server sends a response.
         * @param response response
         */
        void onResponse(byte[] response);

        /**
         * The methods are called when an error occurs during the connection.
         * @param exception exception
         */
        void onException(Exception exception);
    }
    
    /**
     * Sends an HTTP request to a server.
     * The received response is handelt as {@link RequestEvent}.
     * @param address address
     * @param request request
     * @param event   event
     */
    public static void sendRequest(String address, String request, HttpUtils.RequestEvent event) {
        HttpUtils.sendRequest(address, request, event, null);
    }

    /**
     * Sends an HTTP request to a server.
     * The received response is handelt as {@link RequestEvent}.
     * @param address  address
     * @param request  request
     * @param event    event
     * @param keystore keystore
     */
    public static void sendRequest(String address, String request, HttpUtils.RequestEvent event, Keystore keystore) {
        new Thread(() -> {
            try {
                final byte[] response = sendRequest(address, request, keystore);
                if (event != null)
                    event.onResponse(response);
            } catch (Exception exception) {
                if (event != null)
                    event.onException(exception);
            }
        }).start();
    }
    
    /** Abstract class to implement an authentication. */
    public abstract static class Authentication {
        
        private String user;
        
        private String password;
        
        private Authentication(String user, String password) {
            this.user     = user;
            this.password = password;
        }

        /** Basic Authentication */
        public static class Basic extends Authentication {
            
            /** 
             * Constructor, creates a new Basic Authentication.
             * @param user     user
             * @param password password
             */
            public Basic(String user, String password) {
                super(user, password);
            }
        }

        /** Digest Authentication */
        public static class Digest extends Authentication {
            
            /** 
             * Constructor, creates a new Digest Authentication.
             * @param user     user
             * @param password password
             */
            public Digest(String user, String password) {
                super(user, password);
            }
        }
    }
    
    /**
     * Sends an HTTP request to a server.
     * @param  address        address
     * @param  request        request
     * @param  authentication authentication
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, Authentication authentication)
            throws IOException, GeneralSecurityException {
        return HttpUtils.sendRequest(address, request, authentication, null);
    }
    
    private static class DigestAuthenticate {
        
        private String realm;
        private String qop;
        private String nonce;
        private String opaque;
        private String nc;
        private String cnonce;
        
        private static DigestAuthenticate create(String header)
                throws NoSuchAlgorithmException {
            final String authenticate = header.replaceAll("(?si)^.*\r\n(WWW-Authenticate: Digest [^\r\n]+).*$", "$1");
            final String algorithm = authenticate.replaceAll("(?i).* algorithm=\"([^\"]+).*$", "$1");
            final MessageDigest secure = MessageDigest.getInstance(algorithm);
            final DigestAuthenticate digestAuthenticate = new DigestAuthenticate();
            digestAuthenticate.realm  = authenticate.replaceAll("(?i).* realm=\"([^\"]*).*$", "$1");
            digestAuthenticate.qop    = authenticate.replaceAll("(?i).* qop=\"([^\"]*).*$", "$1");
            digestAuthenticate.nonce  = authenticate.replaceAll("(?i).* nonce=\"([^\"]*).*$", "$1");
            digestAuthenticate.opaque = authenticate.replaceAll("(?i).* opaque=\"([^\"]*).*$", "$1");
            digestAuthenticate.nc     = "00000001";
            digestAuthenticate.cnonce = new Date().toString();
            digestAuthenticate.cnonce = Codec.encodeHex(secure.digest(digestAuthenticate.cnonce.getBytes()));
            return digestAuthenticate;
        }
        
        /**
         * Creates an HTTP header for authorization via digest.
         * @param  header   HTTP-Header
         * @param  method   HTTP-Methoden
         * @param  uri      URI
         * @param  user     User
         * @param  password Password
         * @return HTTP header for authorization via digest
         * @throws NoSuchAlgorithmException 
         */
        private static String create(String header, String method, String uri, String user, String password)
                throws NoSuchAlgorithmException {

            final MessageDigest secure = MessageDigest.getInstance("md5");
            final DigestAuthenticate digestAuthenticate = DigestAuthenticate.create(header);

            String s1 = user + ":" + digestAuthenticate.realm + ":" + password;
            s1 = Codec.encodeHex(secure.digest(s1.getBytes()));
            String s2 = method + ":" + uri;
            s2 = Codec.encodeHex(secure.digest(s2.getBytes()));
            String s3 = ":" + digestAuthenticate.nonce + ":" + digestAuthenticate.nc + ":" + digestAuthenticate.cnonce + ":" + digestAuthenticate.qop + ":";
            s3 = Codec.encodeHex(secure.digest((s1 + s3 + s2).getBytes()));

            return "Authorization: Digest username=\"" + user + "\""
                   + ", algorithm=\"MD5\""
                   + ", nc=\"00000001\""
                   + ", qop=\"auth\""
                   + ", uri=\"" + uri + "\""
                   + ", nonce=\"" + digestAuthenticate.nonce + "\""
                   + ", cnonce=\"" + digestAuthenticate.cnonce + "\""
                   + ", response=\"" + s3 + "\""
                   + ", opaque=\"" + digestAuthenticate.opaque + "\""
                   + "\r\n";
        }
    }
    
    /**
     * Sends an HTTP request to a server.
     * @param  address        address
     * @param  request        request
     * @param  authentication authentication
     * @param  keystore       keystore
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static byte[] sendRequest(String address, String request, Authentication authentication, Keystore keystore)
            throws IOException, GeneralSecurityException {
        
        if (authentication.user == null)
            authentication.user = "";
        if (authentication.password == null)
            authentication.password = "";
        
        if (authentication instanceof Authentication.Digest) {

            final byte[] responseData = HttpUtils.sendRequest(address, request, keystore);
            final String response = new String(responseData);
            final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
            if (!header.matches("(?si)^.*\r\nWWW-Authenticate: Digest .*$"))
                return responseData;

            final String method = request.split(" ")[0];
            final String uri    = request.split(" ")[1];
            
            int index = request.indexOf("\r\n\r\n");
            request = request.substring(0, index +2)
                    + DigestAuthenticate.create(header, method, uri, authentication.user, authentication.password)
                    + request.substring(index);
            
        } else {

            int index = request.indexOf("\r\n\r\n");
            request = request.substring(0, index +2)
                    + "Authorization: Basic " + Codec.encodeBase64(authentication.user + ":" + authentication.password) + "\r\n"
                    + request.substring(index);
        }
        
        return HttpUtils.sendRequest(address, request, keystore);
    }
}