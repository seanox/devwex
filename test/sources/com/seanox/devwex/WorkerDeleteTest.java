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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.seanox.test.Codec;
import com.seanox.test.Pattern;

/** Test cases for {@link com.seanox.devwex.Worker}. */
public class WorkerDeleteTest extends AbstractStageRequestTest {
    
    /** 
     * Test case for acceptance.
     * The Deleting of files whose path is a directory is responded with the
     * location and status 302.
     * @throws Exception
     */
    @Test
    public void testAcceptance_01()
            throws Exception {
        
        String request;
        String response;
        File   target;
        
        request = "Put /delete_test_1\\ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        target = new File(AbstractStage.getRootStage(), "documents_vh_A/delete_test_1");
        Assert.assertTrue(target.exists());        
        
        request = "Put /delete_test_2/x1/x2/x3 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        target = new File(AbstractStage.getRootStage(), "documents_vh_A/delete_test_2/x1/x2/x3");
        Assert.assertTrue(target.exists());        
        
        request = "Put /delete_test_2/x1/file_test.2 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 10\r\n"
                + "\r\n"
                + "1234567890";
        AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        target = new File(AbstractStage.getRootStage(), "documents_vh_A/delete_test_2/x1/file_test.2");
        Assert.assertTrue(target.exists());        
        
        request = "Put /delete_test_2/x1/file_test.1 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 10\r\n"
                + "\r\n"
                + "1234567890";
        AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        target = new File(AbstractStage.getRootStage(), "documents_vh_A/delete_test_2/x1/file_test.1");
        Assert.assertTrue(target.exists());        
        
        request = "Delete /delete_test_2/x1/file_test.2/ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_302));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/delete_test_2/x1/file_test.2")));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("302", request)));
    }
    
    /** 
     * Test case for acceptance.
     * The Deleting of files whose path is a directory is responded with the
     * location and status 302.
     * @throws Exception
     */
    @Test
    public void testAcceptance_02()
            throws Exception {
        
        final String request = "Delete /delete_test_2/x1 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_302));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/delete_test_2/x1/")));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("302", request)));
    }
    
    /** 
     * Test case for acceptance.
     * The deletion of files is responded with status 200 and without content.
     * @throws Exception
     */
    @Test
    public void testAcceptance_03()
            throws Exception {
        
        final String request = "Delete /delete_test_2/x1/file_test.2 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LOCATION_DIFFUSE));        
        
        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        final String body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertTrue(body.length() <= 0);
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request)));        
    }
    
    /** 
     * Test case for acceptance.
     * The Deleting of files that do not exist is responded with status 404.
     * @throws Exception
     */
    @Test
    public void testAcceptance_04()
            throws Exception {
        
        final String request = "Delete /delete_test_2/x1/file_test.2 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_404));
    }
    
    /** 
     * Test case for acceptance.
     * The deletion of directories is responded with status 200 and without
     * content.
     * @throws Exception
     */
    @Test
    public void testAcceptance_05()
            throws Exception {
        
        final String request = "Delete /delete_test_2/x1/x2/x3/ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LOCATION_DIFFUSE));
        
        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        final String body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertTrue(body.length() <= 0);
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request)));
    }
    
    /** 
     * Test case for acceptance.
     * The Deleting of directories that do not exist is responded with status 404.
     * @throws Exception
     */
    @Test
    public void testAcceptance_06()
            throws Exception {
        
        final String request = "Delete /delete_test_2/x1/x2/x3/ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_404));
    }
    
    /** 
     * Test case for acceptance.
     * The deletion of directories with subdirectories is responded with status 200.
     * All subdirectories and files will be deleted.
     * @throws Exception
     */
    @Test
    public void testAcceptance_07()
            throws Exception {
        
        final File target = new File(AbstractStage.getRootStage(), "documents_vh_A/delete_test_1");
        
        Assert.assertTrue(target.exists());
        
        final String request = "Delete /delete_test_1/ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LOCATION_DIFFUSE));
        
        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        final String body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertTrue(body.length() <= 0);        
        
        Assert.assertFalse(target.exists());

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));
    }
    
    /** 
     * Test case for acceptance.
     * The deletion of directories with subdirectories is responded with status 200.
     * All subdirectories and files will be deleted.
     * @throws Exception
     */
    @Test
    public void testAcceptance_08()
            throws Exception {
        
        final File target = new File(AbstractStage.getRootStage(), "documents_vh_A/delete_test_2");
        
        Assert.assertTrue(target.exists());
        
        final String request = "Delete /delete_test_2/ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LOCATION_DIFFUSE));
        
        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        final String body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertTrue(body.length() <= 0);        
        
        Assert.assertFalse(target.exists());

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));
    }    
    
    /** 
     * Test case for acceptance.
     * DELETE is executed by a CGI.
     * The request is responded with Status 200.
     * @throws Exception
     */
    @Test
    public void testAcceptance_09()
            throws Exception {
        
        final String request = "Delete /method.jsx HTTP/1.0\r\n"
                + "Host: vHe\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        final String body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertTrue(body.matches("(?si)^\\s*hallo\\s*$"));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));
    }

    /** 
     * Test case for acceptance.
     * DELETE is executed by a CGI, but the CGI does not exist.
     * The request is responded with Status 405.
     * @throws Exception
     */
    @Test
    public void testAcceptance_10()
            throws Exception {
        
        final String request = "Delete /method.php HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_405));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_405));
    }
    
    /** 
     * Test case for acceptance.
     * DELETE is executed by a module.
     * The path of the uri is for a module is absolute and so the module with
     * the path {@code /test.module} will also responses an uri with the path
     * {@code /test.module123}. 
     * @throws Exception
     */
    @Test
    public void testAcceptance_11()
            throws Exception {
        
        final String request = "Delete /test.module123 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS("001 Test ok")));
        Assert.assertTrue(response.matches("(?s)^.*\r\nModule: module.WorkerModule_A::Service\r\n.*$"));
        Assert.assertTrue(response.matches("(?s)^.*\r\nOpts: module.WorkerModule_A \\[v:xx=123\\] \\[m\\]\r\n.*$"));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("1")));
    }
    
    /** 
     * Test case for acceptance.
     * Delete is executed for an absolute reference (URL). Thus, the request is
     * responded with Status 200. The path is absolute, so {@code /test.xxx123}
     * is also covered by {@code /test.xxx}.
     * @throws Exception
     */
    @Test
    public void testAcceptance_12()
            throws Exception {

        String request;
        String response;
        String accessLog;
        String header;
        String body;
        
        final File target = new File(AbstractStage.getRootStage(), "documents_vh_A/file.xxx");
        
        Assert.assertFalse(target.exists());        
        
        request = "Put /file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/file.xxx")));        
        Assert.assertTrue(target.exists());  
        
        request = "Delete /test.xxx123 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LOCATION_DIFFUSE));
        
        header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertTrue(body.length() <= 0);        
        Assert.assertFalse(target.exists());

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));
        
        request = "Delete /test.xxx123 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));      
        
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_404));
        
        request = "Delete /test.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));      
        
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_404));
    }
    
    /** 
     * Test case for acceptance.
     * Delete is executed for an absolute reference (URL).
     * Thus, the request is responded with Status 200.
     * @throws Exception
     */
    @Test
    public void testAcceptance_13()
            throws Exception {

        String request;
        String response;
        String accessLog;
        String header;
        String body;
        
        final File target = new File(AbstractStage.getRootStage(), "documents_vh_A/file.xxx");
        Assert.assertFalse(target.exists());        
        
        request = "Put /file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/file.xxx")));        
        Assert.assertTrue(target.exists());  
        
        request = "Delete /test.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LOCATION_DIFFUSE));
        
        header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertTrue(body.length() <= 0);        
        Assert.assertFalse(target.exists());

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));
        
        request = "Delete /test.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));      
        
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_404));
    }  
    
    /** 
     * Test case for acceptance.
     * Delete with a URL with redirection.
     * The request is responded with status 302 and a location.
     * @throws Exception
     */
    @Test
    public void testAcceptance_14()
            throws Exception {
        
        final String request = "Delete /redirect/a/b/c HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_302));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://www.xXx.zzz/?a=2/a/b/c"))); 
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_302));
    }    

    /** 
     * Test case for acceptance.
     * Delete with a forbidden URL.
     * The request is responded with status 403.
     * @throws Exception
     */
    @Test
    public void testAcceptance_15()
            throws Exception {
        
        final String request = "Delete /forbidden/absolute.html HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_403));
    }
    
    /** 
     * Test case for acceptance.
     * Delete with Basic-Authentication without access data.
     * The request is responded with status 401.
     * @throws Exception
     */
    @Test
    public void testAcceptance_16()
            throws Exception {
        
        final String request = "Delete /authentication/a/ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));
    }
    
    /** 
     * Test case for acceptance.
     * Delete with Basic Authentication.
     * The request must be responded with status 200 because the access data are
     * included.
     * @throws Exception
     */
    @Test
    public void testAcceptance_17()
            throws Exception {
        
        String request;
        String response;
        
        final File target = new File(AbstractStage.getRootStage(), "documents_vh_A/authentication/a/file.xxx");
        Assert.assertFalse(target.exists());     
        
        request = "Put /authentication/a/file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Authorization: Basic " + Codec.encodeBase64("usr-a:pwd-a") + "\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";        
        AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(target.exists());
        
        request = "Delete /authentication/a/file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Authorization: Basic " + Codec.encodeBase64("usr-a:pwd-a") + "\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";        
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);        
        Assert.assertFalse(target.exists()); 
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LOCATION_DIFFUSE));
        
        request = "Delete /authentication/a/file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Authorization: Basic " + Codec.encodeBase64("usr-a:pwd-a") + "\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";        
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);        
        Assert.assertFalse(target.exists()); 
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));        
    }
}