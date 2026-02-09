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

/** Test cases for {@link Section#merge(Section)}. */
public class SectionMergeTest extends AbstractTest {

    @Test
    public void testMerge_01() {
        
        final Section base = new Section(false);
        base.set("A", "1");
        final Section other = new Section(false);
        other.set("B", "2");
        base.merge(other);
        
        Assert.assertEquals("1", base.get("A"));
        Assert.assertEquals("2", base.get("B"));
        Assert.assertEquals(2, base.size());
    }

    /** Test case for overwrite */
    @Test
    public void testMerge_02() {
        
        final Section base = new Section(false);
        base.set("A", "1");
        final Section other = new Section(false);
        other.set("A", "X"); // overwrite
        base.merge(other);
        
        Assert.assertEquals("X", base.get("A"));
        Assert.assertEquals(1, base.size());
    }

    /** Test case for empty (value is allowed in normal mode) */
    @Test
    public void testMerge_03() {
        
        final Section base = new Section(false);
        base.set("A", "1");
        final Section other = new Section(false);
        other.set("A", "");
        base.merge(other);

        Assert.assertEquals("", base.get("A"));
        Assert.assertEquals(1, base.size());
    }
    
    
    @Test
    public void testMerge_11() {
        
        final Section base = new Section(true);
        base.set("A", "1");
        final Section other = new Section(true);
        other.set("B", "2");
        base.merge(other);
        
        Assert.assertEquals("1", base.get("A"));
        Assert.assertEquals("2", base.get("B"));
        Assert.assertEquals(2, base.size());
    }

    /** Test case for overwrite */

    @Test
    public void testMerge_12() {
        
        final Section base = new Section(true);
        base.set("A", "1");
        final Section other = new Section(true);
        other.set("A", "X");
        base.merge(other);
        
        Assert.assertEquals("X", base.get("A"));
        Assert.assertEquals(1, base.size());
    }

    /** Test case for empty (remove in smart mode) */
    @Test
    public void testMerge_13() {
        
        final Section base = new Section(true);
        base.set("A", "1");
        final Section other = new Section(true);
        other.set("A", "");
        base.merge(other);
        
        Assert.assertTrue(base.contains("A"));
        Assert.assertEquals(1, base.size());
    }

    /** Test case for empty (ignored, not added) */
    @Test
    public void testMerge_14() {
        
        final Section base = new Section(true);
        final Section other = new Section(true);
        
        other.set("A", "");
        base.merge(other);
        Assert.assertEquals(0, base.size());
    }
}