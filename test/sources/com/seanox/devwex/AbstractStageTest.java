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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/** Abstract class to implement a stage based test. */
abstract class AbstractStageTest {
    
    @BeforeClass
    public static void prepareStage()
            throws Exception {
        AbstractStage.prepareStage();
    }
    
    @AfterClass
    public static void cleanStage()
            throws Exception {
        AbstractStage.cleanStage();
    }
    
    @Before
    public void prepareTest()
            throws InterruptedException {
        AbstractStage.prepareTest();
    }
    
    @After
    public void cleanTest()
            throws InterruptedException {
        AbstractStage.cleanTest();
    }
}