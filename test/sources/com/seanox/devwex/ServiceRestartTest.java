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

import com.seanox.test.Executor;
import com.seanox.test.Executor.Worker;

/** Test cases for {@link com.seanox.devwex.Service}. */
public class ServiceRestartTest extends AbstractStageTest {
    
    /** 
     * Test case for acceptance.
     * Asynchronous restart must work.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_01()
            throws Exception {
        
        final int loops = 50;
        Executor executor = Executor.create(loops, new Worker() {
            @Override
            protected void execute() {
                Assert.assertTrue(Service.restart());
            }
        });
        
        executor.execute();
        Assert.assertTrue(executor.await(90000));
        
        String outputLog = AbstractStage.getOutputStreamCapture().toString();
        Assert.assertFalse(outputLog.contains(".Exception"));
        Assert.assertFalse(outputLog.contains(".Error"));
        Assert.assertFalse(outputLog.contains(".Throwable"));
        Assert.assertEquals(loops +1, outputLog.split("SERVICE RESTARTED").length);
    } 
}