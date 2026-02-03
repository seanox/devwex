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

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/** Abstract class to implement a test. */
abstract class AbstractTest {
    
    @BeforeClass
    public static void prepare() {

        // Force UTF-8 as the default character set / encoding prior to Java 18,
        // as this will be the standard later on.
        System.setProperty("file.encoding", StandardCharsets.UTF_8.name());
        try {
            final Field field = Charset.class.getDeclaredField("defaultCharset");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception exception) {
        }
        final String defaultCharsetName = Charset.defaultCharset().name();
        Assert.assertEquals(defaultCharsetName, StandardCharsets.UTF_8.name());

        final String enforce = System.getProperty("java.version.enforce", "false").trim();
        if (enforce.matches("(?i)^(1|true|on)$")) {
            final String version = System.getProperty("java.version");
            if (!version.matches("^1\\.8\\..*$"))
                throw new RuntimeException("Java 8 is required");
        }
    }

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(final Description description) {
            System.out.printf("Starting %s::%s%n", description.getClassName(), description.getMethodName());
        }
    };
}
