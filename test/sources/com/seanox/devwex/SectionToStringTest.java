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

/** Test cases for {@link com.seanox.devwex.Section}. */
public class SectionToStringTest extends AbstractTest {
    
    @Test
    public void testEncodingKey_1()
            throws Exception {
        final Section section = new Section();
        section.set(" 1\0\0 ", "xxx");
        section.set(" 2\r\n ", "xxx");        
        section.set(" 1\0\0a ", "xxx");
        section.set(" 2\r\nb ", "xxx");        
        section.set(" \0\0a ", "xxx");
        section.set(" \r\nb ", "xxx");    
        section.set(" a1\7\7b2 ", "xxx");
        section.set(" a1\7\7 ", "xxx");
        section.set(" \7\7b2 ", "xxx");
        section.set(" \00A7\00A7 ", "xxx");
        section.set(" a1\7\7b2 ", "xxx");
        Assert.assertEquals(MockUtils.readTestContent(), SectionTest.toString(section));
    }
    
    @Test
    public void testEncodingKey_2()
            throws Exception {
        final Section section = new Section();
        section.set(" 12345 ", "xxx");
        section.set(" 12[5] ", "xxx");
        section.set(" 1 [5] ", "xxx");
        section.set(" 12[34 ", "xxx");
        section.set(" 1234] ", "xxx");
        section.set(" 12=34 ", "xxx");
        section.set(" 12;34 ", "xxx");
        section.set(" 1 = 2 ", "xxx");
        section.set(" 1 + 2 ", "xxx"); 
        section.set(" = 2a ", "xxx");
        section.set(" + 2b ", "xxx"); 
        section.set(" 2c = ", "xxx");
        section.set(" 2d + ", "xxx");
        Assert.assertEquals(MockUtils.readTestContent(), SectionTest.toString(section));
    }
    
    @Test
    public void testEncodingKey_3()
            throws Exception {
        final Section section = new Section();
        section.set("a", "xx1");
        section.set(" b ", "xx2");        
        section.set("  c  ", "xx3");
        section.set("    a   ", "xx4");        
        section.set(" \0\0a ", "xx5");
        section.set(" \r\nb ", "xx6");    
        section.set(" a1\7\7b2 ", "xx7");
        section.set(" a1\7\7 ", "xx8");
        section.set(" \7\7b2 ", "xx9");
        section.set(" \00A7\00A7 ", "xxA");
        section.set(" a1\7\7b2 ", "xxB");
        Assert.assertEquals(MockUtils.readTestContent(), SectionTest.toString(section));
    }
    
    @Test
    public void testEncodingValue_1()
            throws Exception {
        final Section section = new Section();
        section.set("c1", "xxxx\n");
        section.set("c2", "xxxx\txxxx");
        section.set("c3", "xxxx\0xxxx");
        section.set("c4", "xxxx;xxxx");
        section.set("c5", "xxxx;\0xxxx");
        Assert.assertEquals(MockUtils.readTestContent(), SectionTest.toString(section));
    }
    
    @Test
    public void testEncodingValue_2()
            throws Exception {
        final Section section = new Section();
        section.set("d1", "+ xxxx");
        section.set("d2", "; xxxx");
        section.set("d3", "= xxxx");
        section.set("d4", "~ xxxx");
        section.set("d5", " 12345 ");
        Assert.assertEquals(MockUtils.readTestContent(), SectionTest.toString(section));
    }    

    @Test
    public void testIndenting_1()
            throws Exception {
        final Section section = new Section();
        section.set("x", "xxxx xxxx xxxx xxxx xxxx xxxx xxxx xxxx xxxx");
        section.set("xxxx", "    xxxx xxxx    ");
        section.set("xxxx xxxx", "    xxxx xxxx xxxx    ");
        section.set("xxxx xxxx xxxx xxxx", "   xxxx xxxx   ");
        section.set("xxxx xxxx xxxx", "   xxxx;xxxx   ");
        section.set("zzzz", null);
        section.set("zzzz zzzz", "    zzzz zzzz zzzz    ");
        Assert.assertEquals(MockUtils.readTestContent(), SectionTest.toString(section));
    }
}