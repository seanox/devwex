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
package com.seanox.devwex;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import com.seanox.test.HttpUtils;
import com.seanox.test.Pattern;
import com.seanox.test.TextUtils;

/** Test cases for {@link com.seanox.devwex.Worker}. */
public class WorkerAccessLogTest extends AbstractStageRequestTest {
    
    /** 
     * Test case for acceptance.
     * The usage of time symbols in the file name must work.
     * @throws Exception
     */
    @Test
    public void testAcceptance_1()
            throws Exception {
        
        final String accessLog = new SimpleDateFormat("'access-'yyyyMMdd'.log'").format(new Date());
        final File accessLogFile = new File(AbstractStage.getRootStage(), accessLog);
        accessLogFile.delete();
        
        final String request = "GET / HTTP/1.0\r\n"
                + "Host: vHk\r\n"
                + "\r\n"; 
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18080", request));
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        
        Thread.sleep(AbstractStageRequestTest.SLEEP);
        Assert.assertTrue(accessLogFile.exists());
    }
    
    /** 
     * Test case for acceptance.
     * The time zone must be set correctly in the access log.
     * @throws Exception
     */
    @Test
    public void testAcceptance_2()
            throws Exception {
        
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18080", "GET / HTTP/1.0\r\n\r\n");
        final String pattern = new SimpleDateFormat("Z", Locale.US).format(new Date());
        Thread.sleep(AbstractStageRequestTest.SLEEP);
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches("^.*?\\[\\d{2}/\\w{3}/\\d{4}(:\\d{2}){3} \\" + pattern + "\\]\\s.*$"));
    }   

    /** 
     * Test case for acceptance.
     * Special characters (\, ") must be escaped.
     * @throws Exception
     */
    @Test
    public void testAcceptance_3()
            throws Exception {
        
        String request;
        
        request = "G\"ET /nix\"xxx\"_zzz\u00FF HTTP/1.0\r\n"
                + "UUID: Nix\"123\"\r\n";
        HttpUtils.sendRequest("127.0.0.1:18080", request + "\r\n");

        request = "GET / HTTP/1.0\r\n";
        AbstractStageRequestTest.sendRequest("127.0.0.1:18080", request + "\r\n");

        final String accessLog = AbstractStage.getAccessStreamCapture().toString();
        Assert.assertTrue(accessLog, accessLog.contains(" \"G\\\"ET /nix\\\"xxx\\\"_zzz\\xFF HTTP/1.0\" "));
        Assert.assertTrue(accessLog, accessLog.contains(" \"Nix\\\"123\\\""));
    }
    
    /** 
     * Test case for acceptance.
     * The usage of CGI variables in the file name must work.
     * @throws Exception
     */
    @Test
    public void testAcceptance_4()
            throws Exception {
        
        final String accessLog = new SimpleDateFormat("'access-'yyyyMMdd'_vHl.log'").format(new Date());
        final File accessLogFile = new File(AbstractStage.getRootStage(), accessLog);
        accessLogFile.delete();
        
        final String request = "GET / HTTP/1.0\r\n"
                + "Host: vHl\r\n"
                + "\r\n"; 
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18080", request));
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        
        Thread.sleep(AbstractStageRequestTest.SLEEP);
        Assert.assertTrue(accessLogFile.exists());
    }
    
    /** 
     * Test case for acceptance.
     * Unicode characters must be ignored as default ASCII.
     * @throws Exception
     */
    @Test
    public void testAcceptance_5()
            throws Exception {

        final String request = TextUtils.normalize("GET /nix_xxx_zzz\ud801\udc00 HTTP/1.0\r\n", StandardCharsets.ISO_8859_1);
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18080", request + "\r\n");
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.contains(" \"GET /nix_xxx_zzz? HTTP/1.0\" "));
    }    
    
    /** 
     * Test case for acceptance.
     * Unicode characters in UTF-8 must be escaped.
     * @throws Exception
     */
    @Test
    public void testAcceptance_6()
            throws Exception {
        
        final String request = "GET /nix_xxx_zzz" + URLEncoder.encode("\ud801\udc00", "UTF-8") +  " HTTP/1.0\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18080", request + "\r\n");
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.contains(" \"GET /nix_xxx_zzz%F0%90%90%80 HTTP/1.0\" "));
    }    
}