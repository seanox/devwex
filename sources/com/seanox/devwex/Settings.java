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
 * Settings, processes configuration data in INI format and provides it as
 * {@link Settings} and {@link Section}.<br>
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
public class Settings implements Cloneable {

    /** Map of sections */
    private final LinkedHashMap entries;
    
    /** Option to activate smart mode */
    private final boolean smart;

    /** Constructor, creates Settings. */
    public Settings() {
        this(false);
    }
    
    /** 
     * Constructor, creates Settings.
     * Optionally, smart behavior can be enabled, which changes the behavior of
     * some methods.
     * @param smart activates smart mode
     */
    public Settings(boolean smart) {
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
        string = string.toUpperCase().trim();
        if (string.matches("^(?i)0x([0-9a-f]{2})+$"))
            return new String(new BigInteger(string.substring(2), 16).toByteArray()).toUpperCase().trim();
        return string;
    }

    /**
     * Determines the contained sections from the string.
     * Parsing ignores invalid sections, keys and values and returns the
     * determined sections as Settings.
     * @param  text String to parse
     * @return the determined sections as Settings
     */
    public static Settings parse(String text) {
        return Settings.parse(text, false);
    }
    
    /**
     * Determines the contained sections from the string.
     * Parsing ignores invalid sections, keys and values and returns the
     * determined sections as Settings.
     * 
     * Optionally, smart behavior can be enabled, which changes the behavior of
     * some methods.
     *     
     *     <dir>{@link #parse(String, boolean)}</dir>
     * Only sections that are not empty are applied.
     *     
     *     <dir>{@link #get(String)}</dir>
     * Never returns {@code null} for valid keys. If an unknown section is
     * requested, it will be recreated.
     *     
     *     <dir>{@link #set(String, Section)}</dir>
     * If {@code null} is passed as the value of a section key, the method
     * behaves like {@link #remove(String)} and deletes the section.    
     * 
     *     <dir>{@link #merge(Settings)}</dir>
     * Only sections that are not empty are applied. If a section contain value
     * {@code null}, the method behaves like {@link #remove(String)}.
     * 
     * @param  text  String to parse
     * @param  smart Activates smart mode
     * @return the determined sections as Settings
     */
    public static Settings parse(String text, boolean smart) {

        Settings settings = new Settings(smart);

        if (text == null)
            return settings;

        LinkedHashMap entries = new LinkedHashMap();
        StringBuffer  buffer  = null;

        StringTokenizer tokenizer = new StringTokenizer(text, "\r\n");
        while (tokenizer.hasMoreTokens()) {

            String line = ((String)tokenizer.nextElement()).trim();
            if (line.startsWith("[")) {
                
                buffer = new StringBuffer();
                
                // The line is processed as follows:
                //   - the unique/valid name of the section is determined
                //   - the comment part is removed
                //   - inadmissible subsequent sections are removed
                //   - if necessary existing derivations are loaded
                String[] strings = line.replaceAll("^(?i)(?:\\[\\s*([^\\[\\]\\;]+)\\s*\\]\\s*(?:extends\\s+([^\\[\\]\\;]+))*)*.*$", "$1 \00 $2").split("\00");
                
                // The section will be decoded and optimized if necessary but
                // only valid sections will be loaded
                String section = Settings.decode(strings[0]);
                if (section.length() <= 0)
                    continue;
                entries.put(section, buffer);
                
                // Any existing derivations are registered and loaded
                strings = strings[1].split("\\s+");
                for (int index = 0; index < strings.length; index++) {
                    section = Settings.decode(strings[index]);
                    if (entries.containsKey(section))
                        buffer.append("\r\n").append(entries.get(section));
                }
                
            } else if (buffer != null) {

                // Content is accepted only for valid section
                buffer.append("\r\n").append(line);
            }
        }
        
        Enumeration enumeration = Collections.enumeration(entries.keySet());
        while (enumeration.hasMoreElements()) {
            String section = (String)enumeration.nextElement();
            String value   = ((StringBuffer)entries.get(section)).toString().trim();
            if (value.length() > 0
                    || !smart)
                settings.entries.put(section, Section.parse(value, smart));
        }

        return settings;
    }

    /**
     * Returns an enumeration of the keys of all sections.
     * @return the keys of all sections as enumeration
     */
    public synchronized Enumeration elements() {
        return Collections.enumeration(this.entries.keySet());
    }

    /**
     * Returns {@code true} if the key to a section is contained.
     * @param  key Section name
     * @return {@code true} if the key to a section is contained
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
     * Returns the section corresponding to the key. If the key is not contained
     * or invalid, {@code null} is returned. In smart mode, never returns
     * {@code null} for valid keys. If an unknown section is requested, it will
     * be recreated.
     * @param  key Section name
     * @return the determined section, otherwise {@code null} 
     */
    public synchronized Section get(String key) {

        if (key != null)
            key = key.toUpperCase().trim();
        if (key == null
                || key.length() <= 0)
            return null;
        
        Section section = (Section)this.entries.get(key);
        if (section == null
                && this.smart) {
            section = new Section(true);
            this.set(key, section);
        }
        return section;
    }

    /**
     * Sets the corresponding section with the specified content. Existing
     * sections are replaced. Empty keys and {@code null} are ignored and return
     * {@code null}. In smart mode sections are removed if {@code null} is
     * passed as value. The behavior is the same as for {@link #remove(String)}. 
     * @param  key     Section name
     * @param  section Section
     * @return previously assigned section, otherwise {@code null}
     */
    public synchronized Section set(String key, Section section) {

        if (key == null)
            return null;
        key = key.toUpperCase().trim();
        if (key.length() <= 0)
            return null;
        
        if (section == null
                && !this.smart)
            return (Section)this.entries.remove(key);
        if (section == null)
            section = new Section(this.smart);
        return (Section)this.entries.put(key, section);
    }

    /**
     * Removes the specified section from Settings.
     * @param  key Name of the section to be removed
     * @return previously assigned section, otherwise {@code null}
     */
    public synchronized Section remove(String key) {
        if (key != null)
            key = key.toUpperCase().trim();
        if (key == null
                || key.length() <= 0)
            return null;
        return (Section)this.entries.remove(key);
    }

    /**
     * Merges the passed sections. Existing sections are updated and new
     * sections are created. In smart mode, only sections that are not empty are
     * applied. If a section contain value {@code null}, the method behaves like
     * {@link #remove(String)}.
     * @param  settings Sections to be applied
     * @return the current instance with the merged sections
     */
    public synchronized Settings merge(Settings settings) {
        
        if (settings == null)
            return this;

        // Sections are combined or, if necessary, newly created
        Enumeration enumeration = Collections.enumeration(this.entries.keySet());
        while (enumeration.hasMoreElements()) {
            String entry = (String)enumeration.nextElement();
            Section section = settings.get(entry);
            this.set(entry, section.merge(this.get(entry)));
        }
        
        return this;
    }

    /**
     * Returns the number of sections.
     * @return number of sections
     */
    public synchronized int size() {
        return this.entries.size();
    }

    /** Resets Settings completely and discards all sections. */
    public synchronized void clear() {
        this.entries.clear();
    }

    @Override
    public synchronized Object clone() {
        Settings settings = new Settings(this.smart);
        Enumeration enumeration = Collections.enumeration(this.entries.keySet());
        while (enumeration.hasMoreElements()) {
            String entry = (String)enumeration.nextElement();
            settings.entries.put(entry, ((Section)this.entries.get(entry)).clone());
        }
        return settings;
    }
}