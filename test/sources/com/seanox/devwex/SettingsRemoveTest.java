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

/** Test cases for {@link Settings#remove(String)}. */
public class SettingsRemoveTest extends AbstractTest {

    @Test
    public void testRemove_01() {

        final Settings settings = new Settings();
        final Section section = new Section();
        settings.set("A", section);

        final Section removed = settings.remove("A");
        Assert.assertSame(section, removed);
        Assert.assertFalse(settings.contains("A"));
    }

    @Test
    public void testRemove_02() {

        final Settings settings = new Settings();
        settings.set("A", new Section());

        final Section removed = settings.remove("B");
        Assert.assertNull(removed);
        Assert.assertEquals(1, settings.size());
    }

    @Test
    public void testRemove_03() {

        final Settings settings = new Settings();
        settings.set(" a ", new Section());

        final Section removed = settings.remove("A");
        Assert.assertNotNull(removed);
        Assert.assertEquals(0, settings.size());
    }

    @Test
    public void testRemove_04() {
        final Settings settings = new Settings();
        for (final String key : new String[]{null, "", " ", "\t", "\n"}) {
            Assert.assertNull(settings.remove(key));
            Assert.assertEquals(0, settings.size());
        }
    }
}