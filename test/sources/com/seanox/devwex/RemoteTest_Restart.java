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

/**
 * Test cases for {@link com.seanox.devwex.Remote}.
 *
 * @author  Seanox Software Solutions
 * @version 5.5.0 20220906
 */
public class RemoteTest_Restart extends AbstractStageTest {
    
    /** 
     * Test case for RESTART.
     * Commando @{code RESTART} must restart the server.
     * @throws Exception
     */
    @Test
    public void testRestart()
            throws Exception {
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18001", "RESTaRT\r\n"));
        AbstractStage.getOutputStreamCapture().await("(?s).*\\b\\QSERVICE RESTARTED\\E\\b.*");
        final String outputLog = AbstractStage.getOutputStreamCapture().toString().trim();
        Assert.assertTrue(outputLog.matches("^(?si).*\\R[\\d\\- :]+\\s+SERVICE RESTARTED\\s+\\([\\d\\.]+\\s+SEC\\)(\\R.*)?$"));
        Assert.assertEquals("SERVICE RESTARTED\r\n", response);
    }
}