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
 * Test cases for {@link com.seanox.devwex.Initialize#set(String, Section)}.
 *
 * @author  Seanox Software Solutions
 * @version 5.5.0 20220911
 */
public class InitializeTest_Set extends AbstractTest {
    
    @Test
    public void testKeyInvalid_1() {
        final Initialize initialize = new Initialize();
        Assert.assertNull(initialize.set("", null));
    }
    
    @Test
    public void testKeyInvalid_2() {
        final Initialize initialize = new Initialize();
        Assert.assertNull(initialize.set(" ", null));
    }

    @Test
    public void testKeyInvalid_3() {
        final Initialize initialize = new Initialize();
        Assert.assertNull(initialize.set("   ", null));
    }

    @Test
    public void testKeyInvalid_5() {
        final Initialize initialize = new Initialize();
        Assert.assertNull(initialize.set(null, null));
    }

    @Test
    public void testKeyInvalid_6() {
        final Initialize initialize = new Initialize();
        Assert.assertNull(initialize.set(" \0\0 ", null));
    }

    @Test
    public void testKeyInvalid_7() {
        final Initialize initialize = new Initialize();
        Assert.assertNull(initialize.set(" \r\n ", null));
    }
    
    @Test
    public void testKeyInvalid_8() {
        final Initialize initialize = new Initialize();
        Assert.assertNull(initialize.set(" \07\07 ", null));
    }

    @Test
    public void testKeyInvalid_9() {
        final Initialize initialize = new Initialize();
        Assert.assertNull(initialize.set(" \40\40 ", null));
    }

    @Test
    public void testKeyInvalid_A() {
        final Initialize initialize = new Initialize();
        Assert.assertNull(initialize.set(" \33\33 ", null));
    }
    
    @Test
    public void testKeyTolerance_1() {
        
        final Initialize initialize = new Initialize();
        final Section section1 = new Section();
        initialize.set("A", section1);
        Assert.assertEquals(section1, initialize.get("A"));
        Assert.assertEquals(section1, initialize.get("a"));
        
        final Section section2 = new Section();
        initialize.set("a", section2);
        Assert.assertEquals(section2, initialize.get("A"));
        Assert.assertEquals(section2, initialize.get("a"));
        
        final Section section3 = new Section();
        initialize.set(" a", section3);
        Assert.assertEquals(section3, initialize.get("A"));
        Assert.assertEquals(section3, initialize.get("a"));
        
        final Section section4 = new Section();
        initialize.set(" a ", section4);
        Assert.assertEquals(section4, initialize.get("A"));
        Assert.assertEquals(section4, initialize.get("a"));

        final Section section5 = new Section();
        initialize.set("a ", section5);
        Assert.assertEquals(section5, initialize.get("A"));
        Assert.assertEquals(section5, initialize.get("a"));
    }
    
    @Test
    public void testKeyOverwrite_1() {
        final Initialize initialize = new Initialize();
        final Section section1 = new Section();
        initialize.set("A", section1);
        final Section section2 = new Section();
        initialize.set("a", section2);
        final Section section3 = new Section();
        initialize.set(" A", section3);
        final Section section4 = new Section();
        initialize.set(" a   ", section4);
        Assert.assertEquals(section4, initialize.get("A"));
        Assert.assertEquals(section4, initialize.get("a"));
    }
}