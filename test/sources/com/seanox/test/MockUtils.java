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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Utilities for mock data.
 *
 * @author  Seanox Software Solutions
 * @version 1.1.0 20220826
 */
public class MockUtils {
    
    private MockUtils() {
    }     
    
    /**
     * Creates a readable InputStream.
     * This starts with A, is filled with - and ends with E.
     *   <dir>e.g. {@code A-----E}</dir>
     * <ul>
     *   <li>          
     *     Is the space less than 2, then the InputStream contains only E.
     *   <li>  
     *     Is the space less than 1, then the InputStream is empty.
     * </ul>
     * The Data will be created on the fly and will not stored in the memory.
     * @param  size size
     * @return the created readable InputStream.
     */
    public static InputStream createInputStream(final long size) {
        return new InputStream() {
            
            private long counter;
            
            @Override
            public int available() {
                return (int)Math.max(Math.min(0, size -this.counter), Integer.MAX_VALUE);
            }

            @Override
            public int read() {
                try {
                    if (this.counter > size -1)
                        return -1;
                    else if (this.counter == size -1)
                        return 'E';
                    else if (this.counter > 0)
                        return '-';
                    else return 'A'; 
                } finally {
                    this.counter++;
                }
            }
            
            @Override
            public int read(final byte[] bytes, final int offset, final int length)
                    throws IOException {
                
                if (bytes == null)
                    throw new NullPointerException();
                else if (offset < 0 || length < 0 || length > bytes.length - offset)
                    throw new IndexOutOfBoundsException();
                else if (length == 0)
                    return 0;
                
                if (this.counter <= 0
                        || this.counter + offset + length >= size -1)
                    return super.read(bytes, offset, length);
                
                Arrays.fill(bytes, 0, length, (byte)'-');
                this.counter += offset +length;
                return length;
            }
        };
    }

    /**
     * Reads a text file based on the package and the file name of the calling
     * class and method. The filename of the text file is composed like this:
     * {@code <package>/<class> ... <method>.txt} 
     * @return the content of the resource file as string
     * @throws Exception
     *     If the resource file cannot be found in the ClassPath.
     */
    public static String readTestContent()
            throws Exception {
        final StackTraceElement stackTraceElement = Arrays.stream(new Throwable().getStackTrace())
                .filter(fetch -> !MockUtils.class.getName().equals(fetch.getClassName()))
                .findFirst().get();
        final String source = String.format("%s", stackTraceElement.getMethodName());
        return MockUtils.readTestContent(source);
    }

    /**
     * Reads a text file based on the package and the file name of the calling
     * class and method. The filename of the text file is composed like this:
     * {@code <package>/<class> ... <method>_<index>.txt} 
     * @param  index
     * @return the content of the resource file as string
     * @throws Exception
     *     If the resource file cannot be found in the ClassPath.
     */
    public static String readTestContent(final int index)
            throws Exception {
        final StackTraceElement stackTraceElement = Arrays.stream(new Throwable().getStackTrace())
                .filter(fetch -> !MockUtils.class.getName().equals(fetch.getClassName()))
                .findFirst().get();
        final String source = String.format("%s_%d", stackTraceElement.getMethodName(), index);
        return MockUtils.readTestContent(source);
    }

    /**
     * Reads a text file based on the package and the file name of the calling
     * class. The filename of the text file is composed like this:
     * {@code <package>/<class> ... <source>.txt} 
     * @param  source
     * @return the content of the resource file as string
     * @throws Exception
     *     If the resource file cannot be found in the ClassPath.
     */
    public static String readTestContent(final String source)
            throws Exception {
        final StackTraceElement stackTraceElement = Arrays.stream(new Throwable().getStackTrace())
                .filter(fetch -> !MockUtils.class.getName().equals(fetch.getClassName()))
                .findFirst().get();
        final String resourcePath = stackTraceElement.getClassName()
                .replace('.', '/') + String.format(" ... %s.txt", source);
        final ClassLoader classLoader = Class.forName(stackTraceElement.getClassName()).getClassLoader();
        return new String(StreamUtils.read(classLoader.getResourceAsStream(resourcePath)));
    }
}