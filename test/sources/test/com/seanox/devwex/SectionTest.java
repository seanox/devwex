/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * Devwex, Experimental Server Engine
 * Copyright (C) 2022 Seanox Software Solutions
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
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * TestSuite for {@link com.seanox.devwex.Section}.
 *
 * @author  Seanox Software Solutions
 * @version 5.5.0 20220827
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SectionTest_Contains.class,
    SectionTest_Parse.class,
    SectionTest_Set.class,
    SectionTest_Get.class,
    SectionTest_ToString.class
})
public class SectionTest extends AbstractTestSuite {
    
    @SuppressWarnings("unchecked")
    static String toString(final Section section) {
        
        final Map<String, String> entriesSrc;
        try {entriesSrc = (Map<String, String>)AbstractTestInternalAccess.getFieldValue(section, "entries");
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }        

        int indent = 0;

        final Map<String, String> entries = new LinkedHashMap<>();
        for (String key : entriesSrc.keySet()) {
            
            // Empty keys are ignored
            if (key.trim().isEmpty())
                continue;

            // the value is coded when:
            //   - starts with +=   
            //   - contains < 0x1F 
            String value = entriesSrc.get(key).trim();
            if (value.matches("^(?s)\\s*(?:(?:[\\+=])|(?:.*[\\x00-\\x1F])).*$"))
                value = String.format("0x%X", new BigInteger(1, value.getBytes()));
            
            // the key is coded when:
            //  - starts with +=   
            //  - contains ;=[] < 0x1F
            if (key.matches("^(?s)\\s*(?:(?:[\\+=])|(?:.*[\\x00-\\x1F\\[\\];=])).*$"))
                key = String.format("0x%X", new BigInteger(1, key.getBytes()));
            
            // the key is expanded when:
            //   - the value contains a semicolon (comment character)
            indent = Math.max(indent, key.length() +(value.contains(";") ? 4 : 0));
            
            entries.put(key, value);
        }
        
        // the indentation is created as a placeholder
        String space = " ";
        while (space.length() < indent)
            space = space.concat(space);
        space = space.substring(0, indent);

        final ByteArrayOutputStream buffer  = new ByteArrayOutputStream();
        final PrintStream writer = new PrintStream(buffer);
        for (String key : entries.keySet()) {
            String value = entries.get(key);
            if (!value.isEmpty()) {

                // the key will be normalized
                // the key is expanded when:
                //   - the value contains a semicolon (comment character)
                if (value.contains(";"))
                    key = key.concat(space.substring(key.length(), indent -4)).concat(" [+]");
                else key = key.concat(space.substring(key.length(), indent));

                writer.println(key.concat(" = ").concat(value));
                
            } else writer.println(key);
        }
        
        return buffer.toString();
    }
}