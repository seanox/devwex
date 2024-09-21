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

/** Test cases for {@link com.seanox.devwex.Remote}. */
public class RemoteInvalidTest extends AbstractStageTest {
    
    /** 
     * Test case for an unknown command and overlength.
     * The length of the request is limited to 65535 bytes and must be responded
     * with {@code INFO: UNKNOWN COMMAND}.
     * @throws Exception
     */
    @Test
    public void testUnknownCommand_1()
            throws Exception {
        final StringBuilder command = new StringBuilder();
        while (command.length() < 65536)
            command.append("XXXXXXXXX");
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18001", command + "\r\n"));
        Assert.assertEquals("UNKNOWN COMMAND\r\n", response);
    }
    
    /** 
     * Test case for an unknown command.
     * The command {@code Restar[\r\n]123} is invalid and must be responded with
     * {@code INFO: UNKNOWN COMMAND}.
     * @throws Exception
     */
    @Test
    public void testUnknownCommand_2()
            throws Exception {
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18001", "restar\r\n123"));
        Assert.assertEquals("UNKNOWN COMMAND\r\n", response);
    }
}