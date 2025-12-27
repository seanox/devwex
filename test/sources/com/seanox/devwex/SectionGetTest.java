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

/** Test cases for {@link com.seanox.devwex.Section}. */
public class SectionGetTest extends AbstractTest {
    
    @Test
    public void testAcceptance_01() {
        final Section section = new Section();
        for (final String key : new String[] {null, "", " ", " \t ", " \r ", " \n ", " \7 "}) {
            final String value = section.get(key);
            Assert.assertNull(value);
        }
    }
    
    @Test
    public void testAcceptance_02() {
        final Section section = new Section(false);
        for (final String key : new String[] {null, "", " ", " \t ", " \r ", " \n ", " \7 "}) {
            final String value = section.get(key);
            Assert.assertNull(value);
        }
    }    
    
    @Test
    public void testAcceptance_03() {
        final Section section = new Section(true);
        for (final String key : new String[] {null, "", " ", " \t ", " \r ", " \n ", " \7 "}) {
            final String value = section.get(key);
            Assert.assertNotNull(value);
        }
    }

    @Test
    public void testAcceptance_04() {
        final Section section = new Section();
        for (final String key : new String[] {null, "", " ", " \t ", " \r ", " \n ", " \7 "}) {
            final String value = section.get(key, "o");
            Assert.assertEquals("o", value);
            Assert.assertEquals(0, section.size());
        }
    }
    
    @Test
    public void testAcceptance_05() {
        final Section section = new Section(false);
        for (final String key : new String[] {null, "", " ", " \t ", " \r ", " \n ", " \7 "}) {
            final String value = section.get(key, "o");
            Assert.assertEquals("o", value);
            Assert.assertEquals(0, section.size());
        }
    }    
    
    @Test
    public void testAcceptance_06() {
        final Section section = new Section(true);
        for (final String key : new String[] {null, "", " ", " \t ", " \r ", " \n ", " \7 "}) {
            final String value = section.get(key, "o");
            Assert.assertEquals("o", value);
            Assert.assertEquals(0, section.size());
        }
    }    
    
    @Test
    public void testAcceptance_07() {
        final Section section = new Section();
        final String value = section.get("x");
        Assert.assertNull(value);
    }

    @Test
    public void testAcceptance_08() {
        final Section section = new Section(false);
        final String value = section.get("x");
        Assert.assertNull(value);
    }    

    @Test
    public void testAcceptance_09() {
        final Section section = new Section(true);
        final String value = section.get("x");
        Assert.assertEquals("", value);
        Assert.assertEquals(0, section.size());
    }    

    @Test
    public void testAcceptance_10() {
        final Section section = new Section(true);
        final String value = section.get("x", "a");
        Assert.assertEquals("a", value);
        Assert.assertEquals(0, section.size());
    }    
}