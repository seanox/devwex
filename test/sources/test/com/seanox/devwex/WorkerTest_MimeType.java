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
import com.seanox.test.Pattern;

/**
 * Test cases for {@link com.seanox.devwex.Worker}.
 *
 * @author  Seanox Software Solutions
 * @version 5.5.0 20220908
 */
public class WorkerTest_MimeType extends AbstractStageRequestTest {
    
    /** 
     * Test case for acceptance.
     * For the file extension {@code xxx} was not defined a mimetype.
     * The request must be responded with status 200 and the standard mimetype.
     * @throws Exception
     */
    @Test
    public void testAcceptance_1()
            throws Exception {
        final String request = "GET /mimetype_test.xxx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18080", request));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_OCTET_STREAM));
    }
    
    /** 
     * Test case for acceptance.
     * For the file extension {@code xls} was defined a mimetype.
     * The request must be responded with status 200 and the defined a mimetype.
     * @throws Exception
     */
    @Test
    public void testAcceptance_2()
            throws Exception {
        final String request = "GET /mimetype_test.xls HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18080", request));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_APPLICATION_VND_MS_EXCEL));
    }    
    
    /** 
     * Test case for acceptance.
     * For the file extension {@code xls} was defined a mimetype.
     * The request with {@code Accept: *}{@code /*} must be responded with
     * status 200 and the defined a mimetype.
     * @throws Exception
     */
    @Test
    public void testAcceptance_3()
            throws Exception {
        final String request = "GET /mimetype_test.xls HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Accept: */*\r\n"
                + "\r\n";
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18080", request));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_APPLICATION_VND_MS_EXCEL));
    }  
    
    /** 
     * Test case for acceptance.
     * For the file extension {@code xls} was defined a mimetype.
     * The request with {@code Accept: application/*} must be responded with
     * status 200 and the defined a mimetype.
     * @throws Exception
     */
    @Test
    public void testAcceptance_4()
            throws Exception {
        final String request = "GET /mimetype_test.xls HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Accept: application/*\r\n"
                + "\r\n";
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18080", request));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_APPLICATION_VND_MS_EXCEL));
    }
    
    /** 
     * Test case for acceptance.
     * For the file extension {@code xls} was defined a mimetype.
     * The request with {@code Accept: *}{@code /vnd.ms-excel} must be responded
     * with status 200 and the defined a mimetype.
     * @throws Exception
     */
    @Test
    public void testAcceptance_5()
            throws Exception {
        final String request = "GET /mimetype_test.xls HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Accept: */vnd.ms-excel\r\n"
                + "\r\n";
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18080", request));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_APPLICATION_VND_MS_EXCEL));
    } 
    
    /** 
     * Test case for acceptance.
     * For the file extension {@code xxx} was not defined a mimetype.
     * The request must be responded with status 200 and the standard mimetype.
     * @throws Exception
     */
    @Test
    public void testAcceptance_6()
            throws Exception {
        final String request = "GET /mimetype_test.xxx HTTP/1.0\r\n"
                + "Host: vHi\r\n"
                + "Accept: */*\r\n"
                + "\r\n";
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18080", request));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_OCTET_STREAM));
    }  
    
    /** 
     * Test case for acceptance.
     * Accept and the content-type the server has determined do not match.
     * The request must be responded with status 406.
     * @throws Exception
     */
    @Test
    public void testAcceptance_7()
            throws Exception {
        final String request = "GET /mimetype_test.xxx HTTP/1.0\r\n"
                + "Host: vHi\r\n"
                + "Accept: no/*\r\n"
                + "\r\n";
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18080", request));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_406));
    }
    
    /** 
     * Test case for acceptance.
     * Accept and the content-type the server has determined do not match.
     * The request must be responded with status 406.
     * @throws Exception
     */
    @Test
    public void testAcceptance_8()
            throws Exception {
        final String request = "GET /mimetype_test.xxx HTTP/1.0\r\n"
                + "Host: vHi\r\n"
                + "Accept: */no\r\n"
                + "\r\n";
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18080", request));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_406));
    }  
    
    /** 
     * Test case for acceptance.
     * For the file extension {@code xxx} was not defined a mimetype.
     * The request must be responded with status 200 and the standard mimetype.
     * Accept is blank and is ignored.
     * @throws Exception
     */
    @Test
    public void testAcceptance_9()
            throws Exception {
        final String request = "GET /mimetype_test.xxx HTTP/1.0\r\n"
                + "Host: vHi\r\n"
                + "Accept:     \r\n"
                + "\r\n";
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18080", request));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_OCTET_STREAM));
    } 
}