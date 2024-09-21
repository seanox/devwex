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
import com.seanox.test.TextUtils;

/** Test cases for {@link com.seanox.devwex.Worker}. */
public class WorkerTextTest extends AbstractTest {
    
    private static String textHash(final String string)
            throws Exception {
        return (String)AbstractTestInternalAccess.invoke(Worker.class, "textHash",
                new Object[] {string});
    }

    private static String textEscape(final String string)
            throws Exception {
        return (String)AbstractTestInternalAccess.invoke(Worker.class, "textEscape",
                new Object[] {string});
    }
    
    private static String textDecode(final String string)
            throws Exception {
        return (String)AbstractTestInternalAccess.invoke(Worker.class, "textDecode",
                new Object[] {string});
    }
    
    /** 
     * Test case for acceptance.
     * Test method {@link Worker#textEscape(String)}
     * @throws Exception
     */  
    @Test
    public void testAcceptance_1()
            throws Exception {
        for (int loop = 1; loop < 2; loop += 2)
            Assert.assertEquals("#" + loop + ":", 
                    MockUtils.readTestContent(loop +1),
                    WorkerTextTest.textEscape(MockUtils.readTestContent(loop)));
    }
    
    /** 
     * Test case for acceptance.
     * Test method {@link Worker#textHash(String, String, String)}
     * @throws Exception
     */    
    @Test
    public void testAcceptance_3()
            throws Exception {
        final String content = MockUtils.readTestContent();
        final String[] lines = content.split("\\R"); 
        for (int loop = 0; loop < lines.length; loop += 2) {
            Assert.assertEquals("#" + (loop +1) + ": " + lines[loop], lines[loop +1],
                    WorkerTextTest.textHash(TextUtils.unescape(lines[loop])));
        }
    }
    
    /** 
     * Test case for acceptance.
     * Test method {@link Worker#textDecode(String, String, String)}
     * @throws Exception
     */    
    @Test
    public void testAcceptance_4()
            throws Exception {
        final String content = MockUtils.readTestContent();
        final String[] lines = content.split("\\R"); 
        for (int loop = 0; loop < lines.length; loop += 2) {
            Assert.assertEquals("#" + (loop +1) + ": " + lines[loop], lines[loop +1],
                    WorkerTextTest.textDecode(lines[loop]));
        }
    }
    
    /** 
     * Test case for acceptance.
     * Test method {@link Worker#textEscape(String)}
     * @throws Exception
     */  
    @Test
    public void testAcceptance_5()
            throws Exception {
        final String string = new String(("\ud801\udc00").getBytes("UTF-8"), "UTF-8");
        Assert.assertEquals("\\uD801\\uDC00", WorkerTextTest.textEscape(string));
    }    
}