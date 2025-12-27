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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/** Abstract class to implement a test. */
abstract class AbstractTest {
    
    @BeforeClass
    public static void prepare() {
        
        System.setProperty("file.encoding", StandardCharsets.ISO_8859_1.name());
        if (!Charset.defaultCharset().name().matches("(?i)^((Windows|CP)-?)1252|ISO-8859-1$"))
            throw new RuntimeException("Character encoding ISO-8859-1 required");

        final String version = System.getProperty("java.version");
        if (!version.matches("^1\\.8\\..*$"))
            throw new RuntimeException("Java 1.8.x is required");
    }

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(final Description description) {
            System.out.printf("Starting %s:%s%n", description.getClassName(), description.getMethodName());
        }
    };
}
