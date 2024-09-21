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

import com.seanox.test.StreamUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/** Test cases for {@link com.seanox.devwex.Service}. */
public class ServiceRemoteTest extends AbstractStageTest {

    private static final int SLEEP = 25;
    
    /** 
     * Preparation of the runtime environment.
     * @throws Exception
     */
    @BeforeClass
    public static void initiate()
            throws Exception {
        final String rootStageProgram = AbstractStage.getRootStageProgram().toString();
        Files.copy(Paths.get(rootStageProgram, "devwex.ini"), Paths.get(rootStageProgram, "devwex.ini_"), StandardCopyOption.REPLACE_EXISTING); 
        Files.copy(Paths.get(rootStageProgram, "devwex.sapi"), Paths.get(rootStageProgram, "devwex.ini"), StandardCopyOption.REPLACE_EXISTING);
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
     * Internally uses only the internal commands RESTART | STATUS | STOP if the
     * server instance is already running. Parameters for the remote connections
     * are ignored. 
     * @throws Exception
     */     
    @Test
    public void testAcceptance_01_internal()
            throws Exception {
        
        Service.main(new String[] {"status"});
        
        Thread.sleep(SLEEP);
        final String output = AbstractStage.getOutputStreamCapture().toString();
        Assert.assertFalse(output.contains("REMOTE ACCESS FAILED"));
        Assert.assertFalse(output.contains("Connection refused: connect"));
        Assert.assertFalse(output.contains("Network is unreachable: connect"));
        Assert.assertFalse(output.contains("unknown_host"));
        Assert.assertTrue(output.contains("SAPI: TCP 127.0.0.1"));
    } 
    
    /** 
     * Test case for acceptance.
     * Internally uses only the internal commands RESTART | STATUS | STOP if the
     * server instance is already running. Parameters for the remote connections
     * are ignored. 
     * @throws Exception
     */      
    @Test
    public void testAcceptance_02_internal()
            throws Exception {
        
        Service.main(new String[] {"status", "unknown_host"});
        
        Thread.sleep(SLEEP);
        final String output = AbstractStage.getOutputStreamCapture().toString();
        Assert.assertFalse(output.contains("REMOTE ACCESS FAILED"));
        Assert.assertFalse(output.contains("Connection refused: connect"));
        Assert.assertFalse(output.contains("Network is unreachable: connect"));
        Assert.assertFalse(output.contains("unknown_host"));
        Assert.assertTrue(output.contains("SAPI: TCP 127.0.0.1"));     
    } 
    
    /** 
     * Test case for acceptance.
     * Internally uses only the internal commands RESTART | STATUS | STOP if the
     * server instance is already running. Parameters for the remote connections
     * are ignored. 
     * @throws Exception
     */      
    @Test
    public void testAcceptance_03_internal()
            throws Exception {
        
        Service.main(new String[] {"status", "1234"});

        Thread.sleep(SLEEP);
        final String output = AbstractStage.getOutputStreamCapture().toString();
        Assert.assertFalse(output.contains("REMOTE ACCESS FAILED"));
        Assert.assertFalse(output.contains("Connection refused: connect"));
        Assert.assertFalse(output.contains("Network is unreachable: connect"));
        Assert.assertFalse(output.contains("unknown_host"));
        Assert.assertTrue(output.contains("SAPI: TCP 127.0.0.1"));
    }     
    
    /** 
     * Test case for acceptance.
     * Internally uses only the internal commands RESTART | STATUS | STOP if the
     * server instance is already running. Parameters for the remote connections
     * are ignored. 
     * @throws Exception
     */      
    @Test
    public void testAcceptance_04_internal()
            throws Exception {

        Service.main(new String[] {"status", "127.0.0.1:18000"});

        Thread.sleep(SLEEP);
        final String output = AbstractStage.getOutputStreamCapture().toString();
        Assert.assertFalse(output.contains("REMOTE ACCESS FAILED"));
        Assert.assertFalse(output.contains("Connection refused: connect"));
        Assert.assertFalse(output.contains("Network is unreachable: connect"));
        Assert.assertFalse(output.contains("unknown_host"));
        Assert.assertTrue(output.contains("SAPI: TCP 127.0.0.1"));
    } 
    
    /** 
     * Test case for acceptance.
     * Internally uses only the internal commands RESTART | STATUS | STOP if the
     * server instance is already running. Parameters for the remote connections
     * are ignored. 
     * @throws Exception
     */      
    @Test
    public void testAcceptance_05_internal()
            throws Exception {

        Service.main(new String[] {"status", "127.0.0.1:18001"});
        
        Thread.sleep(SLEEP);
        final String output = AbstractStage.getOutputStreamCapture().toString();
        Assert.assertFalse(output.contains("REMOTE ACCESS FAILED"));
        Assert.assertFalse(output.contains("Connection refused: connect"));
        Assert.assertFalse(output.contains("Network is unreachable: connect"));
        Assert.assertFalse(output.contains("unknown_host"));
        Assert.assertTrue(output.contains("SAPI: TCP 127.0.0.1"));
    }   
    
    private static String remoteCall(String[] options)
            throws IOException {
        if (options == null)
            options = new String[0];
        final String classPath = String.join(File.pathSeparator,
                new String[] {"./classes", "../program/classes"});
        String command = String.format("java -cp %s com.seanox.devwex.Service", classPath);
        for (String option : options)
            command += " " + option;
        final Process process = Runtime.getRuntime().exec(command);
        final InputStream errorStream = process.getErrorStream();
        if (Objects.nonNull(errorStream)) {
            final String errorOutput = new String(StreamUtils.read(errorStream)).trim();
            if (errorOutput.length() > 0)
                throw new IOException(errorOutput);
        }
        final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final StringBuilder output = new StringBuilder();
        for (String line; (line = reader.readLine()) != null;) {
            output.append(line);
            output.append(System.lineSeparator());
        }
        return output.toString();
    }
    
    /** 
     * Test case for acceptance.
     * Default connection must work.
     * But not here because the default is not used.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_01_external()
            throws Exception {
        Thread.sleep(SLEEP);
        final String output = ServiceRemoteTest.remoteCall(new String[] {"status"});
        if (!output.contains("SAPI: TCP 127.0.0.1:25000")) {
            Assert.assertTrue(output.contains("REMOTE ACCESS FAILED"));
            Assert.assertTrue(output.contains("Connection refused: connect"));
            Assert.assertFalse(output.contains("Network is unreachable: connect"));
            Assert.assertFalse(output.contains("unknown_host"));
            Assert.assertFalse(output.contains("SAPI"));
        }
    } 
    
    /** 
     * Test case for acceptance.
     * If an unknown host is used, an error must occur.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_02_external()
            throws Exception {
        Thread.sleep(SLEEP);
        final String output = ServiceRemoteTest.remoteCall(new String[] {"status", "unknown_host"});
        Assert.assertTrue(output.contains("REMOTE ACCESS FAILED"));
        Assert.assertTrue(output.contains("unknown_host"));
        Assert.assertFalse(output.contains("Network is unreachable: connect"));
        Assert.assertFalse(output.contains("Connection refused: connect"));
        Assert.assertFalse(output.contains("SAPI"));       
    } 
    
    /** 
     * Test case for acceptance.
     * If an invalid port is used, an error must occur.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_03_external()
            throws Exception {
        Thread.sleep(SLEEP);
        final String output = ServiceRemoteTest.remoteCall(new String[] {"status", "1234"});
        Assert.assertTrue(output.contains("REMOTE ACCESS FAILED"));
        Assert.assertTrue(output.contains("Network is unreachable: connect"));
        Assert.assertFalse(output.contains("Connection refused: connect"));
        Assert.assertFalse(output.contains("unknown_host"));
        Assert.assertFalse(output.contains("SAPI"));
    }     
    
    /** 
     * Test case for acceptance.
     * If an invalid port is used, an error must occur.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_04_external()
            throws Exception {
        Thread.sleep(SLEEP);
        final String output = ServiceRemoteTest.remoteCall(new String[] {"status", "127.0.0.1:18000"});
        Assert.assertTrue(output.contains("REMOTE ACCESS FAILED"));
        Assert.assertTrue(output.contains("Connection refused: connect"));
        Assert.assertFalse(output.contains("Network is unreachable: connect"));
        Assert.assertFalse(output.contains("unknown_host"));
        Assert.assertFalse(output.contains("SAPI"));
    } 
    
    /** 
     * Test case for acceptance.
     * If the correct connection is used, a status response must be returned.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_05_external()
            throws Exception {
        Thread.sleep(SLEEP);
        final String output = ServiceRemoteTest.remoteCall(new String[] {"status", "127.0.0.1:18001"});
        Assert.assertFalse(output.contains("REMOTE ACCESS FAILED"));
        Assert.assertFalse(output.contains("Connection refused: connect"));
        Assert.assertFalse(output.contains("Network is unreachable: connect"));
        Assert.assertFalse(output.contains("unknown_host"));
        Assert.assertTrue(output.contains("SAPI"));
    }   
}