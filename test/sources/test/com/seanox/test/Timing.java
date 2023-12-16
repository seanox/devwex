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

import org.junit.Assert;

/** 
 * Simple time measurement and testing.
 *
 * @author  Seanox Software Solutions
 * @version 1.0.1 20220909
 */
public class Timing {

    /** start time */
    private Long startTime;
    
    /** stop time */
    private Long stopTime;
    
    /** Constructor, creates a new Timing object. */
    private Timing() {
        this.reset();
    }
    
    /**
     * Creates a new Timing object.
     * The time measurement is not started automatically.
     * @return the created Timing object
     */
    public static Timing create() {
        return Timing.create(false);
    }
    
    /**
     * Creates a new Timing object.
     * With parameter 'start', the measurement will start automatically.
     * @param  start {@code true} starts with the creation
     * @return the created Timing object
     */        
    public static Timing create(final boolean start) {
        final Timing timing = new Timing();
        if (start)
            timing.start();
        return timing;
    }

    /** Starts the measurement, if it is not running yet. */
    public void start() {
        long delta = 0;
        if (this.stopTime != null)
            delta = this.stopTime -this.startTime;
        if (this.startTime == null)
            this.startTime = System.currentTimeMillis() -delta;
    }
    
    /** Stopps the measurement, if it is running. */
    public void stop() {
        if (this.startTime != null)
            this.stopTime = System.currentTimeMillis();
    }
    
    /** Resets the measurement. */
    public void reset() {
        this.startTime = null;
        this.stopTime  = null;
    }
    
    /** Restarts the measurement (reset + start). */
    public void restart() {
        this.reset();
        this.start();
    }
    
    /**
     * Gets the current measured time in milliseconds.
     * @return the current measured time in milliseconds
     */
    public long timeMillis() {
        if (this.startTime == null)
            return 0;
        return System.currentTimeMillis() -this.startTime;
    }
    
    /**
     * Checks whether the currently measured time is greater than or equal
     * to the specified millisecond.
     * @param milliseconds milliseconds
     */
    public void assertTimeOut(final int milliseconds) {
        if (this.timeMillis() < milliseconds)
            Assert.assertEquals("out of " + milliseconds + " ms", "out of " + this.timeMillis() + " ms");
    }

    /**
     * Checks whether the currently measured time is outside the specified time
     * frame in milliseconds.
     * @param millisecondsFrom milliseconds from
     * @param millisecondsTo   milliseconds To
     */
    public void assertTimeOut(final int millisecondsFrom, final int millisecondsTo) {
        final long time = this.timeMillis();
        if (time >= millisecondsFrom
                && time <= millisecondsTo)
            Assert.assertEquals("out of " + millisecondsFrom + " - " + millisecondsTo + " ms", "out of " + time + " ms");
    } 

    /**
     * Checks whether the currently measured time is less than or equal to
     * the specified millisecond.
     * @param milliseconds milliseconds
     */
    public void assertTimeIn(final int milliseconds) {
        if (this.timeMillis() > milliseconds)
            Assert.assertEquals("in " + milliseconds + " ms", "in " + this.timeMillis() + " ms");
    }   
    
    /**
     * Checks whether the currently measured time is within the specified
     * time frame in milliseconds.
     * @param millisecondsFrom milliseconds from
     * @param millisecondsTo   milliseconds To
     */
    public void assertTimeIn(final int millisecondsFrom, final int millisecondsTo) {
        final long time = this.timeMillis();
        if (time < millisecondsFrom
                || time > millisecondsTo)
            Assert.assertEquals("in " + millisecondsFrom + " - " + millisecondsTo + " ms", "in " + time + " ms");
    }   
}