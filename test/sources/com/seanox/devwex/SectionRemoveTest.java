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

/** Test cases for {@link Section#remove(String)}. */
public class SectionRemoveTest extends AbstractTest {

    /** Test case for remove key */
    @Test
    public void testRemove_01() {
        
        final Section section = new Section();
        section.set("A", "1");
        final String removed = section.remove("A");
        
        Assert.assertEquals("1", removed);
        Assert.assertFalse(section.contains("A"));
    }

    /** Test case for remove unknown key */
    @Test
    public void testRemove_02() {
        
        final Section section = new Section();
        section.set("A", "1");
        final String removed = section.remove("B");
        
        Assert.assertNull(removed);
        Assert.assertEquals(1, section.size());
    }

    /** Test case for key normalization (uppercase + trim) */
    @Test
    public void testRemove_03() {
        
        final Section section = new Section();
        section.set(" a ", "1");
        final String removed = section.remove("A");
        
        Assert.assertEquals("1", removed);
        Assert.assertEquals(0, section.size());
    }

    /** Test case for invalid keys */
    @Test
    public void testRemove_04() {
        final Section section = new Section();
        for (final String key : new String[]{null, "", " ", "\t", "\n"}) {
            Assert.assertNull(section.remove(key));
            Assert.assertEquals(0, section.size());
        }
    }
}