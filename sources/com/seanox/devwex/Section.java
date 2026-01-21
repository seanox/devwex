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

import java.math.BigInteger;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

/**
 * Section is a dictionary of keys and values used by {@link Settings}, which
 * processes configuration data in INI format.<br>
 * <br>
 * The INI format used for configuration is a compatible. extension to the
 * classic format. It is also line-based and uses sections in which keys with
 * values are stored. Section and key names are case-insensitive. Declarations
 * with the same name cause sections and keys to be overwritten.<br>
 * <br>
 * Sections support multiple inheritance with the keyword {@code EXTENDS}. For
 * this purpose, after the keyword the names of the referenced sections are
 * specified from which the keys and values are to be applied. Thereby keys of
 * the same name of following sections overwrite already existing values.<br>
 * <br>
 * Assigning values to keys in a section is done with the equal sign. In
 * difference to the original format, the assignment can be continued in the
 * next line if the line starts with the plus sign.<br>
 * <br>
 * Values can be assigned final, variable and optional. The option {@code [?]}
 * at the end of a key is used to search for a key with the same name in the
 * system properties of the Java runtime environment. If none can be determined,
 * the optionally specified value is used. Without a value, such a key is not
 * specified and is ignored.<br>
 * <br>
 * Comments start with a semicolon, are optional, and can be used anywhere on a
 * line so that the following characters are not part of the section, key, or
 * value. The option {@code [+]} at the end of a key can be used to disable the
 * use of comments for that key and use the semicolon in the value.<br>
 * <br>
 * Sections, keys and values also support a hexadecimal notation, starting with
 * {@code 0x...} followed by the hexadecimal string, which can be used only
 * applied to the whole element.
 * 
 * <h3>Example</h3>
 * <pre>
 *   001 [SECTION] EXTENDS SECTION-A SECTION-B      ;comment
 *   002   PARAM-A                 = VALUE-1        ;comment
 *   003   PARAM-B             [+] = VALUE-2; VALUE-3
 *   004                           + VALUE-4; VALUE-5
 *   005   PARAM-C          [?][+] = VALUE-6; VALUE-7
 *   006   PARAM-E          [?]                     ;comment
 *   007
 *   008 [0x53454354494F4E2D41]
 *   009   PARAM-A                 = 0x574552542D31 ;comment
 *   010   0x504152414D2D42        = VALUE-2        ;comment 
 *   011   0x504152414D2D43    [+] = 0x574552542D33
 *   012   PARAM-D                 = 0x574552542D34
 *   013                           + 0x574552542D35
 *   014   PARAM-E          [?][+] = 0x574552542D363B20574552542D37
 *   015   0x504152414D2D45 [?]                     ;comment
 * </pre>
 * 
 * <h4>Line 1</h4>
 * The section with the name {@code SECTION} is defined, the keyword
 * {@code EXTENDS} refers to the derivation from the sections {@code SECTION-A}
 * and {@code SECTION-B}. Thus, {@code SECTION} is based on the keys and values
 * of the sections {@code SECTION-A} and {@code SECTION-B}. From the semicolon
 * onwards, the following characters are interpreted as comments.
 * 
 * <h4>Line 2</h4>
 * The value {@code VALUE-1} is assigned to the key {@code PARAM-A}. The
 * following characters are interpreted as comments from the semicolon onwards.
 * 
 * <h4>Line 3</h4>
 * The key {@code PARAM-B} is assigned {@code VALUE-2; VALUE-3} as a value, with
 * the option {@code [+]} at the end of the key disables the line comment and
 * uses all characters for the value assignment, but it is not possible to
 * specify a comment in this line.
 * 
 * <h4>Line 4</h4>
 * The value assignment of <i>line 3</i> is continued and the value {@code
 * VALUE-4; VALUE-5} is added to the existing value of the key {@code PARAM-B}.
 * The option {@code [+]} from <i>line 3</i> is also taken from <i>line 4</i>,
 * which also disables the line comment and uses all characters as value
 * assignment. It is not possible to enter a comment in this line. Further
 * options are not possible.
 * 
 * <h4>Line 5</h4>
 * The assignment for the key {@code PARAM-C} is dynamic, and the system
 * properties (VM arguments) of the Java runtime environment are searched for
 * key {@code PARAM-C} of the same name, case-insensitive. The key must be part
 * of the runtime environment or can be set as VM argument (property) at the
 * program start in the form {@code -Dkey=value}. If the system properties of
 * the Java runtime environment do not contain a corresponding key, {@code
 * VALUE-6; VALUE-7} is used as the value. By combining it with the option
 * {@code [+]} at the end of the key, the line comment is disabled and all
 * characters are used for the value assignment. It is not possible to enter a
 * comment in this line.
 * 
 * <h4>Line 6</h4>
 * The value assignment for the key {@code PARAM-E} is dynamic, and the system
 * properties (VM arguments) of the Java runtime environment are searched for
 * the key of the same name, case-insensitive. The key must be part of the
 * runtime environment or can be set as VM argument (property) at the program
 * start in the form {@code -Dkey=value}. If the system properties of the Java
 * runtime environment do not contain a corresponding key, this key is ignored
 * because no alternative value has been specified. Comments are supported in
 * this line.
 * 
 * <h4>Line 8 - 15</h4>
 * Like the examples from <i>lines 1 - 6</i>, the hexadecimal notation is used
 * for sections, keys and values.
 */
