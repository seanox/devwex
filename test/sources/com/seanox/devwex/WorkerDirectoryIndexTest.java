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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.seanox.test.Pattern;

/** Test cases for {@link com.seanox.devwex.Worker}. */
public class WorkerDirectoryIndexTest extends AbstractStageRequestTest {
    
    /** 
     * Preparation of the runtime environment.
     * @throws Exception
     */
    @BeforeClass
    public static void initiate()
            throws Exception {
        final File rootStage = new File(AbstractStage.getRootStage(), "documents/empty").getCanonicalFile();
        Files.walkFileTree(rootStage.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes)
                    throws IOException {
                if (path.toFile().getName().contains("ignore"))
                        path.toFile().delete();
                else if (path.toFile().getName().contains("hidden"))
                        Files.setAttribute(path, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /** 
     * Test case for acceptance.
     * Without sorting, the file index of directories must not contain '?'.
     * @throws Exception
     */
    @Test
    public void testAcceptance_01()
            throws Exception {

        final String request = "GET / HTTP/1.0\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18181", request);

        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertTrue(header.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(header.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(header.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2") + "\r\n";
        Assert.assertTrue(body.contains("\r\nindex of: \r\n"));
        Assert.assertTrue(body.contains("\r\norder by: na\r\n"));
        Assert.assertFalse(body.contains("?"));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));  
    }
    
    /** 
     * Test case for acceptance.
     * With sorting, the file index of directories must not contain '?'.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_02()
            throws Exception {

        final String request = "GET /?d HTTP/1.0\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18181", request);

        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertTrue(header.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(header.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(header.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2") + "\r\n";
        Assert.assertTrue(body.contains("\r\nindex of: \r\n"));
        Assert.assertTrue(body.contains("\r\norder by: da\r\n"));
        Assert.assertFalse(body.contains("?"));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));  
    }
    
    /** 
     * Test case for acceptance.
     * The path from subdirectories must be created correctly.
     * @throws Exception
     */
    @Test
    public void testAcceptance_03()
            throws Exception {

        final String request = "GET /test_a/test/ HTTP/1.0\r\n"
                + "Host: vHb\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18181", request);

        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertTrue(header.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(header.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(header.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));

        final String body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2") + "\r\n";
        Assert.assertTrue(body.contains("\r\nindex of: /test_a/test\r\n"));
        Assert.assertTrue(body.contains("\r\norder by: na\r\n"));
        Assert.assertFalse(body.contains("?"));

        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));  
    }
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code [SERVER/VIRTUAL:INI] INDEX = ON}
     * Hidden files must be included in the index.
     * The flag {@code x} must be set for an empty directory.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_04()
            throws Exception {
        
        for (int loop = 1; loop <= 3; loop++) {
            final String request = "GET /empty/" + loop + "/ HTTP/1.0\r\n"
                    + "\r\n";
            final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18080", request);
            if (loop == 1) {
                Assert.assertTrue(response.contains("\r\norder by: xna\r\n"));
                Assert.assertFalse(response.contains("mime"));
            }
            if (loop == 2) {
                Assert.assertTrue(response.contains("\r\norder by: na\r\n"));
                Assert.assertTrue(response.contains("mime"));
            }
            if (loop == 3) {
                Assert.assertTrue(response.contains("\r\norder by: na\r\n"));
                Assert.assertTrue(response.contains("mime"));
            }
        }
    }
    
    /** 
     * Test case for acceptance.
     * Configuration: {@code [SERVER/VIRTUAL:INI] INDEX = ON [S]}
     * The index must not contain hidden files.
     * The flag {@code x} must be set for an empty directory.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_05()
            throws Exception {

        // Hidden files only work in Windows.
        if (!this.isWindows())
            return;
        
        for (int loop = 1; loop <= 3; loop++) {
            final String request = "GET /empty/" + loop + "/ HTTP/1.0\r\n"
                    + "\r\n";
            final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18182", request);
            if (loop == 1) {
                Assert.assertTrue(response.contains("\r\norder by: xna\r\n"));
                Assert.assertFalse(response.contains("mime"));
            }
            if (loop == 2) {
                Assert.assertTrue(response.contains("\r\norder by: xna\r\n"));
                Assert.assertFalse(response.contains("mime"));
            }
            if (loop == 3) {
                Assert.assertTrue(response.contains("\r\norder by: na\r\n"));
                Assert.assertTrue(response.contains("mime"));
            }
        }
    }    
}