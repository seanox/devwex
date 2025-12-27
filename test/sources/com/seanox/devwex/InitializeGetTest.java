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

/** Test cases for {@link com.seanox.devwex.Initialize}. */
public class InitializeGetTest extends AbstractTest {
    
    @Test
    public void testAcceptance_01() {
        final Initialize initialize = new Initialize();
        for (final String key : new String[] {null, "", " ", " \t ", " \r ", " \n ", " \7 "}) {
            final Section section = initialize.get(key);
            Assert.assertNull(section);
        }
    }
    
    @Test
    public void testAcceptance_02() {
        final Initialize initialize = new Initialize(false);
        for (final String key : new String[] {null, "", " ", " \t ", " \r ", " \n ", " \7 "}) {
            final Section section = initialize.get(key);
            Assert.assertNull(section);
        }
    }    
    
    @Test
    public void testAcceptance_03() {
        final Initialize initialize = new Initialize(true);
        for (final String key : new String[] {null, "", " ", " \t ", " \r ", " \n ", " \7 "}) {
            final Section section = initialize.get(key);
            Assert.assertNull(section);
        }
    }
    
    @Test
    public void testAcceptance_04() {
        final Initialize initialize = new Initialize();
        final Section section = initialize.get("x");
        Assert.assertNull(section);
    }
    
    @Test
    public void testAcceptance_05() {
        final Initialize initialize = new Initialize(false);
        final Section section = initialize.get("x");
        Assert.assertNull(section);
    }    
    
    @Test
    public void testAcceptance_06() {
        final Initialize initialize = new Initialize(true);
        final Section section1 = initialize.get("x");
        Assert.assertNotNull(section1);
        Assert.assertEquals(1, initialize.size());
        section1.set("a", "1");
        final Section section2 = initialize.get("x");
        Assert.assertNotNull(section2);
        Assert.assertEquals(1, section2.size());
        Assert.assertEquals("1", section2.get("a"));
    }     
}