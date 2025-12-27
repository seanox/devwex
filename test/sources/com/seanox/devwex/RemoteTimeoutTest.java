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
import com.seanox.test.Timing;

/** Test cases for {@link com.seanox.devwex.Remote}. */
public class RemoteTimeoutTest extends AbstractStageTest {

    /** 
     * Test case for timeout.
     * The data access from remote access is limited to 10 seconds.
     * The request has to be aborted after ca. 10 seconds.
     * A response is not expected.
     * @throws Exception
     */    
    @Test(timeout=30000)
    public void testTimeout_1()
            throws Exception {
        final Timing timing = Timing.create(true);
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18001"));
        timing.assertTimeIn(11000); 
        Assert.assertTrue(response.isEmpty());
    }    
    
    /** 
     * Test case for timeout.
     * The data access from remote access is limited to 10 seconds.
     * The request has to be aborted after ca. 10 seconds, even if a request is
     * started. A response is not expected.
     * @throws Exception
     */   
    @Test(timeout=30000)
    public void testTimeout_2()
            throws Exception {
        final Timing timing = Timing.create(true);
        final String response = new String(HttpUtils.sendRequest("127.0.0.1:18001", "sTatuS"));
        timing.assertTimeIn(11000); 
        Assert.assertTrue(response.isEmpty());
    }     
}