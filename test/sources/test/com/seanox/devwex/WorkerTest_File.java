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

import com.seanox.test.MockUtils;

/**
 * Test cases for {@link com.seanox.devwex.Worker}.
 *
 * @author  Seanox Software Solutions
 * @version 5.5.0 20220827
 */
public class WorkerTest_File extends AbstractTest {
    
    private static String fileNormalize(final String path)
            throws Exception {
        return (String)AbstractTestInternalAccess.invoke(Worker.class, "fileNormalize",
                new Object[] {path});
    }
    
    /** 
     * Test case for acceptance.
     * Test method {@link Worker#fileNormalize(String)}
     * @throws Exception
     */ 
    @Test
    public void testAcceptance_1()
            throws Exception {
        final String content = MockUtils.readTestContent();
        final String[] lines = content.split("\\R");
        for (int loop = 0; loop < lines.length; loop += 2) {
            final String pattern = lines[loop +1];
            final String value = WorkerTest_File.fileNormalize(lines[loop]);
            Assert.assertEquals("#" + (loop +1) + ": " + lines[loop], pattern, value);
        }
    }
}