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
package com.seanox.test;

import java.util.Base64;

/**
 * Codec utilities for encoding and decoding.
 *
 * @author  Seanox Software Solutions
 * @version 1.0.1 20220828
 */
public class Codec {
    
    private Codec() {
    }    

    /**
     * Encodes a text in Base64.
     * @param  text text
     * @return the encoded text
     */
    public static String encodeBase64(final String text) {
        if (text == null)
            return null;
        return Codec.encodeBase64(text.getBytes());
    }

    /**
     * Encodes bytes in Base64.
     * @param  bytes bytes
     * @return the encoded bytes
     */
    public static String encodeBase64(final byte[] bytes) {
        if (bytes == null)
            return null;
        return new String(Base64.getEncoder().encode(bytes));
    }
    
    /**
     * Encodes a text hexadecimal.
     * @param  text text
     * @return the encoded text
     */    
    public static String encodeHex(final String text) {
        if (text == null)
            return null;
        return Codec.encodeHex(text.getBytes());
    }

    /**
     * Encodes bytes in hexadecimal.
     * @param  bytes bytes
     * @return the encoded bytes
     */    
    public static String encodeHex(final byte[] bytes) {
        if (bytes == null)
            return null;
        final StringBuilder builder = new StringBuilder(bytes.length *2);
        for (final byte digit : bytes)
            builder.append(String.format("%02x", Byte.valueOf(digit)));
        return builder.toString();
    }
}