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
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Map;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/** TestSuite for {@link com.seanox.devwex.Initialize}. */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    InitializeContainsTest.class,
    InitializeGetTest.class,
    InitializeParseTest.class,
    InitializeSetTest.class,
    InitializeToStringTest.class
})
public class InitializeTest extends AbstractTestSuite {
    
    @SuppressWarnings("unchecked")
    static String toString(final Initialize initialize) {
        
        final Map<String, Section> entries;
        try {entries = (Map<String, Section>)AbstractTestInternalAccess.getFieldValue(initialize, "entries");
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        
        String shadow = "";
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final PrintStream writer = new PrintStream(buffer);
        for (String key : entries.keySet()) {
            
            // Empty keys are ignored
            if (key.trim().isEmpty())
                continue;
            
            final String section = SectionTest.toString(entries.get(key)).trim();
            
            // the key is coded when:
            //   - contains ;[] < 0x1F
            if (key.matches("^(?s)\\s*(?:(?:.*[\\x00-\\x1F\\[\\];])).*$"))
                key = String.format("0x%X", new BigInteger(1, key.getBytes()));
            
            if (!shadow.isEmpty())
                writer.println();
            writer.println(("[").concat(key).concat("]"));
            if (!section.isEmpty())
                writer.println(section.trim());
            
            shadow = section;
        }
        
        return buffer.toString();
    }
}