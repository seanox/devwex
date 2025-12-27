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

import com.seanox.test.MockUtils;

/** Test cases for {@link com.seanox.devwex.Section#parse(String)}. */
public class SectionParseTest extends AbstractTest {
    
    @Test
    public void testAcceptance_1()
            throws Exception {
        System.setProperty("param-c", "p_c");
        final Section section = Section.parse(MockUtils.readTestContent(1));
        Assert.assertEquals(MockUtils.readTestContent(2), SectionTest.toString(section));
    }

    @Test
    public void testAcceptance_2()
            throws Exception {
        final Section section = Section.parse(MockUtils.readTestContent(1));
        Assert.assertEquals(MockUtils.readTestContent(2), SectionTest.toString(section));
    }
    
    @Test
    public void testAcceptance_3()
            throws Exception {
        final Section section = Section.parse(MockUtils.readTestContent(1));
        Assert.assertEquals(MockUtils.readTestContent(2), SectionTest.toString(section));
    }    
    
    @Test
    public void testAcceptance_4()
            throws Exception {
        final Section section = Section.parse(MockUtils.readTestContent(1));
        Assert.assertEquals(MockUtils.readTestContent(2), SectionTest.toString(section));
    } 
    
    @Test
    public void testAcceptance_5()
            throws Exception {
        final Section section = Section.parse(MockUtils.readTestContent(1));
        Assert.assertEquals(MockUtils.readTestContent(2), SectionTest.toString(section));
    }

    @Test
    public void testAcceptance_6()
            throws Exception {
        final Section section = Section.parse(MockUtils.readTestContent(1));
        Assert.assertEquals(MockUtils.readTestContent(2), SectionTest.toString(section));
    }

    @Test
    public void testAcceptance_7()
            throws Exception {
        final Section section = Section.parse(MockUtils.readTestContent(1));
        Assert.assertEquals(MockUtils.readTestContent(2), SectionTest.toString(section));
    }
    
    @Test
    public void testAcceptance_8()
            throws Exception {
        final Section section = Section.parse(MockUtils.readTestContent(1));
        Assert.assertEquals(MockUtils.readTestContent(2), SectionTest.toString(section));
    }    
    
    @Test
    public void testOverride_1()
            throws Exception {
        final Section section = Section.parse(MockUtils.readTestContent(1));
        Assert.assertEquals(MockUtils.readTestContent(2), SectionTest.toString(section));
    }

    @Test
    public void testDynamic_1()
            throws Exception {
        final Section section = Section.parse(MockUtils.readTestContent(1));
        Assert.assertEquals(MockUtils.readTestContent(2).toLowerCase(), SectionTest.toString(section).toLowerCase());
    }
}