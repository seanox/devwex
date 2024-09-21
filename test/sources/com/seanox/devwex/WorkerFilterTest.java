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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.seanox.test.Pattern;

/** Test cases for {@link com.seanox.devwex.Worker}. */
public class WorkerFilterTest extends AbstractStageRequestTest {
    
    /** 
     * Test case for acceptance.
     * Without 'Feld-C' the request is responded with status 403.
     * filter expression: {@code GET IS EMPTY FELD-C}
     * @throws Exception
     */
    @Test
    public void testAcceptance_01()
            throws Exception {
        
        String request;
        String response;
        
        request = "GET / HTTP/1.0\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));
        
        request = "GET / HTTP/1.0\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
    }    
    
    /** 
     * Test case for acceptance.
     * The filter method ALL is working for all methods, also unknown methods.
     * The request is responded with status 403.
     * filter expression: {@code ALL IS CONTAINS REQUEST ABC}
     * @throws Exception
     */
    @Test
    public void testAcceptance_02()
            throws Exception {
        
        String request;
        String response;
        
        request = "GET /xxxAbCxxx HTTP/1.0\r\n"
                + "FELD-C: xxx\r\n"
                + "FELD-A: xxx\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));
        
        request = "GET /xxxAbxxx HTTP/1.0\r\n"
                + "FELD-C: xxx\r\n"
                + "FELD-A: xxx\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        
        request = "HEAD /xxxAbCxxx HTTP/1.0\r\n"
                + "FELD-C: xxx\r\n"
                + "FELD-A: xxx\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));
        
        request = "HEAD /xxxAbxxx HTTP/1.0\r\n"
                + "FELD-C: xxx\r\n"
                + "FELD-A: xxx\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));        
        
        request = "INVALID_METHOD /xxxAbCxxx HTTP/1.0\r\n"
                + "FELD-C: xxx\r\n"
                + "FELD-A: xxx\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));  
        
        request = "INVALID_METHOD /xxxAbxxx HTTP/1.0\r\n"
                + "FELD-C: xxx\r\n"
                + "FELD-A: xxx\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_405));  
    }  
    
    /** 
     * Test case for acceptance.
     * If 'FeldA' contains 'A1', the HEAD request is responded with status 403
     * but the GET request is responded with status 200.
     * filter expression: {@code HEAD IS CONTAINS FELDA A1}
     * @throws Exception
     */
    @Test
    public void testAcceptance_03()
            throws Exception {
        
        String request;
        String response;
        
        request = "HEAD / HTTP/1.0\r\n"
                + "Felda: BA12\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));
        
        request = "GET / HTTP/1.0\r\n"
                + "Felda: BA12\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));        
    } 
    
    /** 
     * Test case for acceptance.
     * If 'FeldA' ends not with 'A2', the request is responded with status 200
     * but with 'A2' at the end, the request is responded with status 403.
     * filter expression: {@code HEAD IS ENDS FELDA A2}
     * @throws Exception
     */
    @Test
    public void testAcceptance_04()
            throws Exception {
        
        String request;
        String response;
        
        request = "HEAD / HTTP/1.0\r\n"
                + "Felda: BA21\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        
        request = "HEAD / HTTP/1.0\r\n"
                + "Felda: 1BA2\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));   
    }
    
    /** 
     * Test case for acceptance.
     * The filter must also work with encoded special characters (MIME/UFT8).
     * The request is responded with status 403.
     * filter expression: {@code GET IS CONTAINS PATH -%df%c3%9f-}
     * @throws Exception
     */
    @Test
    public void testAcceptance_05()
            throws Exception {
        
        final String request = "GET /xxx-%df%c3%9f- HTTP/1.0\r\n\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));  
    } 
    
    /** 
     * Test case for acceptance.
     * If 'FeldA' starts not with 'A3', the request is responded with status 200
     * but with 'A3' at the beginn, the request is responded with status 403.
     * filter expression: {@code HEAD IS STARTS FELDA A3}
     * @throws Exception
     */
    @Test
    public void testAcceptance_06()
            throws Exception {
        
        String request;
        String response;
        
        request = "HEAD / HTTP/1.0\r\n"
                + "Felda: BA3\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        
        request = "HEAD / HTTP/1.0\r\n"
                + "Felda: A3B\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));   
    }  
    