public class Section implements Cloneable {

    /** Map of keys */
    private LinkedHashMap entries;

    /** Option to activate smart mode */
    private final boolean smart;

    /** Constructor, creates Section. */
    public Section() {
        this(false);
    }

    /**
     * Constructor, creates Section.
     * @param smart activates smart mode
     */
    public Section(boolean smart) {
        this.entries = new LinkedHashMap();
        this.smart   = smart;
    }

    /**
     * Decodes hexadecimal values into a string if necessary.
     * @param  string String to decode
     * @return the decoded and trimmed string
     */
    private static String decode(String string) {
        if (string == null)
            string = "";
        string = string.trim();
        if (string.matches("^(?i)0x([0-9A-F]{2})+$"))
            return new String(new BigInteger(string.substring(2), 16).toByteArray()).trim();
        return string;
    }

    /**
     * Determines the contained keys and values from the string.
     * Parsing ignores invalid keys and values and returns the determined keys
     * and values as Section.
     * @param  text String to parse
     * @return the determined keys and values as Section
     */
    public static Section parse(String text) {
        return Section.parse(text, false);
    }

    /**
     * Determines the contained keys and values from the string.
     * Parsing ignores invalid keys and values and returns the determined
     * keys and values as Section.
     *
     * Optionally, smart behavior can be enabled, which changes the behavior of
     * some methods.
     *
     *     <dir>{@link #parse(String, boolean)}</dir>
     * Only keys that are not empty are applied.
     *
     *     <dir>{@link #get(String)}</dir>
     * Never returns {@code null} for valid keys. If an unknown key is
     * requested, it will be recreated.
     *
     *     <dir>{@link #set(String, String)}</dir>
     * If {@code null} is passed as the value of a key, the method behaves like
     * {@link #remove(String)} and deletes the key.
     *
     *     <dir>{@link #merge(Section)}</dir>
     * Only keys that do not have empty values are applied. If a key has
     * {@code null} as value, the method behaves like {@link #remove(String)}.
     *
     * @param  text  String to parse
     * @param  smart Activates smart mode
     * @return the determined sections as Section
     */
    public static Section parse(String text, boolean smart) {
        
        Section section = new Section(smart);

        if (text == null)
            return section;
        
        LinkedHashMap entries = new LinkedHashMap();
        StringBuffer  buffer  = null;

        int option  = 0;

        StringTokenizer tokenizer = new StringTokenizer(text, "\r\n");
        while (tokenizer.hasMoreTokens()) {
            
            // Next line is read
            String line = tokenizer.nextToken().trim();
            if (!line.startsWith("+")) {

                option = 0;
                if (line.matches("^[^#;=]+\\[\\s*\\+\\s*\\].*$"))
                    option |= 1;
                if (line.matches("^[^#;=]+\\[\\s*\\?\\s*\\].*$"))
                    option |= 2;
                
                // Comment part will be removed if necessary
                if ((option & 1) == 0)
                    line = line.replaceAll("[#;].*$", "").trim();

                buffer = null;

                String value;
                String label;

                // Key is determined, decoded if necessary and optimized
                label = line.replaceAll("^([^#;=]+?)?((?:\\s*\\[\\s*.?\\s*\\])+)?(?:\\s*=\\s*(.*))?\\s*$", "$1");
                label = Section.decode(label).toUpperCase();
                
                // Only valid keys are applied
                if (label.length() <= 0)
                    continue;
                
                if ((option & 2) != 0) {
                    
                    // Value is searched directly in system properties
                    value = System.getProperty(label);
                    
                    // System properties are searched for the key regardless of
                    // upper/lower case
                    Enumeration enumeration = Collections.enumeration(System.getProperties().keySet());
                    while (value == null && enumeration.hasMoreElements()) {
                        String entry = (String)enumeration.nextElement();
                        if (!label.equalsIgnoreCase(entry.trim()))
                            continue;
                        value = System.getProperty(entry, "").trim();
                        break;
                    }

                    // System properties are searched for the key regardless of
                    // upper/lower case
                    enumeration = Collections.enumeration(System.getenv().keySet());
                    while (value == null && enumeration.hasMoreElements()) {
                        String entry = (String)enumeration.nextElement();
                        if (!label.equalsIgnoreCase(entry.trim()))
                            continue;
                        value = System.getenv(entry);
                        value = value == null ? "" : value.trim();
                        break;
                    }
                    
                    if (value != null) {
                        entries.put(label, new StringBuffer(value));
                        continue;
                    }
                }
                
                // Value is determined, decoded if necessary and optimized
                value = line.trim();
                value = value.replaceAll("^([^#;=]+?)?((?:\\s*\\[\\s*.?\\s*\\])+)?(?:\\s*=\\s*(.*))?\\s*$", "$3");
                value = Section.decode(value);
                
                buffer = new StringBuffer(value);
                entries.put(label, buffer);
                
            } else if (buffer != null) {
                
                // Content is processed only with valid key

                // Comment part will be removed if necessary
                if ((option & 1) == 0)
                    line = line.replaceAll("[#;][^#;]*$", "").trim();

                line = line.substring(1).trim();
                if (line.length() > 0)
                    line = Section.decode(line);
                if (line.length() > 0)
                    buffer.append(" ").append(Section.decode(line));                
            }
        }
        
        Enumeration enumeration = Collections.enumeration(entries.keySet());
        while (enumeration.hasMoreElements()) {
            String entry = (String)enumeration.nextElement();
            String value = ((StringBuffer)entries.get(entry)).toString().trim();
            if (value.length() > 0
                    || !smart)
                section.entries.put(entry, value);
        }
        
        return section;
    }

