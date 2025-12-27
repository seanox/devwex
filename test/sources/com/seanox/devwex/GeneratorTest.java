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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.seanox.test.MockUtils;
import com.seanox.test.Timing;

/** Test cases for {@link com.seanox.devwex.Generator}. */
public class GeneratorTest extends AbstractTest {
    
    @Test
    public void testAcceptance_1()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent(1).getBytes());
        Assert.assertEquals(MockUtils.readTestContent(2), new String(generator.extract()));
    }
    
    @Test
    public void testAcceptance_2()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent(1).getBytes());
        Assert.assertEquals(MockUtils.readTestContent(1), new String(generator.extract()));
    }
    
    @Test
    public void testAcceptance_3()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testAcceptance_0_0").getBytes());
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }
    
    @Test
    public void testAcceptance_4()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testAcceptance_0_0").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        String path = "";
        for (final String entry : ("/1/22/333/4444/55555").split("/")) {
            path = path.concat(entry);
            values.put("base", path);
            values.put("name", entry);
            generator.set("path", values);
        }
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }
    
    @Test
    public void testAcceptance_5()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testAcceptance_0_0").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int loop = 1; loop < 7; loop++) {
            final String charX = Character.toString((char)('A' -1 + loop));
            values.put("case", charX + "1");
            values.put("name", charX + "2");
            values.put("date", charX + "3");
            values.put("size", charX + "4");
            values.put("type", charX + "5");
            values.put("mime", charX + "6");
            buffer.write(generator.extract("file", values));
        }
        values.put("file", buffer.toByteArray());
        generator.set(values);
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }
    
    @Test
    public void testAcceptance_6()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testAcceptance_0_1").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("name", "A");
        values.put("date", "B");
        values.put("size", "C");
        values.put("type", "D");
        values.put("mime", "E");
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final Timing timing = Timing.create(true);
        for (long loop = 1; loop < 10000; loop++) {
            values.put("case", "X" + loop);
            buffer.write(generator.extract("file", values));
        }
        values.put("file", buffer.toByteArray());
        generator.set("file", values);
        generator.extract();
        timing.assertTimeIn(3000);
    }    
    
    @Test
    public void testAcceptance_7()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testAcceptance_0_1").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("name", "A");
        values.put("date", "B");
        values.put("size", "C");
        values.put("type", "D");
        values.put("mime", "E");
        final Timing timing = Timing.create(true);
        for (long loop = 1; loop < 2500; loop++) {
            values.put("case", "X" + loop);
            generator.set("file", values);
        }
        generator.extract();
        timing.assertTimeIn(3000);
    }
    
    @Test
    public void testAcceptance_8()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testAcceptance_0_1").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        String path = "";
        for (final String entry : ("/1/22/333/4444/55555").split("/")) {
            path = path.concat(entry);
            values.put("base", path);
            values.put("name", entry);
            generator.set("path", values);
        }
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }
    
    @Test
    public void testAcceptance_9()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testAcceptance_0_2").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        String path = "";
        for (final String entry : ("/1/22/333/4444/55555").split("/")) {
            path = path.concat(entry);
            values.put("base", path);
            values.put("name", entry);
            generator.set("path", values);
        }
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }

    @Test
    public void testAcceptance_A()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testAcceptance_0_2").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        String path = "";
        for (final String entry : ("1/22/333/4444/55555").split("/")) {
            path = path.concat(entry);
            values.put("base", path);
            values.put("name", entry);
            generator.set("path", values);
        }
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }
    
    @Test
    public void testAcceptance_B() {
        Assert.assertEquals("A\00\00\07\00\00B", new String(Generator.parse(("A#[0x0000070000]B").getBytes()).extract()));
    }
    
    @Test
    public void testAcceptance_C()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testAcceptance_0_0").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        for (int loop = 1; loop < 7; loop++) {
            final String charX = Character.toString((char)('A' -1 + loop));
            values.put("case", charX + "1");
            values.put("name", charX + "2");
            values.put("date", charX + "3");
            values.put("size", charX + "4");
            values.put("type", charX + "5");
            values.put("mime", charX + "6");
            generator.set("file", values);
        }
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }    
    
    @Test
    public void testAcceptance_D()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent(1).getBytes());
        final Hashtable<String, String> values = new Hashtable() {{
            put("a", new Hashtable() {{
                put("a1", "xa1");
                put("a2", "xa2");
                put("a3", "xa3");
                put("b", new Hashtable() {{
                    put("b1", "xb1");
                    put("b2", "xb2");
                    put("b3", "xb3");
                    put("c", new Hashtable() {{
                        put("c1", "xc1");
                        put("c2", "xc2");
                        put("c3", "xc3");
                    }});
                }});
            }});
        }};
        generator.set(values);
        Assert.assertEquals(MockUtils.readTestContent(2), new String(generator.extract()).replaceAll("\\s+", ""));
    }
    
    @Test
    public void testAcceptance_E()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent(1).getBytes());
        final Hashtable<String, Object> values = new Hashtable() {{
            put("row", new ArrayList() {{
                add(new Hashtable() {{
                    put("cell", new ArrayList() {{
                        add("A1");
                        add("A2");
                        add("A3");
                    }});
                }});
                add(new Hashtable() {{
                    put("cell", new ArrayList() {{
                        add("B1");
                        add("B2");
                        add("B3");
                    }});
                }});
                add(new Hashtable() {{
                    put("cell", new ArrayList() {{
                        add("C1");
                        add("C2");
                    }});
                }});
                add(new Hashtable() {{
                    put("cell", new ArrayList() {{
                        add("D1");
                    }});
                }});
                add(new Hashtable() {{
                    put("cell", new ArrayList() {{
                    }});
                }});
            }});
        }};
        generator.set("table", values);
        Assert.assertEquals(MockUtils.readTestContent(2), new String(generator.extract()));
    }
    
    @Test
    public void testAcceptance_F() {
        final String template = "#[0x5065746572]#[0x7c756e64]#[0x7c646572]#[0x7c576f6c66]";
        final Generator generator = Generator.parse(template.getBytes());
        Assert.assertEquals("Peter|und|der|Wolf", new String(generator.extract()));
    }

    @Test
    public void testAcceptance_G() {
        final String template = "#[0x5065746572]#[0x7C756E64]#[0x7C646572]#[0x7C576F6C66]";
        final Generator generator = Generator.parse(template.getBytes());
        Assert.assertEquals("Peter|und|der|Wolf", new String(generator.extract()));
    }
    
    @Test
    public void testAcceptance_H() {
        final String template = "#[0X5065746572]#[0X7C756E64]#[0X7C646572]#[0X7C576F6C66]";
        final Generator generator = Generator.parse(template.getBytes());
        Assert.assertEquals("Peter|und|der|Wolf", new String(generator.extract()));
    }    

    @Test
    public void testAcceptance_I() {
        final String template = "#[x]#[X]";
        final Generator generator = Generator.parse(template.getBytes());
        final Map<String, String> values = new HashMap<>();
        values.put("X", "1");
        values.put("x", "2");
        generator.set(values);
        Assert.assertEquals("11", new String(generator.extract()));
    }
    
    @Test
    public void testRecursion_1()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testRecursion_0_1").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("teST", "xx1");
        generator.set("path", values);
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }   
    
    @Test
    public void testRecursion_2()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testRecursion_0_1").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("teST", "xx1");
        generator.set("path", values);
        values.put("teST", "xx2");
        generator.set("path", values);
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }   
    
    @Test
    public void testRecursion_3()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testRecursion_0_1").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("teST", "xx1");
        generator.set("path", values);
        values.put("teST", "xx2");
        generator.set("path", values);
        values.put("teST", "xx3");
        generator.set("path", values);
        values.put("teST", "xx4");
        generator.set("path", values);
        values.put("teST", "xx5");
        generator.set("path", values);
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }    
    
    @Test
    public void testRecursion_4()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testRecursion_0_2").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("teST", "xx1");
        generator.set("path", values);
        values.put("teST", "xx2");
        generator.set("path", values);
        values.put("teST", "xx3");
        generator.set("path", values);
        values.put("teST", "xx4");
        generator.set("path", values);
        values.put("teST", "xx5");
        generator.set("path", values);
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }    

    @Test
    public void testRecursion_5()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testRecursion_0_3").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("a", values);
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    } 
    
    @Test
    public void testRecursion_6()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testRecursion_0_3").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("a", values);
        generator.set("b", values);
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    } 
    
    @Test
    public void testRecursion_7()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testRecursion_0_3").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("a", values);
        generator.set("b", values);
        generator.set("c", values);
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }
    
    @Test
    public void testRecursion_8()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testRecursion_0_3").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("a", values);
        generator.set("b", values);
        generator.set("c", values);
        generator.set("d", values);
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }     

    @Test
    public void testRecursion_9()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testRecursion_0_3").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("d", values);
        generator.set("c", values);
        generator.set("b", values);
        generator.set("a", values);
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    }
    
    @Test
    public void testRecursion_A()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent("testRecursion_0_3").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        values.put("a", values);
        values.put("b", values);
        values.put("c", values);
        values.put("d", values);
        generator.set(values);
        Assert.assertEquals(MockUtils.readTestContent(), new String(generator.extract()));
    } 

    @Test
    public void testRecursion_B()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent(1).getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("A", "xa");
        values.put("B", "xb");
        values.put("C", "xc");
        generator.set(values);
        Assert.assertEquals(MockUtils.readTestContent(2), new String(generator.extract()));
    } 
    
    @Test
    public void testNullable_1() {
        final Generator generator = Generator.parse(null);
        generator.extract(null);
        generator.extract("");
        generator.extract(null, null);
        generator.extract("", new Hashtable<>());
        generator.set(null);
        generator.set(new Hashtable<>());
        generator.set(null, null);
        generator.set("", new Hashtable<>());
    } 
    
    @Test
    public void testUnicode_1()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent(1).getBytes());
        Assert.assertEquals(MockUtils.readTestContent(2), new String(generator.extract()));
    }

    @Test
    public void testUnicode_2()
            throws Exception {
        final Generator generator = Generator.parse(MockUtils.readTestContent(1).getBytes());
        Assert.assertEquals(MockUtils.readTestContent(2), new String(generator.extract()));
    }
}