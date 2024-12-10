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

import java.io.File;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.seanox.test.HttpUtils;

/** Test cases for {@link com.seanox.devwex.Service}. */
public class ServiceServerTest extends AbstractStageTest {
    
    /** 
     * Preparation of the runtime environment.
     * @throws Exception
     */
    @BeforeClass
    public static void initiate()
            throws Exception {
        final String rootStageProgram = AbstractStage.getRootStageProgram().toString();
        final long lastModified = new File(rootStageProgram, "devwex.ini").lastModified();
        Files.copy(Paths.get(rootStageProgram, "devwex.ini"), Paths.get(rootStageProgram, "devwex.ini_"), StandardCopyOption.REPLACE_EXISTING); 
        Files.copy(Paths.get(rootStageProgram, "devwex.sapi"), Paths.get(rootStageProgram, "devwex.ini"), StandardCopyOption.REPLACE_EXISTING);
        if (new File(rootStageProgram, "devwex.ini").lastModified() <= lastModified)
            new File(rootStageProgram, "devwex.ini").setLastModified(lastModified +1);
        Thread.sleep(250);
        AbstractStage.await();
    }
    
    /** 
     * Restoration of the runtime environment.
     * @throws Exception
     */
    @AfterClass
    public static void terminate()
            throws Exception {
        final String rootStageProgram = AbstractStage.getRootStageProgram().toString();
        Files.copy(Paths.get(rootStageProgram, "devwex.ini_"), Paths.get(rootStageProgram, "devwex.ini"), StandardCopyOption.REPLACE_EXISTING); 
        Files.delete(Paths.get(rootStageProgram, "devwex.ini_"));
        Thread.sleep(1000);
    } 

    /** 
     * Test case for acceptance.
     * A server can be configured and started by different instances.
     *     [REMOTE:INI]    18001
     *     [REMOTE:A:INI]  18002
     *     [REMOTE:B:INI]  18003
     *     [REMOTE::INI]   18004
     * @throws Exception
     */    
    @Test      
    public void testAcceptance_01()
            throws Exception {
        for (int port = 18001; port < 18004; port++) {
            final String response = new String(HttpUtils.sendRequest("127.0.0.1:" + port, "sTatuS\r"));
            Assert.assertNotNull(response);
            Assert.assertTrue(response, response.contains("\r\nSAPI: "));
            Assert.assertTrue(response, response.contains("\r\nTIME: "));
            Assert.assertTrue(response, response.contains("\r\nTIUP: "));            
        }
    }
    
    /** 
     * Test case for acceptance.
     * Tests when an implementation (default scope) is used in different instances.
     *     [COUNT:INI] com.seanox.devwex
     *     [COUNT:A:INI] com.seanox.devwex
     * @throws Exception
     */     
    @Test      
    public void testAcceptance_02()
            throws Exception {
        
        String response;
        response = new String(HttpUtils.sendRequest("127.0.0.1:" + 18201));
        Assert.assertEquals("1 com.seanox.devwex.Count$1", response);
        for (int loop = 2; loop < 10; loop++) {
            response = new String(HttpUtils.sendRequest("127.0.0.1:" + 18201));
            Assert.assertEquals(loop + " com.seanox.devwex.Count$1", response);
        }
        response = new String(HttpUtils.sendRequest("127.0.0.1:" + 18202));
        Assert.assertEquals("1 com.seanox.devwex.Count$1", response);
        for (int loop = 2; loop < 15; loop++) {
            response = new String(HttpUtils.sendRequest("127.0.0.1:" + 18202));
            Assert.assertEquals(loop + " com.seanox.devwex.Count$1", response);
        }        
        response = new String(HttpUtils.sendRequest("127.0.0.1:" + 18201));
        Assert.assertEquals("10 com.seanox.devwex.Count$1", response);
    }
    
    /** 
     * Test case for acceptance.
     * Tests when an implementation (external scope) is used in different instances.
     *     [COUNT:B1:INI] example
     *     [COUNT:B2:INI] example
     * @throws Exception
     */       
    @Test      
    public void testAcceptance_03()
            throws Exception {
        
        String response;
        response = new String(HttpUtils.sendRequest("127.0.0.1:" + 18203));
        Assert.assertEquals("1 server.Count$1", response);
        for (int loop = 2; loop < 10; loop++) {
            response = new String(HttpUtils.sendRequest("127.0.0.1:" + 18203));
            Assert.assertEquals(loop + " server.Count$1", response);
        }
        response = new String(HttpUtils.sendRequest("127.0.0.1:" + 18204));
        Assert.assertEquals("1 server.Count$1", response);
        for (int loop = 2; loop < 15; loop++) {
            response = new String(HttpUtils.sendRequest("127.0.0.1:" + 18204));
            Assert.assertEquals(loop + " server.Count$1", response);
        }        
        response = new String(HttpUtils.sendRequest("127.0.0.1:" + 18203));
        Assert.assertEquals("10 server.Count$1", response);
    }  
    
    /** 
     * Test case for acceptance.
     * Tests if a scope does not exist.
     *     [COUNT:C:INI] example-x
     * @throws Exception
     */           
    @Test(expected=ConnectException.class)
    public void testAcceptance_04()
            throws Exception {
        final String details = Service.details();
        Assert.assertFalse(details.contains(":18205"));
        HttpUtils.sendRequest("127.0.0.1:" + 18205);
    }  
    
    /** 
     * Test case for acceptance.
     * Tests if an error occurs in constructor of a server.
     * @throws Exception
     */        
    @Test(expected=ConnectException.class)
    public void testAcceptance_05()
            throws Exception {
        final String details = Service.details();
        Assert.assertFalse(details.contains(":18206"));
        HttpUtils.sendRequest("127.0.0.1:" + 18206);
    }    
    
