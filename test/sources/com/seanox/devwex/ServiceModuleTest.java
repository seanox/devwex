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
import java.util.Arrays;
import java.util.Hashtable;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/** Test cases for {@link com.seanox.devwex.Service}. */
public class ServiceModuleTest extends AbstractStageTest {
    
    /** 
     * Preparation of the runtime environment.
     * @throws Exception
     */
    @BeforeClass
    public static void initiate()
            throws Exception {
        final String rootStageProgram = AbstractStage.getRootStageProgram().toString();
        Files.copy(Paths.get(rootStageProgram, "devwex.ini"), Paths.get(rootStageProgram, "devwex.ini_"), StandardCopyOption.REPLACE_EXISTING); 
        Files.copy(Paths.get(rootStageProgram, "devwex.xapi"), Paths.get(rootStageProgram, "devwex.ini"), StandardCopyOption.REPLACE_EXISTING);
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
        Thread.sleep(250);
        AbstractStage.await();
    }
    
    /** 
     * Test case for acceptance.
     * Checks various variants of valid and invalid modules.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_01()
            throws Exception {
        
        Service.restart();
        Thread.sleep(250);
        AbstractStage.await();
        
        final String output = AbstractStage.getOutputStreamCapture().toString();
        for (int loop = 1; loop < 15; loop++) {
            String pattern = String.format("\\QSERVICE INITIATE MODULE module.Acceptance_%02d\\E", loop);
            Assert.assertTrue(pattern, output.matches("(?s).*" + pattern + ".*"));
            if (loop != 8)
                pattern = pattern + "[\r\n]+\\d{4}(-\\d{2}){2} \\d{2}(:\\d{2}){2} \\Qjava.lang.NoSuchMethodException: Invalid interface\\E";
            else pattern = pattern + "[\r\n]+\\d{4}(-\\d{2}){2} \\d{2}(:\\d{2}){2} \\Qjava.lang.Throwable: module.Acceptance_08\\E";
            if (Arrays.asList(5, 6, 7, 9, 10, 11, 12, 14, 15).contains(loop))
                Assert.assertFalse(pattern, output.matches("(?s).*" + pattern + ".*"));
            else Assert.assertTrue(pattern, output.matches("(?s).*" + pattern + ".*"));
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Object getModule(final String module)
            throws Exception {
        final Object service = AbstractTestInternalAccess.getFieldValue(Service.class, "service");
        final Object modules = AbstractTestInternalAccess.getFieldValue(service, "modules");
        for (final Object entry : ((Hashtable<Class<?>, Object>)modules).values())
            if (entry.getClass().getName().equals(module))
                return entry;
        return null;
    }
    
    private static String getModuleExpose(final String module)
            throws Exception {
        if (ServiceModuleTest.getModule(module) == null)
            return null;
        return (String)AbstractTestInternalAccess.invoke(ServiceModuleTest.getModule(module), "expose");
    }
    
    /** 
     * Test case for acceptance.
     * The automatic setting of section keys as parameters is no longer
     * necessary. Check the new behavior.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_02()
            throws Exception {
        
        Object module;
        
        Assert.assertNull(ServiceModuleTest.getModuleExpose("module.Acceptance_00"));
        Assert.assertNull(ServiceModuleTest.getModuleExpose("module.Acceptance_15"));
        Assert.assertEquals("123", ServiceModuleTest.getModuleExpose("module.Acceptance_16"));
        Assert.assertEquals("[123]", ServiceModuleTest.getModuleExpose("module.Acceptance_17"));
        Assert.assertEquals("[*]", ServiceModuleTest.getModuleExpose("module.Acceptance_18"));
        Assert.assertEquals("[*] [*]", ServiceModuleTest.getModuleExpose("module.Acceptance_19"));
        Assert.assertEquals("[*] 123 [*]", ServiceModuleTest.getModuleExpose("module.Acceptance_20"));

        AbstractStage.getOutputStreamCapture().reset();
        
        Assert.assertNull(ServiceModuleTest.getModule("module.Acceptance_21"));
        module = Service.load("module.Acceptance_21");
        Assert.assertNotNull(module);
        module = Service.load((Class<?>)module, null);
        Assert.assertNotNull(module);
        Assert.assertNotNull(ServiceModuleTest.getModule("module.Acceptance_21"));
        Assert.assertNull(ServiceModuleTest.getModuleExpose("module.Acceptance_21"));
        Assert.assertTrue(AbstractStage.getOutputStreamCapture().toString().contains("SERVICE INITIATE MODULE module.Acceptance_21"));
    }
}