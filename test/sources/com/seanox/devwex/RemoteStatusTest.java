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

/** Test cases for {@link com.seanox.devwex.Remote}. */
public class RemoteStatusTest extends AbstractStageTest {
    
    /** 
     * Test case for command: STATUS.
     * The command {@code STATUS[CR]} must return {@code VERS}, {@code TIME},
     * {@code TIUP} and {@code SAPI}.
     * @throws Exception
     */
    @Test
    public void testStatus_1()
            throws Exception {
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18001", "sTatuS\r"));
        Assert.assertNotNull(response);
        Assert.assertTrue(response, response.contains("\r\nSAPI: "));
        Assert.assertTrue(response, response.contains("\r\nTIME: "));
        Assert.assertTrue(response, response.contains("\r\nTIUP: "));
    }
    
    /** 
     * Test case for command: STATUS.
     * The command {@code STATUS[LF]} must return {@code VERS}, {@code TIME},
     * {@code TIUP} and {@code SAPI}.
     * @throws Exception
     */
    @Test
    public void testStatus_2()
            throws Exception {
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18001", "sTatuS\n"));
        Assert.assertNotNull(response);
        Assert.assertTrue(response, response.contains("\r\nSAPI: "));
        Assert.assertTrue(response, response.contains("\r\nTIME: "));
        Assert.assertTrue(response, response.contains("\r\nTIUP: "));
    }
    
    /** 
     * Test case for command: STATUS.
     * The command {@code STATUS[CRLF]} must return {@code VERS}, {@code TIME},
     * {@code TIUP} and {@code SAPI}.
     * @throws Exception
     */
    @Test
    public void testStatus_3()
            throws Exception {
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18001", "sTatuS\r\n"));
        Assert.assertNotNull(response);
        Assert.assertTrue(response, response.contains("\r\nSAPI: "));
        Assert.assertTrue(response, response.contains("\r\nTIME: "));
        Assert.assertTrue(response, response.contains("\r\nTIUP: "));
    }
}