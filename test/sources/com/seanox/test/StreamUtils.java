/**
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Utilities for streams.
 *
 * @author  Seanox Software Solutions
 * @version 1.1.0 20220827
 */
public class StreamUtils {
    
    private StreamUtils() {
    }    

    /**
     * Reads all bytes from a data stream.
     * @param  input input stream
     * @return read bytes as array
     * @throws IOException
     *     In case of incorrect access to the data stream
     */
    public static byte[] read(final InputStream input)
            throws IOException {
        return StreamUtils.read(input, false);
    }
    
    /**
     * Reads all bytes from a data stream.
     * @param  input input stream
     * @param  smart reads until the data stream no longer supplies data.
     * @return read bytes as array
     * @throws IOException
     *     In case of incorrect access to the data stream
     */
    public static byte[] read(InputStream input, boolean smart)
            throws IOException {

        if (!(input instanceof BufferedInputStream))
            input = new BufferedInputStream(input);

        final byte[] bytes = new byte[65535];
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (int size; (size = input.read(bytes)) >= 0;) {
            result.write(bytes, 0, size);
            if (smart
                    && input.available() <= 0
                    && result.size() > 0)
                break;
        } 
        
        return result.toByteArray();
    }
    
    /**
     * Forwards the contents of a data stream.
     * The methods work like the {@link InputStream#read(byte[])}.
     * @param  input  data stream from
     * @param  output data stream to
     * @return the total number of read/forward bytes, or -1 if there is no
     *         more data because the end of the stream has been reached
     * @throws IOException
     *     If the first byte cannot be read for any reason other than the end
     *     of the stream, if the input stream has been closed, or if some other
     *     I/O error occurs
     */
    public static long forward(final InputStream input, final OutputStream output)
            throws IOException {
        return StreamUtils.forward(input, output, 0);
    }
    
    /**
     * Forwards the contents of a data stream.
     * The methods work like the {@link InputStream#read(byte[])}.
     * @param  input  data stream from
     * @param  output data stream to
     * @param  offset skips over and discards bytes of data from the input stream
     * @return the total number of read/forward bytes, or -1 if there is no
     *         more data because the end of the stream has been reached
     * @throws IOException
     *     If the first byte cannot be read for any reason other than the end
     *     of the stream, if the input stream has been closed, or if some other
     *     I/O error occurs
     */
    public static long forward(final InputStream input, final OutputStream output, final long offset)
            throws IOException {
        input.skip(offset);
        final byte[] bytes = new byte[65535];
        long volume = 0;
        for (int length; (length = input.read(bytes)) >= 0; volume += length)
            output.write(bytes, 0, length);
        return volume;
    }      
    
    /**
     * Reads the last bytes from a data stream.
     * @param  input  input stream
     * @param  length number of bytes at the end
     * @return read bytes as array
     * @throws IOException
     *     In case of incorrect access to the data stream
     */    
    public static byte[] tail(final InputStream input, final int length)
            throws IOException {
        return StreamUtils.tail(input, length, false);
    }
    
    /**
     * Reads the last bytes from a data stream.
     * @param  input  input stream
     * @param  length number of bytes at the end
     * @param  smart  reads until the data stream no longer supplies data
     * @return read bytes as array
     * @throws IOException
     *     In case of incorrect access to the data stream
     */       
    public static byte[] tail(InputStream input, int length, boolean smart)
            throws IOException {
        
        if (length < 0)
            throw new IllegalArgumentException("Invalid length");
        
        if (!(input instanceof BufferedInputStream))
            input = new BufferedInputStream(input);

        final byte[] bytes = new byte[65535];
        final ByteArrayOutputStream result = new ByteArrayOutputStream(0);
        for (int size; (size = input.read(bytes)) >= 0;) {
            result.write(bytes, 0, size);
            if (result.size() > length) {
                byte[] temp = result.toByteArray();
                temp = Arrays.copyOfRange(temp, temp.length -length, temp.length);
                result.reset();
                result.write(temp);
            }
            if (smart
                    && input.available() <= 0
                    && result.size() > 0)
                break;
        } 
        
        return result.toByteArray();
    }    
}