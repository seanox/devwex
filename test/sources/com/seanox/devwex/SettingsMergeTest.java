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

/** Test cases for {@link Settings#merge(Settings)}. */
public class SettingsMergeTest extends AbstractTest {

    @Test
    public void testMerge_01() {

        final Settings base = new Settings(false);
        final Section a1 = new Section(false);
        a1.set("X", "1");
        base.set("A", a1);
        final Settings other = new Settings(false);
        final Section a2 = new Section(false);
        a2.set("Y", "2");
        other.set("A", a2);
        base.merge(other);

        final Section merged = base.get("A");
        Assert.assertEquals("1", merged.get("X"));
        Assert.assertEquals("2", merged.get("Y"));
        Assert.assertEquals(1, base.size());
    }

    /** Test case for overwrite */
    @Test
    public void testMerge_02() {

        final Settings base = new Settings(false);
        final Section a1 = new Section(false);
        a1.set("X", "1");
        base.set("A", a1);
        final Settings other = new Settings(false);
        final Section a2 = new Section(false);
        a2.set("X", "Z");
        other.set("A", a2);
        base.merge(other);

        final Section merged = base.get("A");
        Assert.assertEquals("Z", merged.get("X"));
    }

    /** Test case for empty (allowed in normal mode) */
    @Test
    public void testMerge_03() {

        final Settings base = new Settings(false);
        final Section a1 = new Section(false);
        a1.set("X", "1");
        base.set("A", a1);
        final Settings other = new Settings(false);
        final Section a2 = new Section(false);
        a2.set("X", "");
        other.set("A", a2);
        base.merge(other);

        final Section merged = base.get("A");
        Assert.assertEquals("", merged.get("X"));
    }

    @Test
    public void testMerge_11() {

        final Settings base = new Settings(true);
        final Section a1 = new Section(true);
        a1.set("X", "1");
        base.set("A", a1);
        final Settings other = new Settings(true);
        final Section a2 = new Section(true);
        a2.set("Y", "2");
        other.set("A", a2);
        base.merge(other);

        final Section merged = base.get("A");
        Assert.assertEquals("1", merged.get("X"));
        Assert.assertEquals("2", merged.get("Y"));
    }

    /** Test case for: overwrite */
    @Test
    public void testMerge_12() {

        final Settings base = new Settings(true);
        final Section a1 = new Section(true);
        a1.set("X", "1");
        base.set("A", a1);
        final Settings other = new Settings(true);
        final Section a2 = new Section(true);
        a2.set("X", "Z");
        other.set("A", a2);
        base.merge(other);

        final Section merged = base.get("A");
        Assert.assertEquals("Z", merged.get("X"));
    }

    /** Test case for empty (remove in smart mode) */
    @Test
    public void testMerge_13() {

        final Section a1 = new Section(false);
        a1.set("X", "1");
        final Settings base = new Settings(true);
        base.set("A", a1);
        
        final Section a2 = new Section(false);
        a2.set("X", "");
        final Settings other = new Settings(true);
        other.set("A", a2);
        
        base.merge(other);

        Assert.assertTrue(base.contains("A"));
        Assert.assertTrue(base.get("A").contains("X"));
        Assert.assertEquals("", base.get("A").get("X"));
    }

    
    /** Test case for empty (remove in smart mode) */
    @Test
    public void testMerge_14() {

        final Section a1 = new Section(true);
        a1.set("X", "1");
        final Settings base = new Settings(true);
        base.set("A", a1);
        
        final Section a2 = new Section(true);
        a2.set("X", "");
        final Settings other = new Settings(true);
        other.set("A", a2);
        
        base.merge(other);
        
        // In smart Sections, empty values cause the key to be removed from the
        // Section itself. During merging, this ensures that a missing key in
        // the source Section does not overwrite an existing value in the target
        // Section. Therefore, the value of X in base remains 1.
        
        Assert.assertTrue(base.contains("A"));
        Assert.assertTrue(base.get("A").contains("X"));
        Assert.assertEquals("1", base.get("A").get("X"));
    }
    
    /** Test case for empty (section, ignored + no change) */
    @Test
    public void testMerge_15() {

        final Settings base = new Settings(true);
        final Section a1 = new Section(true);
        a1.set("X", "1");
        base.set("A", a1);
        final Settings other = new Settings(true);
        other.set("A", new Section(true));
        base.merge(other);

        Assert.assertTrue(base.contains("A"));
        Assert.assertEquals("1", base.get("A").get("X"));
    }
}