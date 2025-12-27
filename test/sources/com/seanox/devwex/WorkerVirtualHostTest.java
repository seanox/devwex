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

import org.junit.Assert;
import org.junit.Test;

import com.seanox.test.HttpUtils;
import com.seanox.test.Pattern;

/** Test cases for {@link com.seanox.devwex.Worker}. */
public class WorkerVirtualHostTest extends AbstractStageTest {
    
    /** 
     * Test case for acceptance.
     * Virtual hosts must be resolved correctly.
     * @throws Exception
     */
    @Test
    public void testAcceptance_01()
            throws Exception {
        
        final String request = "GET \\cgi_environment.jsx HTTP/1.0\r\n"
                + "Host: vhA\r\n"
                + "\r\n";

        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18180", request));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        String body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertTrue(body.matches("(?si)^.*\r\nHTTP_HOST=vhA\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\nVIRTUAL_A=Virtualhost A\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\nSERVER_A=Server A\r\n.*$"));
    }
    
    /** 
     * Test case for acceptance.
     * Virtual hosts must be resolved correctly.
     * @throws Exception
     */
    @Test
    public void testAcceptance_02()
            throws Exception {
        
        final String request = "GET \\cgi_environment.jsx HTTP/1.0\r\n"
                + "Host: vhA\r\n"
                + "\r\n";
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18181", request));
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        final String body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertTrue(body.matches("(?si)^.*\r\nHTTP_HOST=vhA\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\nVIRTUAL_A=Virtualhost A\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\nSERVER_C=Server C\r\n.*$"));
    }
    
    /** 
     * Test case for acceptance.
     * The virtual host must observe the parameter SERVER.
     * @throws Exception
     */
    @Test
    public void testAcceptance_03()
            throws Exception {
        
        for (final int port : new int[] {18181, 18182, 18183, 18080}) {
            final String request = "GET \\cgi_environment.jsx HTTP/1.0\r\n"
                    + "Host: vhS\r\n"
                    + "\r\n";
            final String response = new String(HttpUtils.sendRequest("127.0.0.1:" + port, request));
            Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));

            final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
            Assert.assertTrue(header.trim().length() > 0);
            final String body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
            if (port != 18080)
                Assert.assertTrue(body.matches("(?si)^.*\r\nVIRTUAL_S=Virtualhost S\r\n.*$"));
            else 
                Assert.assertFalse(body.matches("(?si)^.*\r\nVIRTUAL_S=Virtualhost S\r\n.*$"));
        }
    }
    
    /** 
     * Test case for acceptance.
     * The virtual host must observe the parameter SERVER.
     * @throws Exception
     */
    @Test
    public void testAcceptance_04()
            throws Exception {
        
        for (final int port : new int[] {18181, 18182, 18183, 18080}) {
            final String request = "GET \\cgi_environment.jsx HTTP/1.0\r\n"
                    + "Host: vhT\r\n"
                    + "\r\n";
            final String response = new String(HttpUtils.sendRequest("127.0.0.1:" + port, request));
            Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));

            final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
            Assert.assertTrue(header.trim().length() > 0);
            final String body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
            Assert.assertTrue(body.matches("(?si)^.*\r\nVIRTUAL_T=Virtualhost T\r\n.*$"));
        }
    }
}