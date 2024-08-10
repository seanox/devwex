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

/**
 * Test cases for {@link com.seanox.devwex.Section#contains(String)}.
 *
 * @author  Seanox Software Solutions
 * @version 5.5.0 20220911
 */
public class SectionTest_Contains extends AbstractTest {
    
    @Test
    public void testKeyInvalid_1() {
        final Section section = new Section();
        Assert.assertNull(section.set("", null));
    }
    
    @Test
    public void testKeyInvalid_2() {
        final Section section = new Section();
        Assert.assertNull(section.set(" ", null));
    }

    @Test
    public void testKeyInvalid_3() {
        final Section section = new Section();
        Assert.assertNull(section.set("   ", null));
    }

    @Test
    public void testKeyInvalid_5() {
        final Section section = new Section();
        Assert.assertNull(section.set(null, null));
    }

    @Test
    public void testKeyInvalid_6() {
        final Section section = new Section();
        Assert.assertNull(section.set(" \0\0 ", null));
    }

    @Test
    public void testKeyInvalid_7() {
        final Section section = new Section();
        Assert.assertNull(section.set(" \r\n ", null));
    }
    
    @Test
    public void testKeyInvalid_8() {
        final Section section = new Section();
        Assert.assertNull(section.set(" \07\07 ", null));
    }

    @Test
    public void testKeyInvalid_9() {
        final Section section = new Section();
        Assert.assertNull(section.set(" \40\40 ", null));
    }

    @Test
    public void testKeyInvalid_A() {
        final Section section = new Section();
        Assert.assertNull(section.set(" \33\33 ", null));
    }
    
    @Test
    public void testKeyTolerance_1() {

        final Section section = new Section();
        section.set("A", "a1");
        Assert.assertEquals("a1", section.get("A"));
        Assert.assertEquals("a1", section.get("a"));
        
        section.set("a", "a2");
        Assert.assertEquals("a2", section.get("A"));
        Assert.assertEquals("a2", section.get("a"));
        
        section.set(" a", "a3");
        Assert.assertEquals("a3", section.get("A"));
        Assert.assertEquals("a3", section.get("a"));
        
        section.set(" a ", "a4");
        Assert.assertEquals("a4", section.get("A"));
        Assert.assertEquals("a4", section.get("a"));

        section.set("a ", "a5");
        Assert.assertEquals("a5", section.get("A"));
        Assert.assertEquals("a5", section.get("a"));
    }
    
    @Test
    public void testKeyOverwrite_1() {
        final Section section = new Section();
        section.set("A", "a1");
        section.set("a", "a2");
        section.set(" A", "a3");
        section.set(" a   ", "a4");
        Assert.assertEquals("a4", section.get("A"));
        Assert.assertEquals("a4", section.get("a"));
    }
}