    /** 
     * Test case for acceptance.
     * The filter must also work with encoded special characters (MIME/UFT8).
     * The request is responded with status 403.
     * filter expression: {@code GET IS CONTAINS PATH -%c3%9f-}
     * @throws Exception
     */
    @Test
    public void testAcceptance_07()
            throws Exception {
        
        final String request = "GET /xxx-%c3%9f- HTTP/1.0\r\n\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));  
    }
    
    /** 
     * Test case for acceptance.
     * If 'FeldA' equals not 'A4', the request is responded with status 200
     * but 'FeldA' equals 'A4', the request is responded with status 403.
     * filter expression: {@code HEAD IS EQUALS FELDA A4}
     * @throws Exception
     */
    @Test
    public void testAcceptance_08()
            throws Exception {
        
        String request;
        String response;
        
        request = "HEAD / HTTP/1.0\r\n"
                + "Felda: BA4\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        
        request = "HEAD / HTTP/1.0\r\n"
                + "Felda: a4\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));   
    }  
    
    /** 
     * Test case for acceptance.
     * The filter must also work with encoded special characters (MIME/UFT8).
     * The request is responded with status 403.
     * filter expression: {@code GET IS CONTAINS PATH -%c3%9f%df-}
     * @throws Exception
     */
    @Test
    public void testAcceptance_09()
            throws Exception {
        
        final String request = "GET /xxx-%c3%9f%df- HTTP/1.0\r\n\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));  
    }
    
    /** 
     * Test case for acceptance.
     * If 'FeldA' equals not 'A4', the request is responded with status 200
     * but 'FeldA' equals 'A4', the request is responded with status 403.
     * filter expression: {@code HEAD IS EQUALS FELDA A4}
     * @throws Exception
     */
    @Test
    public void testAcceptance_10()
            throws Exception {
        
        String request;
        String response;
        
        request = "GET / HTTP/1.0\r\n"
                + "Felda: B1\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));        

        request = "GET / HTTP/1.0\r\n"
                + "Feldb: B2\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        
        request = "GET / HTTP/1.0\r\n"
                + "Feldb: B2\r\n"
                + "Felda: B1\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));   
    } 
    
    /** 
     * Test case for acceptance.
     * The filter must also work with encoded special characters (MIME/UFT8).
     * The request is responded with status 403.
     * filter expression: {@code GET IS CONTAINS PATH -%DF-}
     * @throws Exception
     */
    @Test
    public void testAcceptance_11()
            throws Exception {
        
        final String request = "GET /xxx-%DF- HTTP/1.0\r\n\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));  
    }
    
    /** 
     * Test case for acceptance.
     * The filter must also work with encoded special characters (MIME/UFT8).
     * The request is responded with status 403.
     * filter expression: {@code GET IS CONTAINS PATH -%DF-}
     * @throws Exception
     */
    @Test
    public void testAcceptance_12()
            throws Exception {
        
        final String request = "GET /xxx-\u00DF- HTTP/1.0\r\n\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));  
    } 
    
    /** 
     * Test case for acceptance.
     * If the request contains {@code Felda: t1} and {@code Felda: field-C},
     * the request is responded with status 200 and the content of
     * {@code ../documents/filter_a.html}.
     * filter expression: {@code GET IS CONTAINS FELDA T1 > ./stage/documents/filter_a.html}
     * @throws Exception
     */
    @Test
    public void testAcceptance_13()
            throws Exception {
        
        final String request = "GET / HTTP/1.0\r\n"
                + "Felda: t1\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));  
        Assert.assertTrue(response.contains("filter_a.html"));
    } 
    
