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
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.seanox.test.Codec;
import com.seanox.test.MockUtils;
import com.seanox.test.Pattern;
import com.seanox.test.StreamUtils;
import com.seanox.test.Timing;

/** Test cases for {@link com.seanox.devwex.Worker}. */
public class WorkerPutTest extends AbstractStageRequestTest {
    
    /** 
     * Test case for acceptance.
     * The creation of directories is responded with status 201 and the location.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_01()
            throws Exception {
        
        String request;
        String response;
        
        request = "Delete /put_test_1\\ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        request = "Head /put_test_1\\ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));

        request = "Put /put_test_1\\ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/put_test_1/")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201)); 
    } 
    
    /** 
     * Test case for acceptance.
     * The creation of directories without Content-length is responded with
     * status 201 and the location.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_02()
            throws Exception {
        
        String request;
        String response;
        
        request = "Delete /put_test_2\\ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        request = "Head /put_test_2\\ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));

        request = "Put /put_test_2 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/put_test_2")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201)); 
    }
    
    /** 
     * Test case for acceptance.
     * The creation of directories with invalid characters in the name is
     * responded with status 500.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_03()
            throws Exception {
        
        final String request = "Put /put_test_2.:::2 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        if (this.isWindows()) {
            Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_500));
            final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
            Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_500));
        } else {
            Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));
            final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
            Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201));
        }
    }
    
    /** 
     * Test case for acceptance.
     * The creation of directories that already exists is responded with status 201
     * and the location.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_04()
            throws Exception {
        
        String request;
        String response;
        
        request = "Head /put_test_2\\ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        
        request = "Put /put_test_1/ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/put_test_1/")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201)); 
    }
    
    /** 
     * Test case for acceptance.
     * The creation of directories without a slash at the end is responded with
     * status 201 and the location with a slash at the end.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_05()
            throws Exception {
        
        final String request= "Put /put_test_1 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_302));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/put_test_1/")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_302)); 
    }
    
    /** 
     * Test case for acceptance.
     * The creation of a directory with subdirectories is responded with status 201
     * and the location to the main directory.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_06()
            throws Exception {
        
        final String request = "Put /put_test_1/x1/x2/x3 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/put_test_1/x1/x2/x3")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201)); 
    }
    
    /** 
     * Test case for acceptance.
     * The creation of a files is responded with status 201 and the location to
     * the file.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_07()
            throws Exception {
        
        final String request = "Put /put_test_1/test_file.1 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 10\r\n"
                + "\r\n"
                + "1234567890";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/put_test_1/test_file.1")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201)); 
    }
    
    /** 
     * Test case for acceptance.
     * The overwriting of an existing a files is responded with status 201 and
     * the location to the file.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_08()
            throws Exception {
        
        String request;
        String response;
        
        request = "Head /put_test_1/test_file.1 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        
        request = "Put /put_test_1/test_file.1 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 10\r\n"
                + "\r\n"
                + "1234567890";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/put_test_1/test_file.1")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201)); 
    }
    
    /** 
     * Test case for acceptance.
     * Overlength is ignored. Only as much data is written, as specified by
     * Content-Length.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_09()
            throws Exception {
        
        String request;
        String response;
        
        request = "Put /put_test_1/test_file.2 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 50\r\n"
                + "\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/put_test_1/test_file.2")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201)); 

        request = "head /put_test_1/test_file.2 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH(50)));
    }
    
    /** 
     * Test case for acceptance.
     * Requests with an invalid Content-Length are responded with status 411.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_10()
            throws Exception {
        
        String request;
        String response;
        
        request = "Put /put_test_1/test_file.3 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: x50\r\n"
                + "\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_411));

        request = "head /put_test_1/test_file.3 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
    }
    
    /** 
     * Test case for acceptance.
     * Requests with an invalid Content-Length are responded with status 411.
     * @throws Exception
     */       
    @Test
    public void testAcceptance_11()
            throws Exception {
        
        String request;
        String response;
        
        request = "Put /put_test_1/test_file.3 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: -1\r\n"
                + "\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n"
                + "1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ--1234567890--ABCDEFGHIJKLMNOPQRSTUVWXYZ **\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_411));

        request = "head /put_test_1/test_file.3 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
    }
    
