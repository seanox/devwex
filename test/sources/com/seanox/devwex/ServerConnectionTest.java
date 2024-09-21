/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * Devwex, Experimental Server Engine
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.junit.Assert;
import org.junit.Test;

import com.seanox.test.HttpUtils;
import com.seanox.test.MockUtils;
import com.seanox.test.Pattern;

/** Test cases for {@link com.seanox.devwex.Server}. */
public class ServerConnectionTest extends AbstractStageTest {

    /**
     * Test if the expected/required servers are available.
     * @throws Exception
     */
    @Test
    public void testAcceptance_01()
            throws Exception {

        final Set<String> serverList = new HashSet<>();
        final String status = Service.details();
        for (final String statusLine : status.split("\\R")) {
            if (!statusLine.startsWith("SAPI:"))
                continue;
            serverList.add(statusLine);
        }
        Assert.assertEquals(20, serverList.size());
        
        final String assertStatus = MockUtils.readTestContent();
        for (final String statusLine : assertStatus.split("\\R")) {
            if (!statusLine.startsWith("SAPI:"))
                continue;
            serverList.remove(statusLine);
        }
        Assert.assertEquals(20, assertStatus.split("\\R").length);
        Assert.assertEquals(0, serverList.size());
    }

    /**
     * Initializes the HttpsURLConnection without a client certificate.
     * @throws Exception
     */
    private static void initHttpsUrlConnection()
            throws Exception {
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] {new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
            }
            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
            }
        }}, null);        

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);
    }

    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = OFF} 
     * Connection without client certificate must works.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_02()
            throws Exception {
        ServerConnectionTest.initHttpsUrlConnection();
        final URL url = new URL("https://127.0.0.1:18443");
        final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
        Assert.assertEquals(200, urlConn.getResponseCode());
    }
    
    /** 
     * Test case for acceptance.
     * Connections with SSL/TLS must works.
     * Cross connections does not work.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_03()
            throws Exception {
        String response;
        final String request = "GET / HTTP/1.0\r\n"
                + "\r\n";
        response = new String(HttpUtils.sendRequest("127.0.0.1:18443", request));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_DIFFUSE));
        response = new String(HttpUtils.sendRequest("127.0.0.1:18443", request, AbstractStage.getKeystore()));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
    } 
    
    /** 
     * Test case for acceptance.
     * Connections with SSL/TLS must works.
     * Cross connections does not work.
     * @throws Exception
     */        
    @Test(expected=SSLException.class)
    public void testAcceptance_04()
            throws Exception {
        final String request = "GET / HTTP/1.0\r\n"
                + "\r\n";
        HttpUtils.sendRequest("127.0.0.1:18080", request, AbstractStage.getKeystore());
        Assert.fail();
    }

    /**
     * Initializes the HttpsURLConnection with a client certificate.
     * @throws Exception
     */    
    private static void initHttpsMutualAuthenticationUrlConnection(final File certificate, final String password)
            throws Exception {
        
        final KeyStore clientStore = KeyStore.getInstance("PKCS12");
        clientStore.load(new FileInputStream(certificate), password.toCharArray());
        
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientStore, ("changeIt").toCharArray());
        final KeyManager[] kms = kmf.getKeyManagers();

        final KeyStore trustStore = KeyStore.getInstance("PKCS12");
        trustStore.load(new FileInputStream(new File(AbstractStage.getRootStageCertificates(), "client.keystore")), ("changeIt").toCharArray());

        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        final TrustManager[] tms = tmf.getTrustManagers();

        final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(kms, tms, new SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);
    }    

    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = OFF} 
     * Connection with client certificate must ignore.
     * @throws Exception
     */
    @Test
    public void testAcceptance_05()
            throws Exception {
        final File certificate = new File(AbstractStage.getRootStageCertificates(), "client_a.p12");
        ServerConnectionTest.initHttpsMutualAuthenticationUrlConnection(certificate, "changeIt");
        final URL url = new URL("https://127.0.0.1:18443");
        final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
        Assert.assertEquals(200, urlConn.getResponseCode());
    }
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = OFF} 
     * Connection with an unknown client certificate must ignore.
     * @throws Exception
     */
    @Test
    public void testAcceptance_06()
            throws Exception {
        final File certificate = new File(AbstractStage.getRootStageCertificates(), "client_x.p12");
        ServerConnectionTest.initHttpsMutualAuthenticationUrlConnection(certificate, "changeIt");
        final URL url = new URL("https://127.0.0.1:18443");
        final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
        Assert.assertEquals(200, urlConn.getResponseCode());
    }      
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = ON} 
     * Connection without client certificate must fail.
     * @throws Exception
     */   
    @Test
    public void testAcceptance_07()
            throws Exception {
        ServerConnectionTest.initHttpsUrlConnection();
        final URL url = new URL("https://127.0.0.2:18443");
        final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
        try {Assert.assertNotEquals(200, urlConn.getResponseCode());
        } catch (Exception exception) {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            exception.printStackTrace(new PrintStream(output));
            if (output.toString().contains("SSLException")
                    || (output.toString().contains("SSLSocketImpl")
                            && exception instanceof SocketException))
                return;
        }
        Assert.fail();
    }       
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = ON} 
     * Connection with client certificate must works.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_08()
            throws Exception {
        final File certificate = new File(AbstractStage.getRootStageCertificates(), "client_a.p12");
        ServerConnectionTest.initHttpsMutualAuthenticationUrlConnection(certificate, "changeIt");
        final URL url = new URL("https://127.0.0.2:18443");
        final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
        Assert.assertEquals(200, urlConn.getResponseCode());
    }   
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = AUTO} 
     * Connection without client certificate must works.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_09()
            throws Exception {
        ServerConnectionTest.initHttpsUrlConnection();
        final URL url = new URL("https://127.0.0.3:18443");
        final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
        Assert.assertEquals(200, urlConn.getResponseCode());
    } 
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = AUTO} 
     * Connection with client certificate must works.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_10()
            throws Exception {
        final File certificate = new File(AbstractStage.getRootStageCertificates(), "client_a.p12");
        ServerConnectionTest.initHttpsMutualAuthenticationUrlConnection(certificate, "changeIt");
        final URL url = new URL("https://127.0.0.3:18443");
        final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
        Assert.assertEquals(200, urlConn.getResponseCode());
    } 
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = OFF} 
     * Connection with known and unknown client certificates must works.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_11()
            throws Exception {
        for (final char client : ("abcx").toCharArray()) {
            final File certificate = new File(AbstractStage.getRootStageCertificates(), "client_" + client + ".p12");
            ServerConnectionTest.initHttpsMutualAuthenticationUrlConnection(certificate, "changeIt");
            final URL url = new URL("https://127.0.0.1:18443");
            final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
            Assert.assertEquals(200, urlConn.getResponseCode());
        }
    } 
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = ON} 
     * Connection with known client certificates must works.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_12()
            throws Exception {
        for (final char client : ("abc").toCharArray()) {
            final File certificate = new File(AbstractStage.getRootStageCertificates(), "client_" + client + ".p12");
            ServerConnectionTest.initHttpsMutualAuthenticationUrlConnection(certificate, "changeIt");
            final URL url = new URL("https://127.0.0.2:18443");
            final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
            Assert.assertEquals(200, urlConn.getResponseCode());
        }
    }     

    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = ON} 
     * Connection with unknown client certificate must fail.
     * @throws Exception
     */      
    @Test(expected=SSLException.class)
    public void testAcceptance_13()
            throws Exception {
        final File certificate = new File(AbstractStage.getRootStageCertificates(), "client_x.p12");
        ServerConnectionTest.initHttpsMutualAuthenticationUrlConnection(certificate, "changeIt");
        final URL url = new URL("https://127.0.0.2:18443");
        final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
        Assert.assertNotEquals(200, urlConn.getResponseCode());
        Assert.fail();
    }     
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = AUTO}
     * Connection with known and unknown client certificates must work.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_14()
            throws Exception {
        for (final char client : ("abcx").toCharArray()) {
            final File certificate = new File(AbstractStage.getRootStageCertificates(), "client_" + client + ".p12");
            ServerConnectionTest.initHttpsMutualAuthenticationUrlConnection(certificate, "changeIt");
            final URL url = new URL("https://127.0.0.3:18443");
            final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
            Assert.assertEquals(200, urlConn.getResponseCode());
        }
    }
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = AUTO} + {@code MUTUAL AUTH = ALL IS EMPTY auth_cert} 
     * Connection with client certificate must works.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_15()
            throws Exception {
        for (final char client : ("abc").toCharArray()) {
            final File certificate = new File(AbstractStage.getRootStageCertificates(), "client_" + client + ".p12");
            ServerConnectionTest.initHttpsMutualAuthenticationUrlConnection(certificate, "changeIt");
            final URL url = new URL("https://127.0.0.4:18443");
            final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
            Assert.assertEquals(200, urlConn.getResponseCode());
        }
    }    
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = AUTO} + {@code MUTUAL AUTH = ALL IS EMPTY auth_cert} 
     * Connection with an unknown client certificate must be denied with status 403.
     * @throws Exception
     */
    @Test
    public void testAcceptance_16()
            throws Exception {
        final File certificate = new File(AbstractStage.getRootStageCertificates(), "client_x.p12");
        ServerConnectionTest.initHttpsMutualAuthenticationUrlConnection(certificate, "changeIt");
        final URL url = new URL("https://127.0.0.4:18443");
        final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
        Assert.assertEquals(403, urlConn.getResponseCode());
    }  
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code CLIENTAUTH = AUTO} + {@code MUTUAL AUTH = ALL IS EMPTY auth_cert} 
     * Connection without a client certificate must works.
     * @throws Exception
     */
    @Test
    public void testAcceptance_17()
            throws Exception {
        ServerConnectionTest.initHttpsUrlConnection();
        final URL url = new URL("https://127.0.0.4:18443");
        final HttpsURLConnection urlConn = (HttpsURLConnection)url.openConnection();
        Assert.assertEquals(403, urlConn.getResponseCode());
    }    
}