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

import com.seanox.test.HttpUtils;
import com.seanox.test.HttpUtils.HeaderField;
import com.seanox.test.Pattern;

/** Test cases for {@link com.seanox.devwex.Worker}. */
public class WorkerOptionsTest extends AbstractStageRequestTest {
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, The target is not checked. Whether exits or
     * not, the request is responded with status 200, {@code Allow} and without
     * content details. {@code OPTIONS} is only a request about the supported
     * HTTP methods.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_01()
            throws Exception {

        final String request = "OPTIONS / HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));      
    } 
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, The target is not checked. Whether exits or
     * not, the request is responded with status 200, {@code Allow} and without
     * content details. {@code OPTIONS} is only a request about the supported
     * HTTP methods.
     * @throws Exception
     */ 
    @Test
    public void testAcceptance_02()
            throws Exception {

        final String request = "OPTIONS /test_a HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));      
    } 
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, The target is not checked. Whether exits or
     * not, the request is responded with status 200, {@code Allow} and without
     * content details. {@code OPTIONS} is only a request about the supported
     * HTTP methods.
     * @throws Exception
     */ 
    @Test
    public void testAcceptance_03()
            throws Exception {

        final String request = "OPTIONS /test_ax HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));      
    } 

    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, The target is not checked. Whether exits or
     * not, the request is responded with status 200, {@code Allow} and without
     * content details. {@code OPTIONS} is only a request about the supported
     * HTTP methods.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_04()
            throws Exception {

        final String request = "OPTIONS /method_file.txt HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));      
    }
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, The target is not checked. Whether exits or
     * not, the request is responded with status 200, {@code Allow} and without
     * content details. {@code OPTIONS} is only a request about the supported
     * HTTP methods.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_05()
            throws Exception {

        final String request = "OPTIONS /method_file.txt/ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));      
    } 
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, the request header {@code If-Modified-Since}
     * is ignored. Whether correct or invalid, the request is responded with
     * status 200, {@code Allow} and without content details.
     * @throws Exception
     */
    @Test
    public void testAcceptance_06()
            throws Exception {

        String request;
        String response;
        
        request = "HEAD /method_file.txt HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);       
        String lastModified = HttpUtils.getResponseHeaderValue(response, HeaderField.LAST_MODIFIED);
        
        request = "OPTIONS /method_file.txt HTTP/1.0\r\n"
                + "If-Modified-Since: " + lastModified + "\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);   
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));    
    } 
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, the request header {@code If-Modified-Since}
     * is ignored. Whether correct or invalid, the request is responded with
     * status 200, {@code Allow} and without content details.
     * @throws Exception
     */
    @Test
    public void testAcceptance_07()
            throws Exception {
        
        final String request = "OPTIONS /method_file.txt HTTP/1.0\r\n"
                + "If-Modified-Since: Sat, 01 Jan 2000 00:00:00 GMT\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);   
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));     
    } 
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, the request header {@code If-Modified-Since}
     * is ignored. Whether correct or invalid, the request is responded with
     * status 200, {@code Allow} and without content details.
     * @throws Exception
     */
    @Test
    public void testAcceptance_08()
            throws Exception {

        String request;
        String response;
        
        request = "HEAD /method_file.txt HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);       
        final String lastModified = HttpUtils.getResponseHeaderValue(response, HeaderField.LAST_MODIFIED);
        final String contentLength = HttpUtils.getResponseHeaderValue(response, HeaderField.CONTENT_LENGTH);
        
        request = "OPTIONS /method_file.txt HTTP/1.0\r\n"
                + "If-Modified-Since: " + lastModified + "; xxx; length=" + contentLength + "\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);   
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));    
    } 
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, the request header {@code If-Modified-Since}
     * is ignored. Whether correct or invalid, the request is responded with
     * status 200, {@code Allow} and without content details.
     * @throws Exception
     */
    @Test
    public void testAcceptance_09()
            throws Exception {
        
        String request;
        String response;
        
        request = "HEAD /method_file.txt HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);       
        final String lastModified = HttpUtils.getResponseHeaderValue(response, HeaderField.LAST_MODIFIED);
        final String contentLength = HttpUtils.getResponseHeaderValue(response, HeaderField.CONTENT_LENGTH);
        
        request = "OPTIONS /method_file.txt HTTP/1.0\r\n"
                + "If-Modified-Since: " + lastModified + "; xxx; length=1" + contentLength + "\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);    
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));        
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));     
    }
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, the target (also directories and independent
     * of default) is not checked. Whether exits or not, the request is
     * responded with status 200, {@code Allow} and without content details.
     * {@code OPTIONS} is only a request about the supported HTTP methods.
     * Header fields: {@code Range}, {@code If-Modified-Since} and
     * {@code If-UnModified-Since} are ignored. 
     * @throws Exception
     */     
    @Test
    public void testAcceptance_11()
            throws Exception {
        
        final String request = "OPTIONS /test_a/ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);   
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));     
    } 
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, the target (also directories) is not checked.
     * Whether exits or not, the request is responded with status 200,
     * {@code Allow} and without content details. {@code OPTIONS} is only a
     * request about the supported HTTP methods. Header fields: {@code Range},
     * {@code If-Modified-Since} and {@code If-UnModified-Since} are ignored. 
     * @throws Exception
     */    
    @Test
    public void testAcceptance_12()
            throws Exception {
        
        final String request = "OPTIONS /test_d/ HTTP/1.0\r\n"
                + "If-Modified-Since: Mon, 11 Jan 2004 19:11:58 GMT\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);   
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));     
    } 
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, the request for a forbidden target is
     * responded with status 403. 
     * @throws Exception
     */     
    @Test
    public void testAcceptance_13()
            throws Exception {
        
        final String request = "OPTIONS /forbidden HTTP/1.0\r\n"
                + "If-Modified-Since: Mon, 11 Jan 2004 19:11:58 GMT\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);   
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_403));     
    } 
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, the target (also with an absolute paths) is
     * not checked. Whether exits or not, the request is responded with status 200,
     * {@code Allow} and without content details. {@code OPTIONS} is only a
     * request about the supported HTTP methods. 
     * @throws Exception
     */ 
    @Test
    public void testAcceptance_14()
            throws Exception {
        
        final String request = "OPTIONS /absolute HTTP/1.0\r\n"
                + "If-Modified-Since: Mon, 11 Jan 2004 19:11:58 GMT\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);   
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));      
    } 
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, the target (also with an absolute paths) is
     * not checked. Whether exits or not, the request is responded with status 200,
     * {@code Allow} and without content details. {@code OPTIONS} is only a
     * request about the supported HTTP methods. 
     * @throws Exception
     */    
    @Test
    public void testAcceptance_15()
            throws Exception {
        
        final String request = "OPTIONS /absolutexxx HTTP/1.0\r\n"
                + "If-Modified-Since: Mon, 11 Jan 2004 19:11:58 GMT\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);   
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));      
    } 
    
    /**
     * Test case for acceptance.
     * For the CGI, the method {@code OPTIONS} are responded by the CGI.
     * In the test case, {@code OPTIONS} is for the CGI not allowed and the
     * request is responded with status 405, also if the CGI not exists.
     * @throws Exception
     */
    @Test
    public void testAcceptance_16()
            throws Exception {
        
        final String request = "OPTIONS /method.php HTTP/1.0\r\n"
                + "If-Modified-Since: Mon, 11 Jan 2004 19:11:58 GMT\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);   
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_405));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_405));     
    } 
    
    /**
     * Test case for acceptance.
     * For the CGI, the method {@code OPTIONS} are responded by the CGI.
     * In the test case, {@code OPTIONS} is for the CGI allowed and the request
     * is responded with status 200.
     * @throws Exception
     */
    @Test
    public void testAcceptance_17()
            throws Exception {
        
        final String request = "oPTIONS /method.jsx HTTP/1.0\r\n"
                + "Host: vHb\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);   
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_ALLOW_DIFFUSE));
        
        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        final String body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertTrue(body.matches("(?si)^.*hallo.*$"));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));     
    }
    
    /**
     * Test case for acceptance.
     * For the CGI, the method {@code OPTIONS} are responded by the CGI.
     * In the test case, {@code OPTIONS} is for the CGI not allowed and the
     * request is responded with status 405.
     * @throws Exception
     */
    @Test
    public void testAcceptance_18()
            throws Exception {
        
        final String request = "oPTIONS /method.jsx HTTP/1.0\r\n"
                + "Host: vHe\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);   
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_405));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_405));    
    }
    
    private void assertAcceptance_19(int count, String path, String start, String end)
            throws Exception {
        
        if (start != null
                && start.contains("-")
                && end == null)
            end = "";
        if (end != null
                && end.contains("-")
                && start == null)
            start = "";

        String request = "Options " + path + " HTTP/1.0\r\n"
                + "Host: vHa\r\n";
        if (start != null
                || end != null) {
            request += "Range: bYteS = " + (start != null ? start : "");
            if (start != null
                    && end != null)
                request += count % 2 == 0 ? "-" : " - ";
            request += end != null ? end : "";
            request += "\r\n";
        }

        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request + "\r\n");

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_RANGE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));
    }
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, the target is not checked. Whether exits or
     * not, the request is responded with status 200, {@code Allow} and without
     * content details. {@code OPTIONS} is only a request about the supported
     * HTTP methods. Header fields: {@code Range}, {@code If-Modified-Since} and
     * {@code If-UnModified-Since} are ignored. 
     * @throws Exception
     */
    @Test
    public void testAcceptance_19()
            throws Exception {

        for (final String path : new String[] {"/partial_content.txt", "/partial_content_empty.txt",
                "/partial_content-nix.txt", "/"}) {

            int count = 0;
        
            this.assertAcceptance_19(++count, path, "0",      "0");
            this.assertAcceptance_19(++count, path, "0",      "1");    
            this.assertAcceptance_19(++count, path, "0",      "127");
            this.assertAcceptance_19(++count, path, "0",      "65535");
            this.assertAcceptance_19(++count, path, "1",      "0");
            this.assertAcceptance_19(++count, path, "1",      "1");
            this.assertAcceptance_19(++count, path, "1",      "127");    
            this.assertAcceptance_19(++count, path, "1",      "65535");
            this.assertAcceptance_19(++count, path, "127",    "256");
            this.assertAcceptance_19(++count, path, "256",    "127");
    
            this.assertAcceptance_19(++count, path, "127",    "0");
            this.assertAcceptance_19(++count, path, "127",    "1");
            this.assertAcceptance_19(++count, path, "65535",  "0");
            this.assertAcceptance_19(++count, path, "65535",  "1");
            this.assertAcceptance_19(++count, path, "256",    "65535");
            this.assertAcceptance_19(++count, path, "65535",  "256");
            this.assertAcceptance_19(++count, path, "-256",   "127");
            this.assertAcceptance_19(++count, path, "-127",   "256");
            this.assertAcceptance_19(++count, path, "256",    "-127");
            this.assertAcceptance_19(++count, path, "127",    "-256");
    
            this.assertAcceptance_19(++count, path, "0",      "A");
            this.assertAcceptance_19(++count, path, "1",      "A");
            this.assertAcceptance_19(++count, path, "256",    "B");
            this.assertAcceptance_19(++count, path, "65535",  "C");
            this.assertAcceptance_19(++count, path, "-0",     "A");
            this.assertAcceptance_19(++count, path, "-1",     "A");
            this.assertAcceptance_19(++count, path, "-256",   "B");
            this.assertAcceptance_19(++count, path, "-65535", "C");
            this.assertAcceptance_19(++count, path, "A",      "0");
            this.assertAcceptance_19(++count, path, "A",      "1");
    
            this.assertAcceptance_19(++count, path, "B",      "256");
            this.assertAcceptance_19(++count, path, "C",      "65535");
            this.assertAcceptance_19(++count, path, "A",      "-0");
            this.assertAcceptance_19(++count, path, "A",      "-1");
            this.assertAcceptance_19(++count, path, "B",      "-256");
            this.assertAcceptance_19(++count, path, "C",      "-65535");
            this.assertAcceptance_19(++count, path, "0",      "");
            this.assertAcceptance_19(++count, path, "256",    "");
            this.assertAcceptance_19(++count, path, "65535",  "");
            this.assertAcceptance_19(++count, path, "-0",     "");
    
            this.assertAcceptance_19(++count, path, "-1",     "");
            this.assertAcceptance_19(++count, path, "-256",   "");
            this.assertAcceptance_19(++count, path, "-65535", "");
            this.assertAcceptance_19(++count, path, null,     "0");
            this.assertAcceptance_19(++count, path, null,     "256");
            this.assertAcceptance_19(++count, path, null,     "65535");
            this.assertAcceptance_19(++count, path, null,     "A");
            this.assertAcceptance_19(++count, path, null,     null);
            this.assertAcceptance_19(++count, path, "",       "0");
            this.assertAcceptance_19(++count, path, "",       "256");
           
            this.assertAcceptance_19(++count, path, "",       "65535");
            this.assertAcceptance_19(++count, path, "",       "-0");
            this.assertAcceptance_19(++count, path, "",       "-1");
            this.assertAcceptance_19(++count, path, "",       "-256");
            this.assertAcceptance_19(++count, path, "",       "-65535");
            this.assertAcceptance_19(++count, path, "0",      " ");
            this.assertAcceptance_19(++count, path, "1",      " ");
            this.assertAcceptance_19(++count, path, "256",    " ");
            this.assertAcceptance_19(++count, path, "65535",  " ");
            this.assertAcceptance_19(++count, path, "-0",     " ");
            
            this.assertAcceptance_19(++count, path, null,     "-0");
            this.assertAcceptance_19(++count, path, null,     "-1");
            this.assertAcceptance_19(++count, path, null,     "-256");
            this.assertAcceptance_19(++count, path, null,     "-65535");
            this.assertAcceptance_19(++count, path, "0",      null);
            this.assertAcceptance_19(++count, path, "1",      null);
            this.assertAcceptance_19(++count, path, "256",    null);
            this.assertAcceptance_19(++count, path, "65535",  null);
            this.assertAcceptance_19(++count, path, "-0",     null);  
            this.assertAcceptance_19(++count, path, null,     "65535");
            this.assertAcceptance_19(++count, path, null,     "256");
            this.assertAcceptance_19(++count, path, null,     "127");            
            
            this.assertAcceptance_19(++count, path, "-1",     " ");
            this.assertAcceptance_19(++count, path, "-256",   " ");
            this.assertAcceptance_19(++count, path, "-65535", " ");
            this.assertAcceptance_19(++count, path, " ",      "0");
            this.assertAcceptance_19(++count, path, " ",      "1");
            this.assertAcceptance_19(++count, path, " ",      "256");
            this.assertAcceptance_19(++count, path, " ",      "65535");
            this.assertAcceptance_19(++count, path, " ",      "-0");
            this.assertAcceptance_19(++count, path, " ",      "-1");
            this.assertAcceptance_19(++count, path, " ",      "-256");
            
            this.assertAcceptance_19(++count, path, " ",      "-65535");
            this.assertAcceptance_19(++count, path, "0",      "-");
            this.assertAcceptance_19(++count, path, "1",      "-");
            this.assertAcceptance_19(++count, path, "256",    "-");
            this.assertAcceptance_19(++count, path, "65535",  "-");
            this.assertAcceptance_19(++count, path, "-0",     "-");    
            this.assertAcceptance_19(++count, path, "-1",     "-");
            this.assertAcceptance_19(++count, path, "-256",   "-");
            this.assertAcceptance_19(++count, path, "-65535", "-");
            this.assertAcceptance_19(++count, path, "-",      "0");
    
            this.assertAcceptance_19(++count, path, "-",      "256");    
            this.assertAcceptance_19(++count, path, "-",      "65535");
            this.assertAcceptance_19(++count, path, "-",      "-0");
            this.assertAcceptance_19(++count, path, "-",      "-1");
            this.assertAcceptance_19(++count, path, "-",      "-256");
            this.assertAcceptance_19(++count, path, "-",      "-65535");
            this.assertAcceptance_19(++count, path, "0",      ";");
            this.assertAcceptance_19(++count, path, "1",      ";");    
            this.assertAcceptance_19(++count, path, "256",    ";");
            this.assertAcceptance_19(++count, path, "65535",  ";");
            this.assertAcceptance_19(++count, path, "0;",     null);
            this.assertAcceptance_19(++count, path, "1;",     null);    
            this.assertAcceptance_19(++count, path, "256;",   null);
            this.assertAcceptance_19(++count, path, "65535;", null);            
    
            this.assertAcceptance_19(++count, path, "-0",     ";");
            this.assertAcceptance_19(++count, path, "-1",     ";");
            this.assertAcceptance_19(++count, path, "-256",   ";");    
            this.assertAcceptance_19(++count, path, "-65535", ";");
            this.assertAcceptance_19(++count, path, ";",      "0");
            this.assertAcceptance_19(++count, path, ";",      "1");
            this.assertAcceptance_19(++count, path, ";",      "256");
            this.assertAcceptance_19(++count, path, ";",      "65535");    
            this.assertAcceptance_19(++count, path, ";",      "-0");
            this.assertAcceptance_19(++count, path, ";",      "-1");
            
            this.assertAcceptance_19(++count, path, ";",      "-256");
            this.assertAcceptance_19(++count, path, ";",      "-65535");
            this.assertAcceptance_19(++count, path, "1",      "");
            this.assertAcceptance_19(++count, path, "",       "1");
        }
    }
    
    private void assertAcceptance_22(final String request)
            throws Exception {

        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_RANGE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_ALLOW(("AAA, BBB, XXX, GET, POST, XPOST, CCCC, HEAD, DELETE, PUT, OPTIONS").split(",\\s+"))));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 0)));
    }
    
    /** 
     * Test case for acceptance.
     * For method {@code OPTIONS}, the target is not checked. Whether exits or
     * not, the request is responded with status 200, {@code Allow} and without
     * content details. {@code OPTIONS} is only a request about the supported
     * HTTP methods. Header fields: {@code Range}, {@code If-Modified-Since} and
     * {@code If-UnModified-Since} are ignored. 
     * @throws Exception
     */     
    @Test
    public void testAcceptance_22()
            throws Exception {
        
        String request;
        String response;
        
        final String[] uris = new String[] {"/partial_content.txt", "/partial_content_empty.txt", "/"};
        for (final String uri : uris) {

            String range = "";
            if (uri.equals(uris[0]))
                range = "Range: bytes=2-10\r\n";
            if (uri.equals(uris[1]))
                range = "Range: bytes=0-0\r\n";    
        
            request = "HEAD " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + "\r\n";
            response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);       
            String lastModified = HttpUtils.getResponseHeaderValue(response, HeaderField.LAST_MODIFIED);
            
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + range
                    + "If-Modified-Since: " + lastModified + "\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + range
                    + "If-Modified-Since: " + lastModified + "\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + range
                    + "If-Modified-Since: Thu, 07 Oct 1980 10:20:30 GMT\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + range
                    + "If-UnModified-Since: Thu, 07 Oct 1980 10:20:30 GMT\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + range
                    + "If-UnModified-Since: " + lastModified + "\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + range
                    + "If-UnModified-Since: Thu, 07 Oct 1980 10:20:30 GMT\r\n"
                    + "If-Modified-Since: " + lastModified + "\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + range
                    + "If-UnModified-Since: " + lastModified + "\r\n"
                    + "If-Modified-Since: Thu, 07 Oct 1980 10:20:30 GMT\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + range
                    + "If-UnModified-Since: " + lastModified + "\r\n"
                    + "If-Modified-Since: " + lastModified + "\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + range
                    + "If-UnModified-Since: Thu, 07 Oct 1980 10:20:30 GMT\r\n"
                    + "If-Modified-Since: Thu, 07 Oct 1980 10:20:30 GMT\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + "If-Modified-Since: Thu, 07 Oct 1980 10:20:30 GMT\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + "If-Modified-Since: " + lastModified + "\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + "If-UnModified-Since: Thu, 07 Oct 1980 10:20:30 GMT\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + "If-UnModified-Since: " + lastModified + "\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + "If-UnModified-Since: Thu, 07 Oct 1980 10:20:30 GMT\r\n"
                    + "If-Modified-Since: " + lastModified + "\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + "If-UnModified-Since: " + lastModified + "\r\n"
                    + "If-Modified-Since: Thu, 07 Oct 1980 10:20:30 GMT\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + "If-UnModified-Since: " + lastModified + "\r\n"
                    + "If-Modified-Since: " + lastModified + "\r\n\r\n";
            this.assertAcceptance_22(request);
    
            request = "OPTIONS " + uri + " HTTP/1.0\r\n"
                    + "Host: vHa\r\n"
                    + "If-UnModified-Since: Thu, 07 Oct 1980 10:20:30 GMT\r\n"
                    + "If-Modified-Since: Thu, 07 Oct 1980 10:20:30 GMT\r\n\r\n";
            this.assertAcceptance_22(request);
        }
    }
}