    /** 
     * Test case for acceptance.
     * The creation of a file that already exists as a directory is responded
     * with status 302 and the location to the directory.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_12()
            throws Exception {

        final String request = "Put /put_test_1/x1 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 10\r\n"
                + "\r\n"
                + "1234567890";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_302));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/put_test_1/x1/")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_302));
    }
    
    /** 
     * Test case for acceptance.
     * The creation of a directory that already exists as a file is responded
     * with status 302 and the location to the file.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_13()
            throws Exception {
        
        final String request = "Put /put_test_1/test_file.1/ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_302));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/put_test_1/test_file.1")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_302));
    }
    
    /** 
     * Test case for acceptance.
     * PUT-requests to a CGI, are executed by the CGI application.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_14()
            throws Exception {
        
        final String request = "Put /method.jsx HTTP/1.0\r\n"
                + "Host: vHe\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LOCATION_DIFFUSE));

        final String body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertEquals("hallo", body);
    }
    
    /** 
     * Test case for acceptance.
     * PUT-requests to a CGI, are executed by the CGI application.
     * The request is responded with status 405 if the PUT method not allowed
     * for the CGI.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_15()
            throws Exception {
        final String request = "Put /method.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_405));
    }
    
    /** 
     * Test case for acceptance.
     * PUT-requests to a module, are executed by the module (/test.module).
     * The path is absolute, therefore also /test.module123 is accepted.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_17()
            throws Exception {
        
        final String request = "Put /test.module123 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS("001 Test ok")));
        Assert.assertTrue(response.matches("(?s)^.*\r\nModule: module.WorkerModule_A::Service\r\n.*$"));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("1")));        
    } 
    
    /** 
     * Test case for acceptance.
     * The path /test.xxx is absolute, therefore also /test.xxx123 is accepted.
     * @throws Exception
     */        
    @Test
    public void testAcceptance_18()
            throws Exception {
        
        String request;
        String response;
        
        request = "Delete /file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        request = "Head /file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));

        request = "Put /test.xxx123 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/test.xxx123")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201)); 
        
        request = "Get /test.xxx123 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));    
    }     
    
    /** 
     * Test case for acceptance.
     * PUT is supported for absolute path.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_19() throws Exception {
        
        String request;
        String response;
        
        request = "Delete /file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        request = "Head /file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));

        request = "Put /test.xxx HTTP/1.0\r\n"
            + "Host: vHa\r\n"
            + "Content-Length: 0\r\n"
            + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/test.xxx")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201)); 

        request = "Get /test.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
    } 
    
    /** 
     * Test case for acceptance.
     * PUT requests to a redirected url is responded with status 302 and the
     * location.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_20()
            throws Exception {
        
        final String request = "Put /redirect/a/b/c/file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_302));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://www.xXx.zzz/?a=2/a/b/c/file.xxx")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_302));
    } 
    
    /** 
     * Test case for acceptance.
     * PUT requests to a forbidden url is responded with status 403.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_21()
            throws Exception {
        final String request = "Put /forbidden/absolute-xxx.html HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));
    } 
    
    /** 
     * Test case for acceptance.
     * PUT requests in combination with an authentication must work.
     * In this case the authentication is not correct and the request is
     * responded with status 401.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_22()
            throws Exception {
        final String request = "Put /authentication/a/test.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_401));
    } 
    
    /** 
     * Test case for acceptance.
     * PUT requests in combination with an authentication must work.
     * In this case the authentication is correct and the request is responded
     * with status 201.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_23()
            throws Exception {
        
        String request;
        String response;
        
        request = "Delete /authentication/a/file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Authorization: Basic " + Codec.encodeBase64("usr-a:pwd-a") + "\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        request = "Head /authentication/a/file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Authorization: Basic " + Codec.encodeBase64("usr-a:pwd-a") + "\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        
        request = "Put /authentication/a/file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Authorization: Basic " + Codec.encodeBase64("usr-a:pwd-a") + "\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://vHa:18185/authentication/a/file.xxx")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("201", request, "usr-a")));

        request = "Get /authentication/a/file.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Authorization: Basic " + Codec.encodeBase64("usr-a:pwd-a") + "\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
    } 
    
    /** 
     * Test case for acceptance.
     * For PUT requests, the number of bytes is written, which is specified with
     * Content-Length. If fewer bytes are present, the server waits until the
     * timeout (TIMEOUT = 15000). In this case the request is responded with
     * status 400.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_24()
            throws Exception {
        
        final Timing timing = Timing.create(true);
        final String request = "Put /put_test_1/test_file.3 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 10\r\n"
                + "\r\n"
                + "12345";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        
        timing.assertTimeIn(16000);
        timing.assertTimeOut(15000);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_400));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_400));
    } 
    
    /** 
     * Test case for acceptance.
     * For large PUT requests.
     * No server timeout may occur and the data must be written correctly.
     * In this case the request is responded with status 201.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_25()
            throws Exception {
        
        String request;
        String response;
        
        request = "Delete /put_test_1/test_file.upload HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
            
        final long size = 3L *1000L *1000L *1000L;
        final InputStream input = MockUtils.createInputStream(size);
        request = "Put /put_test_1/test_file.upload HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: " + size + "\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request, input);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201));

        final File file = new File(AbstractStage.getRootStage(), "documents_vh_A/put_test_1/test_file.upload");
        try {
            Assert.assertEquals(size, file.length());
            try (final FileInputStream fileInputStream = new FileInputStream(file)) {
                Assert.assertEquals("---------E", new String(StreamUtils.tail(fileInputStream, 10)));
            }
        } finally {
            if (file.exists())
                file.delete();
        }
    }     

    /** 
     * Test case for acceptance.
     * PUT-requests without a Content-Length creates a file.
     * @throws Exception
     */       
    @Test
    public void testAcceptance_98()
            throws Exception {
        
        String request;
        String response;
        
        request = "Put /put_test_1/test_file.98 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));

        request = "head /put_test_1/test_file.98/ HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LOCATION_DIFFUSE));
    }    
    
    /** 
     * Test case for acceptance.
     * PUT-requests with a Content-Length creates a file.
     * @throws Exception
     */       
    @Test
    public void testAcceptance_99()
            throws Exception {
        
        String request;
        String response;
        
        request = "Put /put_test_1/test_file.99 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: 0\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));

        request = "head /put_test_1/test_file.99 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18185", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH(0)));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LOCATION_DIFFUSE));
    }
}