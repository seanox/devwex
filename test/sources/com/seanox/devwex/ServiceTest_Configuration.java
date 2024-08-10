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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.Assert;
import org.junit.Test;

import com.seanox.test.HttpUtils;
import com.seanox.test.Pattern;

/**
 * Test cases for {@link com.seanox.devwex.Worker}.
 *
 * @author  Seanox Software Solutions
 * @version 5.5.0 20220907
 */
public class ServiceTest_Configuration extends AbstractStageTest {
    
    /** 
     * Test case for acceptance.
     * The configuration devwex.ini is missing.
     * The server must (re)start with the last configuration.
     * @throws Exception
     */
    @Test
    public void testAcceptance_01()
            throws Exception {
        
        final String rootStageProgram = AbstractStage.getRootStageProgram().toString();

        try {
            Files.move(Paths.get(rootStageProgram, "devwex.ini"), Paths.get(rootStageProgram, "devwex.ini_"), StandardCopyOption.REPLACE_EXISTING); 
            Service.restart();
            Thread.sleep(250);
            AbstractStage.await();

            final String request = "Get / HTTP/1.0\r\n"
                    + "\r\n";
            final String response = new String(HttpUtils.sendRequest("127.0.0.1:18080", request));
            Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
            Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));

        } finally {
        
            Files.move(Paths.get(rootStageProgram, "devwex.ini_"), Paths.get(rootStageProgram, "devwex.ini"), StandardCopyOption.REPLACE_EXISTING); 
            Service.restart();
            Thread.sleep(250);
        }
    }
}