    /** 
     * Test case for acceptance.
     * Tests if an error occurs in the run-method of the server-thread.
     * @throws Exception
     */           
    @Test(expected=SocketTimeoutException.class)
    public void testAcceptance_06()
            throws Exception {
        final String details = Service.details();
        Assert.assertTrue(details.contains(":18207"));
        HttpUtils.sendRequest("127.0.0.1:" + 18207);
    }
    
    /** 
     * Test case for acceptance.
     * Tests if an error occurs in the run-method of the accept.
     * @throws Exception
     */      
    @Test(expected=AssertionError.class)
    public void testAcceptance_07()
            throws Exception {
        final String details = Service.details();
        Assert.assertFalse(details.contains(":18208"));
        HttpUtils.sendRequest("127.0.0.1:" + 18208);
    }
    
    /** 
     * Test case for acceptance.
     * Various (walid and invalid) implementations of the SAPI are checked.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_08()
            throws Exception {

        Service.restart();
        Thread.sleep(250);
        AbstractStage.await();
        
        final String output = AbstractStage.getOutputStreamCapture().toString();
        
        //Server 11 has no logic, but the API is implemented correctly
        Assert.assertFalse(output.contains("Exception: server.Acceptance_11"));
        Assert.assertTrue(output.contains("SERVICE INITIATE ACCEPTANCE_11"));
        
        //Server 12 the destroy-method is not implemented correctly
        Assert.assertTrue(output.contains("java.lang.NoSuchMethodException: Invalid interface"));
        
        //Server 13 the expose-method has a different but compatible return type
        Assert.assertFalse(output.contains("Exception: server.Acceptance_13"));        
        Assert.assertTrue(output.contains("SERVICE INITIATE ACCEPTANCE_13"));
        
        //Server 14 the constructor is not implemented correctly
        Assert.assertTrue(output.contains("java.lang.NoSuchMethodException: Invalid interface"));        
        
        //Server 15 the constructor is not implemented correctly, looks compatible but is it not
        Assert.assertTrue(output.contains("java.lang.NoSuchMethodException: Invalid interface"));          
        
        //Server 16 the constructor is not implemented correctly
        Assert.assertTrue(output.contains("java.lang.NoSuchMethodException: Invalid interface"));  
        
        //Server 17 has not implemented java.lang.Runnable, but it's allowed
        Assert.assertTrue(output.matches("(?si)^.*\\QSERVICE INITIATE ACCEPTANCE_17\\E[\\r\\n]+[\\d\\- :]+SERVICE INITIATE ACCEPTANCE_18.*$"));
        
        //Server 18 has implemented java.lang.Thread, not nice but it is ok
        Assert.assertFalse(output.contains("Exception: server.Acceptance_18"));        
        Assert.assertTrue(output.contains("SERVICE INITIATE ACCEPTANCE_18"));
        
        //Server 19 has implemented java.lang.Thread, but  not the run-method, but this is default implemented in java.lang.Thread
        Assert.assertFalse(output.contains("Exception: server.Acceptance_19"));        
        Assert.assertTrue(output.contains("SERVICE INITIATE ACCEPTANCE_19"));   
        
        //Server 21 the implementation of the expose-method is optional, errors in the signature are tolerated
        Assert.assertFalse(output.contains("Exception: server.Acceptance_20"));        
        Assert.assertTrue(output.contains("SERVICE INITIATE ACCEPTANCE_20"));         

        //Server 21 the implementation of the expose-method is optional, errors in the signature are tolerated
        Assert.assertFalse(output.contains("Exception: server.Acceptance_21"));        
        Assert.assertTrue(output.contains("SERVICE INITIATE ACCEPTANCE_21"));         
        
        //Server 21 the implementation of the expose-method is optional, errors in the signature are tolerated
        //    optionally the destroy-method can have a return value/type, this is ignored
        Assert.assertFalse(output.contains("Exception: server.Acceptance_22"));        
        Assert.assertTrue(output.contains("SERVICE INITIATE ACCEPTANCE_22")); 
        
        //Server 31 the definition of the scope is a class and supported
        Assert.assertFalse(output.contains("Exception: server.Acceptance_30"));        
        Assert.assertTrue(output.contains("SERVICE INITIATE ACCEPTANCE_3X"));
        
        //Server 32 the definition of the scope is not clean, it must be a valid package
        Assert.assertTrue(output.contains("SERVICE INITIATE ACCEPTANCE_31"));
        Assert.assertTrue(output.contains("ClassNotFoundException: server.Acceptance_31x"));

        //Server 33 the definition of the scope is not clean, it must be a valid package
        Assert.assertTrue(output.contains("SERVICE INITIATE ACCEPTANCE_32"));
        Assert.assertTrue(output.contains("ClassNotFoundException: server."));
        
        //servers and virtuals must be detected correctly
        //even if the use of blanks in the section is unclean
        Assert.assertTrue(output.matches("(?is).*SERVICE\\s+INITIATE\\s+ACCEPTANCE.*"));
        Assert.assertFalse(output.matches("(?is).*SERVICE\\s+INITIATE\\s+VIRTUAL.*"));
        Assert.assertTrue(output.matches("(?is).*:182[0-9][0-9].*"));
        Assert.assertTrue(output.matches("(?is).*:1831[3-5].*"));
        Assert.assertFalse(output.matches("(?is).*:1831[0-26-9].*"));
        Assert.assertFalse(output.matches("(?is).*:1832[0-9].*"));        
    }
}