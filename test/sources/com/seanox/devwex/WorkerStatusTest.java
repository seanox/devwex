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

/** Test cases for {@link com.seanox.devwex.Worker}. */
public class WorkerStatusTest extends AbstractStageTest {
    
    /** 
     * Test case for acceptance.
     * Request of a forbidden url.
     * Template for status 403 is not dedicated and is contained in status-4xx.html.
     * The response must contains 'Template: status-4xx.html'.
     * @throws Exception
     */
    @Test
    public void testAcceptance_1()
            throws Exception {
        final String request = "Get /forbidden.html HTTP/1.0\r\n"
                + "Host: vHo\r\n"
                + "\r\n";
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18185", request));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_403));
        Assert.assertTrue(response.contains("Template: status-4xx.html"));
    }
    
    /** 
     * Test case for acceptance.
     * Request of a faulty CGI application.
     * Template for status 502 is not dedicated and is contained in status.html.
     * The response must contains 'Template: status.html'.
     * @throws Exception
     */
    @Test
    public void testAcceptance_2()
            throws Exception {
        final String request = "Get /error.cgi HTTP/1.0\r\n"
                + "Host: vHo\r\n"
                + "\r\n";
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18185", request));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_502));
        Assert.assertTrue(response.contains("Template: status.html"));
    }
    
    /** 
     * Test case for acceptance.
     * Request of a not exsting url.
     * Template for status 404 is dedicated.
     * The response must contains 'Template: status-404.html'.
     * @throws Exception
     */
    @Test
    public void testAcceptance_3()
            throws Exception {
        final String request = "Get /not_found.html HTTP/1.0\r\n"
                + "Host: vHo\r\n"
                + "\r\n";
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18185", request));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        Assert.assertTrue(response.contains("Template: status-404.html"));
    }    
}