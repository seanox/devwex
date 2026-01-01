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

/** Test cases for {@link Settings#contains(String)}. */
public class SettingsContainsTest extends AbstractTest {
    
    @Test
    public void testKeyInvalid_1() {
        final Settings settings = new Settings();
        Assert.assertNull(settings.set("", null));
    }

    @Test
    public void testKeyInvalid_2() {
        final Settings settings = new Settings();
        Assert.assertNull(settings.set(" ", null));
    }

    @Test
    public void testKeyInvalid_3() {
        final Settings settings = new Settings();
        Assert.assertNull(settings.set("   ", null));
    }

    @Test
    public void testKeyInvalid_5() {
        final Settings settings = new Settings();
        Assert.assertNull(settings.set(null, null));
    }

    @Test
    public void testKeyInvalid_6() {
        final Settings settings = new Settings();
        Assert.assertNull(settings.set(" \0\0 ", null));
    }

    @Test
    public void testKeyInvalid_7() {
        final Settings settings = new Settings();
        Assert.assertNull(settings.set(" \r\n ", null));
    }

    @Test
    public void testKeyInvalid_8() {
        final Settings settings = new Settings();
        Assert.assertNull(settings.set(" \07\07 ", null));
    }

    @Test
    public void testKeyInvalid_9() {
        final Settings settings = new Settings();
        Assert.assertNull(settings.set(" \40\40 ", null));
    }

    @Test
    public void testKeyInvalid_A() {
        final Settings settings = new Settings();
        Assert.assertNull(settings.set(" \33\33 ", null));
    }
    
    @Test
    public void testKeyTolerance_1() {

        final Settings settings = new Settings();

        final Section section1 = new Section();
        settings.set("A", section1);
        Assert.assertEquals(section1, settings.get("A"));
        Assert.assertEquals(section1, settings.get("a"));
        
        final Section section2 = new Section();
        settings.set("a", section2);
        Assert.assertEquals(section2, settings.get("A"));
        Assert.assertEquals(section2, settings.get("a"));
        
        final Section section3 = new Section();
        settings.set(" a", section3);
        Assert.assertEquals(section3, settings.get("A"));
        Assert.assertEquals(section3, settings.get("a"));
        
        final Section section4 = new Section();
        settings.set(" a ", section4);
        Assert.assertEquals(section4, settings.get("A"));
        Assert.assertEquals(section4, settings.get("a"));

        final Section section5 = new Section();
        settings.set("a ", section5);
        Assert.assertEquals(section5, settings.get("A"));
        Assert.assertEquals(section5, settings.get("a"));
    }
    
    @Test
    public void testKeyOverwrite_1() {
        final Settings settings = new Settings();
        final Section section1 = new Section();
        settings.set("A", section1);
        final Section section2 = new Section();
        settings.set("a", section2);
        final Section section3 = new Section();
        settings.set(" A", section3);
        final Section section4 = new Section();
        settings.set(" a   ", section4);
        Assert.assertEquals(section4, settings.get("A"));
        Assert.assertEquals(section4, settings.get("a"));
    }
}