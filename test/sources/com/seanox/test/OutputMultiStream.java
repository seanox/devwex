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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Multiple output streams are combined into one output stream. Data written to
 * this output stream are multiplied to all subscribed output streams.
 * Subscribers can be flexibly added and removed at runtime. In addition, data
 * stream can also be captured partially.
 *
 * @author  Seanox Software Solutions
 * @version 1.2.0 20220910
 */
public class OutputMultiStream extends OutputStream {
    
    /** Set with subscribers of the stream */
    private final Set<OutputStream> subscribers;
    
    /** Timestamp of last writing */
    private long timing;
    
    /** Indicator for an idle mode in milliseconds (ca. 12.5 x OS time slice) */
    private static final int IDLE = 250;
    
    /** Constructor creates a new OutputMultiStream object. */
    public OutputMultiStream() {
        this((OutputStream[])null);
    }    

    /**
     * Constructor creates a new OutputMultiStream object.
     * @param subscribers output streams
     */
    public OutputMultiStream(final OutputStream... subscribers) {
        this.timing = System.currentTimeMillis();
        this.subscribers = new HashSet<>();
        if (subscribers != null)
            this.subscribe(subscribers);
    }
    
    /**
     * Adds one or more output streams.
     * @param subscribers output streams
     */
    public void subscribe(final OutputStream... subscribers) {
        if (subscribers == null)
            return;
        synchronized (OutputMultiStream.class) {
            this.subscribers.addAll(Arrays.asList(subscribers));
        }
    }

    /**
     * Removes one or more output streams.
     * @param subscribers output streams
     */
    public void unsubscribe(final OutputStream... subscribers) {
        if (subscribers == null)
            return;
        synchronized (OutputMultiStream.class) {
            Arrays.asList(subscribers).forEach(this.subscribers::remove);
        }
    }
    
    /**
     * Creates a capture stream to get the changes from now.
     * @return the created capture stream
     */
    public Capture capture() {
        Capture capture = new Capture();
        this.subscribe(capture);
        return capture;
    }

    /**
     * Waits for an idle stream.
     * Default timeout: 1 second
     * @throws InterruptedException 
     * @throws TimeoutException 
     */
    public void await()
            throws TimeoutException, InterruptedException {
        this.await(-1);
    }

