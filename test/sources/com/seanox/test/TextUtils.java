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
package com.seanox.test;

import java.util.Arrays;

/**
 * Utilities for text and strings.
 *
 * @author  Seanox Software Solutions
 * @version 1.2.1 20220828
 */
public class TextUtils {
    
    private TextUtils() {
    }    

    /**
     * Decodes all escape sequences ({@code \b \t \n \f \r \" \' \\}), three
     * bytes of octal escape sequences ({@code \000-\377}) and four bytes
     * hexadecimal ({@code \u0000-\uFFFF}) after a backslash. The method works
     * tolerant and keeps incorrect sequences. If {@code null} is passed,
     * {@code null} is returned.
     * @param  string string to be decoded 
     * @return the decoded string
     */
    public static String unescape(String string) {

        if (string == null)
            return null;
        
        final byte[] codex = ("\"'\\bfnrt\"'\\\b\f\n\r\t").getBytes();
        final int length = string.length();
        final byte[] bytes = new byte[length *2];
        
        int count = 0;
        for (int loop = 0; loop < length; loop++) {
            int code = string.charAt(loop);
            if (code == '\\') {
                if (loop +1 < length) {
                    int index = Arrays.binarySearch(codex, (byte)string.charAt(loop +1));
                    if (index >= 0
                            && index < 8) {
                        code = codex[index +8];                    
                        loop += 1;
                    } else if (loop +5 < length
                            && string.charAt(loop +1) == 'u') {
                        loop += 5;
                        try {
                            index = Integer.parseInt(string.substring(loop -4, loop), 16);
                            if (index > 0xFF)
                                bytes[count++] = (byte)((index >>> 8) & 0xFF);
                            bytes[count++] = (byte)(index & 0xFF);
                            continue;
                        } catch (NumberFormatException exception) {
                            loop -= 5;
                        }
                    } else {
                        int cache = 0;
                        for (index = 0; index < 3 && loop +1 < length; index++) {
                            if (string.charAt(loop +1) < '0'
                                    || string.charAt(loop +1) > '7'
                                    || (cache << 3) +string.charAt(loop +1) - '0' > 0xFF)
                                break;
                            cache = (cache << 3) +string.charAt(loop +1) - '0';
                            code  = cache;
                            loop++;
                        }
                    }
                }
            }
            bytes[count++] = (byte)code;
        }
        
        return new String(Arrays.copyOfRange(bytes, 0, count));
    }

    /**
     * Encodes the control characters: BS, HT, LF, FF, CR, ', ", \ and all
     * characters outside the ASCII range 0x20-0x7F.
     * The escape uses:
     * <ul>
     *   <li>slash + ISO</li>
     *   <li>slash + three bytes octal (0x80-0xFF)</li>
     *   <li>slash + four bytes hexadecimal (0x100-0xFFFF)</li>
     * </ul>
     * If {@code null} is passed, {@code null} is returned.
     * @param  string string to be escaped
     * @return the escaped string
     */
    public static String escape(String string) {
        
        if (string == null)
            return null;   
        
        final byte[] codec = ("0123456789ABCDEF").getBytes();
        final byte[] codex = ("\b\t\n\f\r\"'\\btnfr\"'\\").getBytes();
        final int length = string.length();
        final byte[] cache = new byte[length *6];
        
        int count = 0;
        for (int loop = 0; loop < length; loop++) {
            final int code = string.charAt(loop);
            final int cursor = Arrays.binarySearch(codex, (byte)code);
            if (cursor >= 0
                    && cursor < 8) {
                cache[count++] = '\\';
                cache[count++] = codex[cursor +8];
            } else if (code > 0xFF) {
                cache[count++] = '\\';
                cache[count++] = 'u';
                cache[count++] = codec[(code >> 16) & 0xF];
                cache[count++] = codec[(code >>  8) & 0xF];
                cache[count++] = codec[(code >>  4) & 0xF];
                cache[count++] = codec[(code & 0xF)];                
            } else if (code < 0x20
                    || code > 0x7F) {
                cache[count++] = '\\';
                cache[count++] = (byte)(0x30 +((code >> 6) & 0x7));
                cache[count++] = (byte)(0x30 +((code >> 3) & 0x7));
                cache[count++] = (byte)(0x30 +(code & 0x7));
            } else cache[count++] = (byte)code;
        }
        return new String(Arrays.copyOfRange(cache, 0, count));          
    }
}