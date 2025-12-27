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
package com.seanox.devwex;

import org.junit.Assert;
import org.junit.Test;

import com.seanox.test.MockUtils;

/** Test cases for {@link com.seanox.devwex.Service#print(Object)}. */
public class ServicePrintTest extends AbstractStageTest {
    
    private static final int SLEEP = 250;
    
    /** 
     * Test case for acceptance.
     * Optional insertion of an indentation.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_1()
            throws Exception {

        String outputPattern = MockUtils.readTestContent(2);
        Service.print(MockUtils.readTestContent(1));
        Thread.sleep(SLEEP);
        String outputLog1 = AbstractStage.getOutputStreamCapture().toString().trim();
        outputLog1 = outputLog1.replaceAll("^[\\d-]+ [\\d:]+", "2000-01-01 14:00:00");
        Assert.assertEquals(outputPattern, outputLog1);
        
        Service.print("----------");
        Thread.sleep(SLEEP);

        outputPattern = MockUtils.readTestContent(3);
        Service.print(MockUtils.readTestContent(1), true);
        Thread.sleep(SLEEP);
        String outputLog3 = AbstractStage.getOutputStreamCapture().toString().trim();
        outputLog3 = outputLog3.replaceAll("(^|\\R)[\\d-]+ [\\d:]+", "$12000-01-01 14:00:00");
        Assert.assertEquals(outputPattern, outputLog3);
    } 
    
    /** 
     * Test case for acceptance.
     * Optional insertion of an indentation.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_2()
            throws Exception {
        
        String outputPattern = MockUtils.readTestContent(2);
        
        Service.print(MockUtils.readTestContent(1));
        Thread.sleep(SLEEP);
        String outputLog1 = AbstractStage.getOutputStreamCapture().toString().trim();
        outputLog1 = outputLog1.replaceAll("^[\\d-]+ [\\d:]+", "2000-01-01 14:00:00");
        Assert.assertEquals(outputPattern, outputLog1);
        
        Service.print("----------");
        Thread.sleep(SLEEP);
        String outputLog2 = AbstractStage.getOutputStreamCapture().toString().trim();
        Service.print(MockUtils.readTestContent(1), true);
        Thread.sleep(SLEEP);
        String outputLog3 = AbstractStage.getOutputStreamCapture().toString().trim();
        Assert.assertEquals(outputLog2 + System.lineSeparator() + outputPattern.substring(20), outputLog3);
    }  
    
    /** 
     * Test case for acceptance.
     * Optional insertion of an indentation.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_3()
            throws Exception {

        String outputPattern = MockUtils.readTestContent(2);
        
        Service.print(MockUtils.readTestContent(1));
        Thread.sleep(SLEEP);
        String outputLog1 = AbstractStage.getOutputStreamCapture().toString().trim();
        outputLog1 = outputLog1.replaceAll("^[\\d-]+ [\\d:]+", "2000-01-01 14:00:00");
        Assert.assertEquals(outputPattern, outputLog1);
        
        Service.print("----------");
        Thread.sleep(SLEEP);
        String outputLog2 = AbstractStage.getOutputStreamCapture().toString().trim();
        Service.print(MockUtils.readTestContent(1), true);
        Thread.sleep(SLEEP);
        String outputLog3 = AbstractStage.getOutputStreamCapture().toString().trim();
        Assert.assertEquals(outputLog2 + System.lineSeparator() + outputPattern.substring(20), outputLog3);
    } 
    
    /** 
     * Test case for acceptance.
     * Optional insertion of an indentation.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_4()
            throws Exception {
        
        String outputPattern = MockUtils.readTestContent(2);
        
        Service.print(MockUtils.readTestContent(1));
        Thread.sleep(SLEEP);
        String outputLog1 = AbstractStage.getOutputStreamCapture().toString().trim();
        outputLog1 = outputLog1.replaceAll("^[\\d-]+ [\\d:]+", "2000-01-01 14:00:00");
        Assert.assertEquals(outputPattern, outputLog1);
        
        Service.print("----------");
        Thread.sleep(SLEEP);
        String outputLog2 = AbstractStage.getOutputStreamCapture().toString().trim();
        Service.print(MockUtils.readTestContent(1), true);
        Thread.sleep(SLEEP);
        String outputLog3 = AbstractStage.getOutputStreamCapture().toString().trim();
        Assert.assertEquals(outputLog2 + System.lineSeparator() + outputPattern.substring(20), outputLog3);
    }

    /** 
     * Test case for acceptance.
     * In case of Throwable/Error/Exception, there is no compulsory indentation.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_5()
            throws Exception {

        Service.print(new Throwable("###1"));
        Thread.sleep(SLEEP);
        String outputLog = AbstractStage.getOutputStreamCapture().toString().trim();
        outputLog = outputLog.replaceAll("^[\\d-]+ [\\d:]+", "2000-01-01 14:00:00");
        Assert.assertTrue(outputLog.startsWith("2000-01-01 14:00:00 java.lang.Throwable: ###1"));
        Assert.assertFalse(outputLog.matches("^.*[\r\n]\\S.*$"));
    }
    
    /** 
     * Test case for acceptance.
     * In case of Throwable/Error/Exception, there is no compulsory indentation.
     * Optional insertion of an indentation.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_6()
            throws Exception {

        Service.print(new Throwable("###1", new Throwable("###2")));
        Thread.sleep(SLEEP);
        String outputLog = AbstractStage.getOutputStreamCapture().toString().trim();
        outputLog = outputLog.replaceAll("^[\\d-]+ [\\d:]+", "2000-01-01 14:00:00");
        Assert.assertTrue(outputLog.startsWith("2000-01-01 14:00:00 java.lang.Throwable: ###1"));
        Assert.assertTrue(outputLog.matches("(?si)^.*[\r\n]\\QCaused by: java.lang.Throwable: ###2\\E.*$"));
    }    

    /** 
     * Test case for acceptance.
     * Optional insertion of an indentation.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_7()
            throws Exception {

        Service.print(new Throwable("###1", new Throwable("###2", new Throwable("###3"))));
        Thread.sleep(SLEEP);
        String outputLog = AbstractStage.getOutputStreamCapture().toString().trim();
        outputLog = outputLog.replaceAll("^[\\d-]+ [\\d:]+", "2000-01-01 14:00:00");
        Assert.assertTrue(outputLog.startsWith("2000-01-01 14:00:00 java.lang.Throwable: ###1"));
        Assert.assertTrue(outputLog.matches("(?si)^.*[\r\n]\\QCaused by: java.lang.Throwable: ###2\\E.*$"));
        Assert.assertTrue(outputLog.matches("(?si)^.*[\r\n]\\QCaused by: java.lang.Throwable: ###3\\E.*$"));
    }
    
    /** 
     * Test case for acceptance.
     * Empty contents are not output.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_8()
            throws Exception {
        
        Service.print("----------");
        Thread.sleep(SLEEP);
        String outputLog1 = AbstractStage.getOutputStreamCapture().toString().trim();
        for (final String text : new String[] {"", " ", "  ", null}) {
            Service.print(text);
            Thread.sleep(SLEEP);
            String outputLog = AbstractStage.getOutputStreamCapture().toString().trim();
            Assert.assertEquals(outputLog1, outputLog);
        }

        Service.print("----------");
        Thread.sleep(SLEEP);
        String outputLog2 = AbstractStage.getOutputStreamCapture().toString().trim();
        for (final String text : new String[] {"", " ", "  ", null}) {
            Service.print(text, true);
            Thread.sleep(SLEEP);
            String outputLog = AbstractStage.getOutputStreamCapture().toString().trim();
            Assert.assertEquals(outputLog2, outputLog);
            outputLog2 = outputLog;
        }
    }    
    
    /** 
     * Test case for acceptance.
     * With option 'strict' line breaks are handled as white spaces and not
     * written out. Without option 'strict' line breaks will be written as a
     * single line break.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_9()
            throws Exception {
        
        String outputLog;
        String output;

        Service.print("-A--------");
        Thread.sleep(SLEEP);
        outputLog = AbstractStage.getOutputStreamCapture().toString().trim();
        for (final String text : new String[] {"\r", "\n", "\r\n", "\n\r", "\r\r", "\n\n", "\r\n\r\n", "\n\r\n\r"}) {
            Service.print(text);
            Thread.sleep(SLEEP);
        }
        Assert.assertEquals(1, outputLog.split("\\R").length);
        Assert.assertEquals(9, AbstractStage.getOutputStreamCapture().toString().trim().split("\\R").length);
        Assert.assertEquals(9, AbstractStage.getOutputStreamCapture().toString().split("\\R").length);
        
        Service.print("-B--------");
        outputLog = AbstractStage.getOutputStreamCapture().toString().trim();
        for (final String text : new String[] {"  \r   ", "  \n  ", "  \r\n  ", "  \n\r  ", "  \r\r  ", "  \n\n  ", "  \r\n\r\n  ", "  \n\r\n\r  "}) {
            Service.print(text);
            Thread.sleep(SLEEP);
        }
        Assert.assertEquals(10, outputLog.split("\\R").length);
        Assert.assertEquals(18, AbstractStage.getOutputStreamCapture().toString().trim().split("\\R").length);
        Assert.assertEquals(18, AbstractStage.getOutputStreamCapture().toString().split("\\R").length);

        Service.print("-C--------");
        outputLog = AbstractStage.getOutputStreamCapture().toString().trim();
        for (final String text : new String[] {"\r", "\n", "\r\n", "\n\r", "\r\r", "\n\n", "\r\n\r\n", "\n\r\n\r"}) {
            Service.print(text, true);
            Thread.sleep(SLEEP);
        }
        output = AbstractStage.getOutputStreamCapture().toString();
        Assert.assertEquals(19, outputLog.split("\\R").length);
        Assert.assertEquals(19, output.trim().split("\\R").length);
        Assert.assertEquals(19, output.split("\\R").length);
        Assert.assertEquals(28, output.replaceAll("\\R", " \r\n ") .split("\\R").length);
        
        Service.print("-D--------");
        outputLog = AbstractStage.getOutputStreamCapture().toString().trim();
        for (final String text : new String[] {"  \r   ", "  \n  ", "  \r\n  ", "  \n\r  ", "  \r\r  ", "  \n\n  ", "  \r\n\r\n  ", "  \n\r\n\r  "}) {
            Service.print(text, true);
            Thread.sleep(SLEEP);
        } 
        output = AbstractStage.getOutputStreamCapture().toString();
        Assert.assertEquals(28, outputLog.split("\\R").length);
        Assert.assertEquals(28, output.trim().split("\\R").length);
        Assert.assertEquals(28, output.split("\\R").length);
        Assert.assertEquals(37, output.replaceAll("\\R", " \r\n ") .split("\\R").length);
        
        Service.print("-E--------");
    } 
}