    /**
     * Returns an enumeration of the keys of all sections.
     * @return the keys of all keys as enumeration
     */
    public synchronized Enumeration elements() {
        return Collections.enumeration(this.entries.keySet());
    }
    
    /**
     * Returns {@code true} if the key to a value is contained.
     * @param  key Name of the key
     * @return {@code true} if the key to a value is contained
     */
    public synchronized boolean contains(String key) {
        if (key != null)
            key = key.toUpperCase().trim();
        if (key == null
                || key.length() <= 0)
            return false;
        return this.entries.containsKey(key);
    }

    /**
     * Returns the value to a corresponding key. In the smart mode will always
     * return a non-null value in form of an empty string.
     * @param  key Name of the key
     * @return the value of the key, otherwise {@code null} or in smart mode an
     *     empty string
     */
    public synchronized String get(String key) {
        return this.get(key, null);
    }

    /**
     * Returns the value to a corresponding key. If the key is not contained,
     * an alternative value can be passed, which will then be returned.
     * In the smart mode will always return a non-null value in form of an
     * empty string.
     * @param  key       Name of the key
     * @param  alternate Alternative value, with unknown key
     * @return the value of the key, otherwise {@code null} or the alternative
     *     value or in smart mode an empty string
     */
    public synchronized String get(String key, String alternate) {

        String value;
        
        if (key != null)
            key = key.toUpperCase().trim();
        if (key == null
                || key.length() <= 0)
            value = null;
        else value = (String)this.entries.get(key);
        
        if (value == null)
            value = alternate;
        if (value == null
                && this.smart)
            value = "";
        return value;
    }

    /**
     * Sets the value of the corresponding key. Existing keys are replaced.
     * Empty keys and {@code null} are ignored and return {@code null}. In
     * smart mode keys with value {@code null} are removed. The behavior is the
     * same as for {@link #remove(String)}.
     * @param  key   Name of the key
     * @param  value Value
     * @return previously assigned value, otherwise {@code null}
     */
     public synchronized String set(String key, String value) {

         if (key == null)
             return null;
         key = key.toUpperCase().trim();
         if (key.length() <= 0)
             return null;

         value = value == null ? "" : value.trim();
         if (value.length() <= 0
                 && this.smart)
             return (String)this.entries.remove(key);
         return (String)this.entries.put(key, value);
    }

    /**
     * Removes the specified key from Section.
     * @param  key Name of the key to be removed
     * @return previously assigned value, otherwise {@code null}
     */
     public synchronized String remove(String key) {
         if (key != null)
             key = key.toUpperCase().trim();
         if (key == null
                 || key.length() <= 0)
             return null;
         return (String)this.entries.remove(key);
     }

    /**
     * Merges the passed keys and values. Existing keys are updated and new
     * keys are created. In smart mode, only keys that are not empty are
     * applied. If a value is {@code null}, the method behaves like
     * {@link #remove(String)}.
     * @param  section Key and values to be applied
     * @return the current instance with the merged keys and values
     */
     public synchronized Section merge(Section section) {
         
         if (section == null)
             return this;
         
         // Keys and values are combined or, if necessary, newly created
         Enumeration enumeration = Collections.enumeration(section.entries.keySet());
         while (enumeration.hasMoreElements()) {
             String entry = (String)enumeration.nextElement();
             String value = section.get(entry);
             if (value.length() > 0
                     || !this.smart)             
                 this.set(entry, value);
         }
         
         return this;
    }

    /**
     * Returns the number of keys.
     * @return number of keys
     */
    public synchronized int size() {
        return this.entries.size();
    }

    /** Resets Section completely and discards all keys. */
    public synchronized void clear() {
        this.entries.clear();
    }

    @Override
    public synchronized Object clone() {
        Section section = new Section(this.smart);
        section.entries = (LinkedHashMap)this.entries.clone();
        return section;
    }
}