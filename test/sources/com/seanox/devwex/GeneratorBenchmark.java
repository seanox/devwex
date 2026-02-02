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

import com.seanox.test.MockUtils;
import com.seanox.test.Timing;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/** Benchmark for {@link Generator}. */
public class GeneratorBenchmark extends AbstractTest {
    
    @Test
    public void testAcceptance_1()
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
    public void testAcceptance_2()
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
}