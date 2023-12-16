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

import org.junit.Assert;
import org.junit.Test;

import com.seanox.test.Codec;
import com.seanox.test.HttpUtils;
import com.seanox.test.HttpUtils.Authentication;
import com.seanox.test.HttpUtils.Authentication.Digest;
import com.seanox.test.HttpUtils.HeaderField;
import com.seanox.test.Pattern;

/**
 * Test cases for {@link com.seanox.devwex.Worker}.
 *
 * @author  Seanox Software Solutions
 * @version 5.5.0 20220908
 */
public class WorkerTest_AuthenticationDigest extends AbstractStageRequestTest {
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code /authentication/a [acc:usr-a:pwd-a:Section-A]}
     * Without {@code usr-a:pwd-a} the request is responded with status 401.
     * @throws Exception
     */
    @Test
    public void testAcceptance_01()
            throws Exception {
        
        final String request = "GET /authentication/a/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST("Section-A"))); 
        final String header = HttpUtils.getResponseHeaderValue(response, HttpUtils.HeaderField.WWW_AUTHENTICATE);
        Assert.assertTrue(header.matches("^.*, qop=\"auth\".*$"));
        Assert.assertTrue(header.matches("^.*, nonce=\"\\w+\".*$"));
        Assert.assertTrue(header.matches("^.*, opaque=\"\\w+\".*$"));
        Assert.assertTrue(header.matches("^.*, algorithm=\"MD5\".*$"));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
    } 
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code /authentication/a/b/d [acc:none]}
     * Without login/password the request is responded with status 200.
     * Access is also possible without login/password.
     * @throws Exception
     */
    @Test
    public void testAcceptance_02()
            throws Exception {
        
        final String request = "GET /authentication/a/b/d/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));
    } 
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code /authentication/a [acc:usr-a:pwd-a:Section-A]}
     * Authentication {@code usr-a:pwd-a} is required for the access.
     * @throws Exception
     */
    @Test
    public void testAcceptance_03()
            throws Exception {
        
        String request;
        String response;
        String accessLog;
        
        request = "GET /authentication/a/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-a", "pwd-a"));
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, "usr-a")));
    } 
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code /authentication/a/b [acc:usr-b:pwd-b:Section-B]}
     * Authentication {@code usr-b:pwd-b} is required for the access.
     * @throws Exception
     */
    @Test
    public void testAcceptance_04()
            throws Exception {
        
        String request;
        String response;
        String accessLog;
        
        request = "GET /authentication/a/b/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 
        
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-b", "pwd-b"));

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST("Section-A")));
         
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
    }
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code /authentication/a/b [acc:usr-b:pwd-b:Section-B]}
     * Authentication {@code usr-b:pwd-b} is required for the access.
     * @throws Exception
     */
    @Test
    public void testAcceptance_05()
            throws Exception {
        
        String request;
        String response;
        String accessLog;
        
        request = "GET /authentication/a/b/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/b/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-b", "pwd-b"));

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, "usr-b")));
    }
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code /authentication/a [acc:usr-a:pwd-a:Section-A]}
     * Authentication {@code usr-a:pwd-a} is required for the access.
     * Transmitted was: {@code usr-b:pwd-b}
     * The request is responded with status 401.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_06()
            throws Exception {
        
        String request;
        String response;
        String accessLog;
        
        request = "GET /authentication/a/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-b", "pwd-b"));

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));   
    }
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code /authentication/a/b/c [acc:usr-a:pwd-a:Section-A2]}
     * Authentication {@code usr-a:pwd-a} is required for the access.
     * Transmitted was: {@code usr-a:pwd-a}
     * The request is responded with status 200.
     * @throws Exception
     */       
    @Test
    public void testAcceptance_07()
            throws Exception {
        
        String request;
        String response;
        String accessLog;
        
        request = "GET /authentication/a/b/c/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/b/c/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-a", "pwd-a"));

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, "usr-a")));
    }
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code /authentication/a/b/c [acc:usr-a:pwd-a:Section-A2]}
     * Authentication {@code usr-a:pwd-a} is required for the access.
     * Transmitted was: nothing
     * The request is responded with status 401.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_08()
            throws Exception {
        
        final String request = "GET /authentication/a/b/c/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST("Section-A2")));
        final String header = HttpUtils.getResponseHeaderValue(response, HttpUtils.HeaderField.WWW_AUTHENTICATE);
        Assert.assertTrue(header.matches("^.*, qop=\"auth\".*$"));
        Assert.assertTrue(header.matches("^.*, nonce=\"\\w+\".*$"));
        Assert.assertTrue(header.matches("^.*, opaque=\"\\w+\".*$"));
        Assert.assertTrue(header.matches("^.*, algorithm=\"MD5\".*$"));        

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
    }
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code /authentication/a/b/d/e [acc:usr-e:pwd-e:Section-E]}
     * Transmitted was: nothing
     * The request is responded with status 401.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_09()
            throws Exception {
        
        final String request = "GET /authentication/a/b/d/e/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST("Section-E")));
        final String header = HttpUtils.getResponseHeaderValue(response, HttpUtils.HeaderField.WWW_AUTHENTICATE);
        Assert.assertTrue(header.matches("^.*, qop=\"auth\".*$"));
        Assert.assertTrue(header.matches("^.*, nonce=\"\\w+\".*$"));
        Assert.assertTrue(header.matches("^.*, opaque=\"\\w+\".*$"));
        Assert.assertTrue(header.matches("^.*, algorithm=\"MD5\".*$"));  
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
    }

    /** 
     * Test case for acceptance.
     * Configuration: {@code /authentication/a/b/d/e/e [acc:usr-e:pwd-e:Section-E]}
     * Transmitted was: nothing
     * The request is responded with status 401.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_10()
            throws Exception {
        
        final String request = "GET /authentication/a/b/d/e/e/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST("Section-E")));
        final String header = HttpUtils.getResponseHeaderValue(response, HttpUtils.HeaderField.WWW_AUTHENTICATE);
        Assert.assertTrue(header.matches("^.*, qop=\"auth\".*$"));
        Assert.assertTrue(header.matches("^.*, nonce=\"\\w+\".*$"));
        Assert.assertTrue(header.matches("^.*, opaque=\"\\w+\".*$"));
        Assert.assertTrue(header.matches("^.*, algorithm=\"MD5\".*$"));  

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));        
    }
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code /authentication/a/b/d/e/e [acc:usr-e:pwd-e:Section-E]}
     * Authentication {@code usr-e:pwd-e} is required for the access.
     * Transmitted was: {@code usr-e:pwd-e}
     * The request is responded with status 200.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_11()
            throws Exception {
        
        String request;
        String response;
        String accessLog;        
        
        request = "GET /authentication/a/b/d/e/e/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/b/d/e/e/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-e", "pwd-e"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, "usr-e")));   
    }
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code /authentication/a/b/d/e/e [acc:usr-e:pwd-e:Section-E]}
     * Authentication {@code usr-e:pwd-e} is required for the access.
     * Transmitted was: {@code usr-e:pwd-e}
     * The request is responded with status 200.
     * @throws Exception
     */       
    @Test
    public void testAcceptance_12()
            throws Exception {
        
        String request;
        String response;
        String accessLog;        
        
        request = "GET /authentication/a/b/d/e/e HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/b/d/e/e HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-e", "pwd-e"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_302));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("302", request, "usr-e")));

        request = "GET /authentication/a/b/d/e/e/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_TEXT_HTML));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/b/d/e/e/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-e", "pwd-e"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, "usr-e")));
    }
    
    private void assertAcceptance_13(final String uri, final String status, final int auth)
            throws Exception {
        
        final String request = "GET " + uri + " HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        Authentication authentication = null;
        if (auth == 1)
            authentication = new Authentication.Digest("usr-e", "pwd-e");
        if (auth == 2)
            authentication = new Authentication.Digest("usr-a", "pwd-a");
        final String response;
        if (authentication != null)
            response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, authentication);
        else response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS(status)));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * The access to @{code /authentication/a/b/d/e/*} is defined in combination
     * with option @{code [C]}. ACC has the higher priority and must take
     * precedence over the option @{code [C]}.
     * All request with ACC are responded with status 401.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_13()
            throws Exception {
        
        this.assertAcceptance_13("/authentication/a/b/c/f", "401", 0);
        this.assertAcceptance_13("/authentication/a/b/c/g", "401", 0);
        this.assertAcceptance_13("/authentication/a/b/c/h", "401", 0);
        this.assertAcceptance_13("/authentication/a/b/c/i", "401", 0);

        this.assertAcceptance_13("/authentication/a/b/c/f", "403", 1);
        this.assertAcceptance_13("/authentication/a/b/c/g", "403", 1);
        this.assertAcceptance_13("/authentication/a/b/c/h", "403", 1);
        this.assertAcceptance_13("/authentication/a/b/c/i", "403", 1);

        this.assertAcceptance_13("/authentication/a/b/c/f", "401", 2);
        this.assertAcceptance_13("/authentication/a/b/c/g", "401", 2);
        this.assertAcceptance_13("/authentication/a/b/c/h", "401", 2);
        this.assertAcceptance_13("/authentication/a/b/c/i", "401", 2);
    }
    
    private void assertAcceptance_14(final String uri, final String realm, final int auth)
            throws Exception {
        
        final String request = "GET " + uri + " HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        Authentication authentication = null;
        if (auth == 1)
            authentication = new Authentication.Digest("usr-e", "pwd-e");
        if (auth == 2)
            authentication = new Authentication.Digest("usr-a", "pwd-a");
        final String response;
        if (authentication != null)
            response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, authentication);
        else response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST(realm)));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * The realm can be specified differently. Spaces at the beginning and end,
     * as well the quotation mark are to be suppressed in the HTTP header.
     * Without a realm, a clean HTTP header with realm must be sent.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_14()
            throws Exception {
        
        this.assertAcceptance_14("/authentication/r/a/", "", 0);
        this.assertAcceptance_14("/authentication/r/b/", "", 0);
        this.assertAcceptance_14("/authentication/r/c/", "", 0);
        this.assertAcceptance_14("/authentication/r/d/", "", 0);
        this.assertAcceptance_14("/authentication/r/e/", "", 0);
        this.assertAcceptance_14("/authentication/r/f/", "x", 0);
        this.assertAcceptance_14("/authentication/r/g/", "x", 0);
        this.assertAcceptance_14("/authentication/r/h/", "x", 0);
        this.assertAcceptance_14("/authentication/r/i/", "x", 0);
        this.assertAcceptance_14("/authentication/r/j/", "\\\"x\\\"", 0);
        this.assertAcceptance_14("/authentication/r/k/", "", 0);
    }
    
    private void assertAcceptance_15(final String uri, final String realm, final String auth)
            throws Exception {
        
        final String request = "GET " + uri + " HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        final String response;
        if (auth != null
                && !auth.trim().isEmpty()) {
            response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest(auth, null));
        } else response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);

        if (realm != null)
            Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST(realm)));      
        else
            Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * Users and realm are separated by the colon when encrypting. Below are
     * tested different combinations in the definition.   
     * @throws Exception
     */      
    @Test
    public void testAcceptance_15()
            throws Exception {
        
        this.assertAcceptance_15("/authentication/s/a/", "", null);
        this.assertAcceptance_15("/authentication/s/a/", "", null);
        this.assertAcceptance_15("/authentication/s/b/", "sb", null);
        this.assertAcceptance_15("/authentication/s/b/", "sb", null);

        this.assertAcceptance_15("/authentication/s/a/", "", "usrSa1:");
        this.assertAcceptance_15("/authentication/s/a/", null, "usrSa2:");
        this.assertAcceptance_15("/authentication/s/b/", "sb", "usrSb1:");
        this.assertAcceptance_15("/authentication/s/b/", null, "usrSb2:");
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * {@code /authentication/a [acc:usr-a:pwd-a:Section-A]}
     * With {@code acc:usr-a:pwd-a} the authentication is correct and the
     * request is responded with status 404, because the request does not exit.
     * In the access-log, the user is also logged because authorization was
     * given. 
     * @throws Exception
     */     
    @Test
    public void testAcceptance_16()
            throws Exception {
        
        String request;
        String response;
        String accessLog;    
        
        request = "GET /authentication/a/xxx HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/xxx HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-a", "pwd-a"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 
        
        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("404", request, "usr-a")));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * {@code /authentication/a [acc:usr-a:pwd-a:Section-A]}
     * The authentication is missing and the request is responded with status 401.
     * Even if the file or directory does not exist.
     * @throws Exception
     */  
    @Test
    public void testAcceptance_17()
            throws Exception {
        
        final String request = "GET /authentication/a/xxx HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * VHA defines {@code /authentication/a [acc:usr-a:pwd-a:Section-A]}.
     * VHF extends VHA, overwrites {@code /authentication/a
     * [acc:usr-a:pwd-a:Section-A] [D]}. VHA must refuse the authorization via
     * DIGEST. VHA must refuse the authorization via BASIC.
     * @throws Exception
     */   
    @Test
    public void testAcceptance_18()
            throws Exception {
        
        final String request = "GET /authentication/a/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "Authorization: Basic " + Codec.encodeBase64("usr-a:pwd-a") + "\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * {@code /authentication/a/b/d/e/e/lock [C]}
     * The URI requires an authorization and has been redirected, because a
     * directory with no slash at the end was requested. The requests must be
     * responded with status 401 and 302.
     * @throws Exception
     */       
    @Test
    public void testAcceptance_19()
            throws Exception {
        
        String request;
        String response;
        String accessLog;    
        
        request = "GET /authentication/a/b/d/e/e HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/b/d/e/e HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-e", "pwd-e"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_302));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("302", request, "usr-e")));

        request = "HEAD /authentication/a/b/d/e/e HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
        
        request = "HEAD /authentication/a/b/d/e/e HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-e", "pwd-e"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_302));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("302", request, "usr-e")));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * {@code /authentication/a/b/d/e/e/lock [C]}
     * The URI requires an authorization and the target does not exist. The
     * requests must be responded with status 401 and 404.
     * @throws Exception
     */ 
    @Test
    public void testAcceptance_20()
            throws Exception {
        
        String request;
        String response;
        String accessLog;    
        
        request = "GET /authentication/a/b/d/e/e/nix HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/b/d/e/e/nix HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-e", "pwd-e"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("404", request, "usr-e")));

        request = "HEAD /authentication/a/b/d/e/e/nix HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "HEAD /authentication/a/b/d/e/e/nix HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-e", "pwd-e"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("404", request, "usr-e")));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * {@code /authentication/a/b/d/e/e/lock [C]}
     * The URI requires an authorization and has been forbidden. The requests
     * must be responded with status 401 and 403.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_21()
            throws Exception {
        
        String request;
        String response;
        String accessLog;   
        
        request = "GET /authentication/a/b/d/e/e/lock HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/b/d/e/e/lock HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-e", "pwd-e"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("403", request, "usr-e")));

        request = "HEAD /authentication/a/b/d/e/e/lock HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "HEAD /authentication/a/b/d/e/e/lock HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-e", "pwd-e"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("403", request, "usr-e")));        
    }
    
    private void assertAcceptance_22_1(final String... args)
            throws Exception {

        String uri = null;
        if (args.length > 0)
            uri = args[0];
        String authorisation = null;
        if (args.length > 1)
            authorisation = args[1];
        String login = null;
        if (args.length > 2)
            login = args[2];

        String request = "GET " + uri + " HTTP/1.0\r\n"
                + "Host: vHa\r\n";
        if (login != null)
            request += "Authorization: Basic " + Codec.encodeBase64(login) + "\r\n";
        request += "\r\n";

        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        if (authorisation != null)
            Assert.assertTrue(HttpUtils.getResponseHeaderValue(response, HeaderField.WWW_AUTHENTICATE).startsWith(authorisation + " "));
        else
            Assert.assertFalse(HttpUtils.exitsResponseHeader(response, HeaderField.WWW_AUTHENTICATE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        if (authorisation != null)
            Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
        else
            Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_302));
    }

    private void assertAcceptance_22_2(final String... args)
            throws Exception {
        
        String uri = null;
        if (args.length > 0)
            uri = args[0];
        String method = null;
        if (args.length > 1)
            method = args[1];
        String user = null;
        if (args.length > 2)
            user = args[2];
        String password = null;
        if (args.length > 3)
            password = args[3];

        String request;
        String response;

        request  = null;
        response = null;
        if (method == null
                || method.trim().isEmpty()) {
            request = "GET " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + "\r\n";
            response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        } else if (("Basic").equalsIgnoreCase(method)) {
            request = "GET " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + "Authorization: Basic " + Codec.encodeBase64(user + ":" + password) + "\r\n"
                    + "\r\n";
            response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        } else if (("Digest").equalsIgnoreCase(method)) {
            request = "GET " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + "\r\n";
            response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Digest(user, password));
        } else Assert.fail("Unsupported authentication method: '" + method + "'");

        Assert.assertNotNull(response);
        Assert.assertFalse(response.trim().isEmpty());
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_302));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE));

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        final String accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("302", request, user)));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * The directory structure {@code /o1} uses a mix of different
     * authorizations. The correct use and response is checked.
     * @throws Exception
     */        
    @Test
    public void testAcceptance_22()
            throws Exception {

        this.assertAcceptance_22_1("/o1", null);
        this.assertAcceptance_22_1("/o1/o2", null);
        this.assertAcceptance_22_1("/o1/o2/o3", null);
        this.assertAcceptance_22_1("/o1/o2/o3/d1", "Digest");
        this.assertAcceptance_22_1("/o1/o2/o3/d1/d2", "Digest");
        this.assertAcceptance_22_1("/o1/o2/o3/d1/d2/d3", "Digest");
        this.assertAcceptance_22_1("/o1/o2/o3/d1/d2/d3/n1", null);
        this.assertAcceptance_22_1("/o1/o2/o3/d1/d2/d3/n1/n2", null);
        this.assertAcceptance_22_1("/o1/o2/o3/d1/d2/d3/n1/n2/n3", null);
        this.assertAcceptance_22_1("/o1/o2/o3/d1/d2/d3/n1/n2/n3/b1", "Basic");
        this.assertAcceptance_22_1("/o1/o2/o3/d1/d2/d3/n1/n2/n3/b1/b2", "Basic");
        this.assertAcceptance_22_1("/o1/o2/o3/d1/d2/d3/n1/n2/n3/b1/b2/b3", "Basic");

        this.assertAcceptance_22_2("/o1", null);
        this.assertAcceptance_22_2("/o1/o2", null);
        this.assertAcceptance_22_2("/o1/o2/o3", null);
        this.assertAcceptance_22_2("/o1/o2/o3/d1", "Digest", "usr-d", "pwd-d");
        this.assertAcceptance_22_2("/o1/o2/o3/d1/d2", "Digest", "usr-d", "pwd-d");
        this.assertAcceptance_22_2("/o1/o2/o3/d1/d2/d3", "Digest", "usr-d", "pwd-d");
        this.assertAcceptance_22_2("/o1/o2/o3/d1/d2/d3/n1");
        this.assertAcceptance_22_2("/o1/o2/o3/d1/d2/d3/n1/n2");
        this.assertAcceptance_22_2("/o1/o2/o3/d1/d2/d3/n1/n2/n3");
        this.assertAcceptance_22_2("/o1/o2/o3/d1/d2/d3/n1/n2/n3/b1", "Basic", "usr-b", "pwd-b");
        this.assertAcceptance_22_2("/o1/o2/o3/d1/d2/d3/n1/n2/n3/b1/b2", "Basic", "usr-b", "pwd-b");
        this.assertAcceptance_22_2("/o1/o2/o3/d1/d2/d3/n1/n2/n3/b1/b2/b3", "Basic", "usr-b", "pwd-b");
    }

    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * {@code /authentication/a [acc:usr-a:pwd-a:Section-A]}
     * The authentication is incorrect and the request is responded with status 401.
     * @throws Exception
     */       
    @Test
    public void testAcceptance_23()
            throws Exception {
        
        String request;
        String response;
        String accessLog;   
        
        request = "GET /authentication/a/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 
        
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-a", "pwd-b"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * {@code /authentication/a [acc: group:C ] [realm:Section-A] [d]}
     * The authentication is incorrect and the request is responded with status 401.
     * @throws Exception
     */       
    @Test
    public void testAcceptance_24()
            throws Exception {
        
        String request;
        String response;
        String accessLog;  
        
        request = "GET /authentication/a/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/a/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-b", "pwd-a"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * {@code /authentication/dvc [Acc:group:c] [realm:sb] [C] [D]}
     * The URI requires an authorization and has been forbidden. The requests
     * must be responded with status 401 and 403.
     * @throws Exception
     */       
    @Test
    public void testAcceptance_25()
            throws Exception {
        
        String request;
        String response;
        String accessLog;  
        
        request = "GET /authentication/dvc HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/dvc HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-a", "pwd-a"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 
        
        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("403", request, "usr-a")));  
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * {@code /authentication/dvr > http://www.heise.de [Acc:group:c] [realm:sb] [R] [D]}
     * The URI requires an authorization and has been redirected. The requests
     * must be responded with status 401 and 302.
     * @throws Exception
     */        
    @Test
    public void testAcceptance_26()
            throws Exception {
        
        String request;
        String response;
        String accessLog;   
        
        request = "GET /authentication/dvr HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 
        
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/dvr HTTP/1.0\r\n"
               + "Host: vHf\r\n"
               + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-a", "pwd-a"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_302));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("302", request, "usr-a")));  
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * {@code /authentication/dvv > ./stage/documents_vh_A/test_a [Acc:group:c] [realm:sb] [D]}
     * The URI requires an authorization and referenced an existing virtual-path.
     * The requests must be responded with status 401 and 200.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_27()
            throws Exception {
        
        String request;
        String response;
        String accessLog;   
        
        request = "GET /authentication/dvv/ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/dvv/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-a", "pwd-a"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, "usr-a")));

        request = "GET /authentication/dvv HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-ax", "pwd-a"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/dvv/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-ax", "pwd-a"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 
       
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * {@code /authentication/dvm > module.WorkerModule_A [v:xx=123] [m] [Acc:group:c] [realm:sb] [D]}
     * The URI requires an authorization and reference a module. The requests 
     * must be responded with status 401 and 001.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_28()
            throws Exception {
        
        String request;
        String response;
        String accessLog;   
        
        request = "GET /authentication/dvm/ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));

        request = "GET /authentication/dvm/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-a", "pwd-a"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS("001")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE)); 

        Thread.sleep(AbstractStageRequestTest.SLEEP);
        accessLog = AbstractStage.getAccessStreamCapture().tail();
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("1", request, "usr-a")));

        request = "GET /authentication/dvm/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest("usr-a", "pwd-ax"));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST)); 

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
    }
    
    private void assertAcceptance_29(final String uri, final String user, final String password, final boolean authorisation)
            throws Exception {
        
        final String request = "GET " + uri + " HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request, new Authentication.Digest(user, password));

         if (authorisation)
             Assert.assertTrue(HttpUtils.exitsResponseHeader(response, HeaderField.WWW_AUTHENTICATE));
         else
             Assert.assertFalse(HttpUtils.exitsResponseHeader(response, HeaderField.WWW_AUTHENTICATE));

         Thread.sleep(AbstractStageRequestTest.SLEEP);
         final String accessLog = AbstractStage.getAccessStreamCapture().tail();
         if (authorisation)
             Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
         else
             Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_404));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * The directory structure {@code /authentication} uses a mix of different
     * authorizations with different options and options. The correct use and
     * response is checked.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_29()
            throws Exception {
        
        this.assertAcceptance_29("/authentication/dg0", "usr-a", "pwd-a", false);
        this.assertAcceptance_29("/authentication/dg0", "usr-b", "pwd-b", false);
        this.assertAcceptance_29("/authentication/dg0", "usr-e", "pwd-e", false);
        this.assertAcceptance_29("/authentication/dg0", "usr-d", "pwd-d", false);
        this.assertAcceptance_29("/authentication/dg0", "usr-x", "pwd-x", false);

        this.assertAcceptance_29("/authentication/dg1", "usr-a", "pwd-a", false);
        this.assertAcceptance_29("/authentication/dg1", "usr-b", "pwd-b", true);
        this.assertAcceptance_29("/authentication/dg1", "usr-e", "pwd-e", false);
        this.assertAcceptance_29("/authentication/dg1", "usr-d", "pwd-d", false);
        this.assertAcceptance_29("/authentication/dg1", "usr-x", "pwd-x", true);

        this.assertAcceptance_29("/authentication/dg2", "usr-a", "pwd-a", false);
        this.assertAcceptance_29("/authentication/dg2", "usr-b", "pwd-b", true);
        this.assertAcceptance_29("/authentication/dg2", "usr-e", "pwd-e", true);
        this.assertAcceptance_29("/authentication/dg2", "usr-d", "pwd-d", false);
        this.assertAcceptance_29("/authentication/dg2", "usr-x", "pwd-x", true);

        this.assertAcceptance_29("/authentication/dg3", "usr-a", "pwd-a", false);
        this.assertAcceptance_29("/authentication/dg3", "usr-b", "pwd-b", false);
        this.assertAcceptance_29("/authentication/dg3", "usr-e", "pwd-e", false);
        this.assertAcceptance_29("/authentication/dg3", "usr-d", "pwd-d", false);
        this.assertAcceptance_29("/authentication/dg3", "usr-x", "pwd-x", false);

        this.assertAcceptance_29("/authentication/dg4", "usr-a", "pwd-a", false);
        this.assertAcceptance_29("/authentication/dg4", "usr-b", "pwd-b", false);
        this.assertAcceptance_29("/authentication/dg4", "usr-e", "pwd-e", false);
        this.assertAcceptance_29("/authentication/dg4", "usr-d", "pwd-d", false);
        this.assertAcceptance_29("/authentication/dg4", "usr-x", "pwd-x", false);

        this.assertAcceptance_29("/authentication/dg5", "usr-a", "pwd-a", true);
        this.assertAcceptance_29("/authentication/dg5", "usr-b", "pwd-b", true);
        this.assertAcceptance_29("/authentication/dg5", "usr-e", "pwd-e", false);
        this.assertAcceptance_29("/authentication/dg5", "usr-d", "pwd-d", false);
        this.assertAcceptance_29("/authentication/dg5", "usr-x", "pwd-x", true);

        this.assertAcceptance_29("/authentication/dg6", "usr-a", "pwd-a", false);
        this.assertAcceptance_29("/authentication/dg6", "usr-b", "pwd-b", false);
        this.assertAcceptance_29("/authentication/dg6", "usr-e", "pwd-e", false);
        this.assertAcceptance_29("/authentication/dg6", "usr-d", "pwd-d", false);
        this.assertAcceptance_29("/authentication/dg6", "usr-x", "pwd-x", false);

        this.assertAcceptance_29("/authentication/dg7", "usr-a", "pwd-a", false);
        this.assertAcceptance_29("/authentication/dg7", "usr-b", "pwd-b", false);
        this.assertAcceptance_29("/authentication/dg7", "usr-e", "pwd-e", false);
        this.assertAcceptance_29("/authentication/dg7", "usr-d", "pwd-d", false);
        this.assertAcceptance_29("/authentication/dg7", "usr-x", "pwd-x", false);

        this.assertAcceptance_29("/authentication/dg8", "usr-a", "pwd-a", false);
        this.assertAcceptance_29("/authentication/dg8", "usr-b", "pwd-b", false);
        this.assertAcceptance_29("/authentication/dg8", "usr-e", "pwd-e", false);
        this.assertAcceptance_29("/authentication/dg8", "usr-d", "pwd-d", false);
        this.assertAcceptance_29("/authentication/dg8", "usr-x", "pwd-x", false);

        this.assertAcceptance_29("/authentication/dg9", "usr-a", "pwd-a", false);
        this.assertAcceptance_29("/authentication/dg9", "usr-b", "pwd-b", false);
        this.assertAcceptance_29("/authentication/dg9", "usr-e", "pwd-e", false);
        this.assertAcceptance_29("/authentication/dg9", "usr-d", "pwd-d", false);
        this.assertAcceptance_29("/authentication/dg9", "usr-x", "pwd-x", false);
    }
    
    private void assertAcceptance_30(final String uri, final String method)
            throws Exception {
        
        final String request = "GET " + uri + " HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE(method)));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
    }
    
    /** 
     * Test case for acceptance.
     * The change of authentication method must be working. In the following,
     * the correct function is checked. The request must be responded with 401
     * and the correct {@code WWW-Authenticate}.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_30()
            throws Exception {
        
        this.assertAcceptance_30("/authentication/dbd/a", "Digest");
        this.assertAcceptance_30("/authentication/dbd/a/b", "Basic");
        this.assertAcceptance_30("/authentication/dbd/a/b/c", "Digest"); 
    }     
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * {@code /authentication/a/b/e [acC:group:BE[realm:Section-BE [D]}
     * With a corrupt acc rule the authentication is ignored and the request is
     * responded with status 200.
     * @throws Exception
     */
    @Test
    public void testAcceptance_98()
            throws Exception {
        
        final String request = "GET /authentication/a/b/e/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIFFUSE));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));
    }
    
    /** 
     * Test case for acceptance.
     * Test for Digest Authentication:
     * {@code /authentication/a/b/e/c [acC:group:BEC][realm:Section-BEC}
     * With a correct acc rule after a corrupt acc rule, the authentication is
     * required and the request is responded with status 401.
     * @throws Exception
     */
    @Test
    public void testAcceptance_99()
            throws Exception {
        
        final String request = "GET /authentication/a/b/e/c/ HTTP/1.0\r\n"
                + "Host: vHf\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_WWW_AUTHENTICATE_DIGEST("Section-BEC")));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
    } 
}