    /** 
     * Test case for acceptance.
     * If the request contains {@code Feldo: t1} the request is responded
     * with status 403 because the target {@code ../documents/filter_a.html_}
     * does not exists.
     * filter expression: {@code GET IS CONTAINS FELDO T1 > ./stage/documents/filter_a.html_}
     * @throws Exception
     */
    @Test
    public void testAcceptance_14()
            throws Exception {
        
        final String request = "GET / HTTP/1.0\r\n"
                + "Feldo: t1\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));
    }   
    
    /** 
     * Test case for acceptance.
     * If the request contains {@code Felda: t2} the request is responded with
     * status 302 and a redirect/location to {@code http://www.xxx.zz/a=1}.
     * filter expression: {@code GET IS CONTAINS FELDA T2 > http://www.xxx.zz/a=1 [r]}
     * @throws Exception
     */
    @Test
    public void testAcceptance_15()
            throws Exception {
        
        final String request = "GET / HTTP/1.0\r\n"
                + "Felda: t2\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_302));  
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_LOCATION("http://www.xxx.zz/a=1")));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_302));
    }
    
    /** 
     * Test case for acceptance.
     * If 'FeldA' equals not '3B1BB2', the request is responded with status 200
     * but 'FeldA' equals '3B1BB2', the request is responded with status 403.
     * filter expression: {@code GET NOT EQUALS FELDA 3B1BB2}
     * @throws Exception
     */
    @Test
    public void testAcceptance_16()
            throws Exception {
        
        String request;
        String response;
        
        request = "GET / HTTP/1.0\r\n"
                + "Felda: 3B1BB2\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18187", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));  

        request = "GET / HTTP/1.0\r\n"
                + "Felda: 3BBB2\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18187", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));  
    }  
    
    /** 
     * Test case for acceptance.
     * If the request contains {@code Felda: t4} the request is responded with
     * status 403 because for the redirect is the option {@code [R]} is
     * missing.
     * filter expression: {@code GET IS CONTAINS FELDA T4 > http://www.xxx.zz/a=1}
     * @throws Exception
     */
    @Test
    public void testAcceptance_17()
            throws Exception {
        
        final String request = "GET / HTTP/1.0\r\n"
                + "Felda: t4\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));  
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LOCATION_DIFFUSE));
    }  
    
    /** 
     * Test case for acceptance.
     * If the request contains {@code Felda: t6} and {@code Feldb: t7} the
     * request is responded with status 200 and the content of
     * {@code ../documents/filter_a.html}.
     * filter expression: {@code GET IS CONTAINS FELDA T6[A] GET IS CONTAINS FELDB T7 > ./stage/documents/filter_a.html}
     * @throws Exception
     */
    @Test
    public void testAcceptance_18()
            throws Exception {
        
        final String request = "GET / HTTP/1.0\r\n"
                + "Felda: t6\r\n"
                + "Feldb: t7\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));  
        Assert.assertTrue(response.contains("filter_a.html"));
    } 
    
    /** 
     * Test case for acceptance.
     * If the request contains {@code Felda: t5}, then a module should be
     * used. But the module definition is incorrect, the option {@code [M]} is
     * missing here. So the request is responded with status 403.
     * filter expression: {@code GET IS CONTAINS FELDA T5 > module.WorkerModule_A}
     * @throws Exception
     */
    @Test
    public void testAcceptance_19()
            throws Exception {
        
        final String request = "GET / HTTP/1.0\r\n"
                + "Felda: t5\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));  
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LOCATION_DIFFUSE));
    }  
    
    /** 
     * Test case for acceptance.
     * If the request contains {@code Felda: t3}, then a module should be
     * used. So the request is responded with status 001.
     * filter expression: {@code GET IS CONTAINS FELDA T3 > module.WorkerModule_A [pA=3] [m]}
     * @throws Exception
     */
    @Test
    public void testAcceptance_20()
            throws Exception {
        
        final String request = "GET / HTTP/1.0\r\n"
                + "Felda: t3\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS("001 Test ok")));
        Assert.assertTrue(response.matches("(?s)^.*\r\nModule: module.WorkerModule_A::Filter\r\n.*$"));
        Assert.assertTrue(response.matches("(?s)^.*\r\nOpts: module.WorkerModule_A \\[pA=3\\] \\[m\\]\r\n.*$"));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("1")));        
    }
    
    /** 
     * Test case for acceptance.
     * VHP has three ALWAYS filters.
     * All filters are executed because no filter changes the server status.
     * The request is responded with status 200 and a test file is created in
     * DocRoot. Each filter increases the value in the file. Finally, the file
     * must contain the value "3".
     * filter expression: {@code ALL ALWAYS > module.WorkerModule_E [M]}
     * @throws Exception
     */
    @Test
    public void testAcceptance_21()
            throws Exception {
        
        final String docRoot = AbstractStage.getRootStage() + "/documents_vh_P";
        final Path testFile = Paths.get(docRoot, "test.txt");
        if (Files.exists(testFile))
            Files.delete(testFile);
        Assert.assertFalse(Files.exists(testFile));
        
        final String request = "GET / HTTP/1.0\r\n"
                + "Host: vHp\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));  
        Assert.assertTrue(Files.exists(testFile));
        final String fileContent = new String(Files.readAllBytes(testFile));
        Assert.assertEquals("3", fileContent);
    }
    
    /** 
     * Test case for acceptance.
     * Configuration:
     *     {@code FLT: GET IS CONTAINS QUERY -404- > module.WorkerModule_B [M]}
     *     {@code REF: /env.test > module.WorkerModule_C [M]}
     * The request is responded with status 404 even the path is defined as
     * module.
     * @throws Exception
     */
    @Test
    public void testAcceptance_22()
            throws Exception {

        String request;
        String response;
        String accessLog;
        
        request = "GET /env.test?a-404- HTTP/1.0\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));  
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_404));  
        
        request = "GET /env.test?a-4x04- HTTP/1.0\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS("003")));  
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("3")));
    }

    /** 
     * Test case for acceptance.
     * Configuration:
     *     {@code FLT: PUT IS CONTAINS QUERY -404- > vF [M]}
     *     {@code REF: /env.test > module.WorkerModule_C [M]}
     * The request is responded with status 201 because the status 404 + PUT is
     * equals to a ressource that does not exist. This is the normal situation
     * to create a new ressource.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_23()
            throws Exception {

        String request;
        String response;
        String accessLog;
        
        final String docRoot = AbstractStage.getRootStage() + "/documents";
        final Path testFile = Paths.get(docRoot, "xxxxx");
        if (Files.exists(testFile))
            Files.delete(testFile);
        Assert.assertFalse(Files.exists(testFile));
        
        request = "PUT /xxxxx?a-404- HTTP/1.0\r\n"
                + "Content-Length: 10\r\n"
                + "\r\n"
                + "1234567890";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));  
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertEquals(10, Files.size(testFile));

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201));  
        
        request = "PUT /xxxxx?a-4x04- HTTP/1.0\r\n"
                + "Content-Length: 10\r\n"
                + "\r\n"
                + "1234567890";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));  
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertEquals(10, Files.size(testFile));

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201));   
        
        request = "PUT /xxxxx?a-4x04- HTTP/1.0\r\n"
                + "Content-Length: 8\r\n"
                + "\r\n"
                + "12345678";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_201));  
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertEquals(8, Files.size(testFile));

        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_201));   
        
        if (Files.exists(testFile))
            Files.delete(testFile);
        Assert.assertFalse(Files.exists(testFile));
    }
    
    /** 
     * Test case for acceptance.
     * Method {@code ALL} was defined for the CGI but {@code METHODS} does this
     * not allow and the request is responded with status 403.
     * @throws Exception
     */
    @Test
    public void testAcceptance_24()
            throws Exception {
        
        String request;
        String response;
        
        request = "GET /nix HTTP/1.0\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        
        request = "GET /nix?xXx1 HTTP/1.0\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));  
        
        request = "GET /nix?xXx HTTP/1.0\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));    

        request = "GET /nix?xxx HTTP/1.0\r\n"
                + "Feld-C: 123\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18186", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));    
    }  
}