    /**
     * Waits for an idle stream.
     * @param  timeout timeout in milliseconds
     * @throws TimeoutException 
     * @throws InterruptedException 
     */
    public void await(long timeout)
            throws TimeoutException, InterruptedException {
        if (timeout < 0)
            timeout = IDLE;
        final long timing = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() -timing > timeout)
                throw new TimeoutException();
            if (this.idle() > 25
                    && this.idle() > IDLE)
                return;
            Thread.sleep(25);
        }
    }

    @Override
    public void write(final int data)
            throws IOException {
        this.timing = System.currentTimeMillis();
        final OutputStream[] streams;
        synchronized (OutputMultiStream.class) {
            streams = this.subscribers.toArray(new OutputStream[0]);
        }
        for (final OutputStream outputStream : streams) {
            this.timing = System.currentTimeMillis();
            outputStream.write(data);
        }
    }
    
    @Override
    public void write(final byte[] data, final int offset, final int length)
            throws IOException {
        this.timing = System.currentTimeMillis();
        final OutputStream[] streams;
        synchronized (OutputMultiStream.class) {
            streams = this.subscribers.toArray(new OutputStream[0]);
        }
        for (final OutputStream outputStream : streams) {
            this.timing = System.currentTimeMillis();
            outputStream.write(data, offset, length);
            outputStream.flush();
        }
    }
    
    @Override
    public void flush()
            throws IOException {
        final OutputStream[] streams;
        synchronized (OutputMultiStream.class) {
            streams = this.subscribers.toArray(new OutputStream[0]);
        }
        for (final OutputStream outputStream : streams)
            outputStream.flush();
    }
    
    /**
     * Returns the idle time in milliseconds.
     * If the stream is unused, this is the time since creation.
     * @return the idle time in milliseconds
     */
    public long idle() {
        return System.currentTimeMillis() -this.timing;
    }
    
    @Override
    public void close()
            throws IOException {
        final OutputStream[] streams;
        synchronized (OutputMultiStream.class) {
            streams = this.subscribers.toArray(new OutputStream[0]);
        }
        for (final OutputStream outputStream : streams)
            outputStream.close();
    }
    
    /** Capture stream to get the changes from the stream. */
    public class Capture extends ByteArrayOutputStream {
        
        /** Timestamp of last writing */
        private long timing;
        
        /** Constructor, creates a new Capture object. */
        private Capture() {
        }
        
        @Override
        public void write(final int data) {
            this.timing = System.currentTimeMillis();
            super.write(data);
        }
        
        @Override
        public void write(final byte[] data, final int offset, final int length) {
            this.timing = System.currentTimeMillis();
            super.write(data, offset, length);
        }

        /**
         * Returns the idle time in milliseconds.
         * If the stream is unused, this is the time since creation.
         * @return the idle time in milliseconds
         */
        public long idle() {
            return System.currentTimeMillis() -this.timing;
        }

        /**
         * Wait until a line break can be detected.
         * Without a line break, the method will block.
         * @throws InterruptedException
         */        
        public void await()
                throws InterruptedException {
            try {this.await(-1);
            } catch (TimeoutException exception) {
            }
        }
        
        /**
         * Wait until a line break can be detected.
         * Optionally, a timeout can be specified if the methods should not
         * block endlessly.
         * @param  timeout
         * @throws TimeoutException
         *     In case when a timeout has been set that has been exceeded.
         * @throws InterruptedException
         */
        public void await(long timeout)
                throws TimeoutException, InterruptedException {
            if (timeout < 0)
                timeout = IDLE;
            final long timing = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() -timing > timeout)
                    throw new TimeoutException();
                if (OutputMultiStream.this.idle() > 25
                        && OutputMultiStream.this.idle() >= IDLE
                        && this.idle() > 25
                        && this.idle() > IDLE)
                    return;
                Thread.sleep(25);
            }
        }
        
        /**
         * Wait until the pattern can be detected.
         * Without the pattern, the method will block.
         * @param  pattern
         * @throws InterruptedException
         */          
        public void await(final String pattern)
                throws InterruptedException {
            try {this.await(pattern, -1);
            } catch (TimeoutException exception) {
            }
        }

        /**
         * Wait until a pattern can be detected.
         * Optionally, a timeout can be specified if the methods should not
         * block endlessly.
         * @param  pattern
         * @param  timeout
         * @throws TimeoutException
         *     In case when a timeout has been set that has been exceeded.
         * @throws InterruptedException
         */        
        public void await(final String pattern, long timeout)
                throws TimeoutException, InterruptedException {
            
            if (pattern == null
                    || pattern.trim().isEmpty())
                throw new IllegalArgumentException("Invalid pattern");
            if (timeout < 0)
                timeout = IDLE;

            final long timing = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() >= timing +timeout)
                    throw new TimeoutException();
                final String content = this.toString();
                if (content.matches(pattern))
                    break;
                Thread.sleep(25);
            }
        }
        
        /**
         * Returns the last line of the output stream. 
         * @return the last line of the output stream
         */
        public String tail() {
            final String content = this.toString().trim();
            final String[] lines = content.split("\\R");
            return lines[lines.length -1];
        }
        
        /**
         * Finds a line in the output log for a pattern.
         * @param  pattern
         * @return to the pattern determined line, otherwise {@code null}.
         */      
        public String fetch(final String pattern) {
            final String content = this.toString().trim();
            final Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(content);
            if (matcher.find()
                    && matcher.groupCount() > 0)
                return matcher.group(1);
            return null;        
        }

        @Override
        public void close()
                throws IOException {
            OutputMultiStream.this.unsubscribe(this);
            super.close();
        }
    }
    
    /** TimeoutException */
    public static class TimeoutException extends Exception {
        private static final long serialVersionUID = 4355449388335541238L;
    }
}