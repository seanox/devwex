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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.seanox.test.HttpUtils.Keystore;
import com.seanox.test.OutputMultiStream;

/** Implementation of the test stage. */
abstract class AbstractStage {

    /** path of {@code ./stage} */
    private static final String PATH_STAGE = "./stage";

    /** path of {@code ./resources} */
    private static final String PATH_RESOURCES = "./resources";

    /** path of {@code ./stage/program} */
    private static final String PATH_STAGE_PROGRAM = "./stage/program";

    /** path of {@code ./stage/program/devwex.ini} */
    private static final String PATH_STAGE_PROGRAM_CONFIGURATION = "./stage/program/devwex.ini";

    /** path of {@code ./stage/access.log} */
    private static final String PATH_STAGE_ACCESS_LOG = "./stage/access.log";

    /** path of {@code ./stage/error.log} */
    private static final String PATH_STAGE_ERROR_LOG = "./stage/error.log";

    /** path of {@code ./stage/output.log} */
    private static final String PATH_STAGE_OUTPUT_LOG = "./stage/output.log";

    /** path of {@code ./stage/certificates} */
    private static final String PATH_STAGE_CERTIFICATES = "./stage/certificates";

    /** path of {@code ./stage/libraries} */
    private static final String PATH_STAGE_LIBRARIES = "./stage/libraries";

    /** keystore for SSL connections */
    private static Keystore keystore;

    /** Reference to the original System.out instance */
    private static final PrintStream systemOut = System.out;

    /** internal multi-stream for system output */
    private static OutputMultiStream outputStream;

    /** capture for the output stream, will be reset before each test */
    private static OutputMultiStream.Capture outputStreamCapture;

    /** Reference to the original System.err instance */
    private static final PrintStream systemErr = System.err;

    /** internal multi-stream for system error output */
    private static OutputMultiStream errorStream ;

    /** capture for the error stream, will be reset before each test */
    private static OutputMultiStream.Capture errorStreamCapture;

    /** internal multi-stream for the access-log */
    private static OutputMultiStream accessStream;

    /** internal multi-stream for the continuation access-log reader */
    private static Thread accessStreamSynchronize;

    /** capture for the access stream, will be reset before each test */
    private static OutputMultiStream.Capture accessStreamCapture;

    private static int lock;

    /**
     * Returns the root stage as file.
     * @return the root stage as file
     * @throws IOException
     */
    static File getRoot()
            throws IOException {
        return new File(".").getCanonicalFile();
    }

    /**
     * Returns the root stage as file.
     * @return the root stage as file
     * @throws IOException
     */
    static File getRootStage()
            throws IOException {
        return new File(AbstractStage.getRoot(), PATH_STAGE).getCanonicalFile();
    }
    
    /**
     * Returns the root stage program as file.
     * @return the root stage program as file
     * @throws IOException
     */
    static File getRootStageProgram()
            throws IOException {
        return new File(AbstractStage.getRoot(), PATH_STAGE_PROGRAM);
    }
    
    /**
     * Returns the root stage configuration as file.
     * @return the root stage configuration as file
     * @throws IOException
     */
    static File getRootStageProgramConfiguration()
            throws IOException {
        return new File(AbstractStage.getRoot(), PATH_STAGE_PROGRAM_CONFIGURATION);
    }

    /**
     * Returns the root stage output log as file.
     * @return the root stage output log as file
     * @throws IOException
     */
    static File getRootStageOutputLog()
            throws IOException {
        return new File(AbstractStage.getRoot(), PATH_STAGE_OUTPUT_LOG);
    }

    /**
     * Returns the root stage error log as file.
     * @return the root stage error log as file
     * @throws IOException
     */
    static File getRootStageErrorLog()
            throws IOException {
        return new File(AbstractStage.getRoot(), PATH_STAGE_ERROR_LOG);
    }

    /**
     * Returns the root stage access log as file.
     * @return the root stage access log as file
     * @throws IOException
     */
    static File getRootStageAccessLog()
            throws IOException {
        return new File(AbstractStage.getRoot(), PATH_STAGE_ACCESS_LOG);
    }

    /**
     * Returns the root stage certificates directory as file.
     * @return the root stage certificates directory as file
     * @throws IOException
     */
    static File getRootStageCertificates()
            throws IOException {
        return new File(AbstractStage.getRoot(), PATH_STAGE_CERTIFICATES);
    }

    /**
     * Returns the default keystore for SSL connections.
     * @return the default keystore for SSL connections
     */
    static Keystore getKeystore() {
        return AbstractStage.keystore;
    }

    /**
     * Returns the capture from the output log.
     * The capture is limited to the output of the current test.
     * @return capture from the output log
     */
    static OutputMultiStream.Capture getOutputStreamCapture() {
        return AbstractStage.outputStreamCapture;
    }

    /**
     * Returns the capture from the error log.
     * The capture is limited to the output of the current test.
     * @return capture from the error log
     */
    static OutputMultiStream.Capture getErrorStreamCapture() {
        return AbstractStage.errorStreamCapture;
    }

    /**
     * Returns the capture from the access log.
     * The capture is limited to the output of the current test.
     * @return capture from the access log
     */
    public static OutputMultiStream.Capture getAccessStreamCapture() {
        return AbstractStage.accessStreamCapture;
    }
    
    /**
     * Waits until the stage data streams are idle.
     * @throws InterruptedException
     */
    public static void await()
            throws InterruptedException {

        if (AbstractStage.outputStream == null
                && AbstractStage.errorStream == null
                && AbstractStage.accessStream == null)
            return;
        
        // Indicator for an idle mode in milliseconds (ca. 12.5 x OS time slice)
        final long idle = 250;
        while (true) {
            Thread.sleep(idle);
            if (AbstractStage.outputStream != null
                    && AbstractStage.outputStream.idle() < idle)
                continue;
            if (AbstractStage.errorStream != null
                    && AbstractStage.errorStream.idle() < idle)
                continue;
            if (AbstractStage.accessStream != null
                    && AbstractStage.accessStream.idle() < idle)
                continue;
            break;
        }
    }
    
    /**
     * Prepares a new stage.
     * A locking mechanism prevents unintentional repeated setup.
     * So the method can be called always with {@link BeforeClass}.
     * @throws Exception
     */
    static void prepareStage()
            throws Exception {

        if (AbstractStage.lock++ > 0)
            return;
        AbstractStage.lock = Math.max(1, AbstractStage.lock);

        final String defaultCharsetName = Charset.defaultCharset().name();
        if (!defaultCharsetName.equalsIgnoreCase("ISO-8859-1")
                && !defaultCharsetName.equalsIgnoreCase("Windows-1252"))
            throw new RuntimeException("Character encoding ISO-8859-1 or Windows-1252 required");

        final String version = System.getProperty("java.version");
        if (!version.matches("^1\\.8\\..*$"))
            throw new RuntimeException("Java 1.8.x is required");

        final File rootStage = AbstractStage.getRootStage();
        if (Files.exists(rootStage.toPath()))
            Files.walkFileTree(rootStage.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path path, final BasicFileAttributes attributes)
                        throws IOException {
                    if (Files.exists(path))
                        Files.delete(path);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path path, final IOException exception)
                        throws IOException {
                    if (Files.exists(path))
                        Files.delete(path);
                    return FileVisitResult.CONTINUE;
                }
            });
        rootStage.mkdir();

        final File rootResources = new File(AbstractStage.getRoot(), AbstractStage.PATH_RESOURCES).getCanonicalFile();
        Files.walkFileTree(rootResources.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path path, final BasicFileAttributes attributes)
                    throws IOException {
                Files.createDirectories(rootStage.toPath().resolve(rootResources.toPath().relativize(path)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path path, final BasicFileAttributes attributes)
                    throws IOException {
                Files.copy(path, rootStage.toPath().resolve(rootResources.toPath().relativize(path)));
                return FileVisitResult.CONTINUE;
            }
        });
        
        final String content = new String(Files.readAllBytes(AbstractStage.getRootStageProgramConfiguration().toPath()));
        final Initialize initialize = Initialize.parse(content);
        final List<String> sectionList = Collections.list(initialize.elements());
        for (final String sectionName : sectionList) {
            if (!sectionName.matches("(?i)server:.*:ssl$"))
                continue;
            final Section section = initialize.get(sectionName);
            if (!section.contains("keystore")
                    || !section.contains("password"))
                continue;
            final String keystore = section.get("keystore");
            final String password = section.get("password");
            AbstractStage.keystore = new Keystore() {

                public String getPassword() {
                    return password;
                }

                public File getFile() {
                    return new File(keystore);
                }
            };
            break;
        }

        final File rootStageLibraries = new File(AbstractStage.getRoot(), AbstractStage.PATH_STAGE_LIBRARIES).getCanonicalFile();
        final String libraries = rootStageLibraries.toString();
        System.setProperty("libraries", libraries);

        final File outputLog = AbstractStage.getRootStageOutputLog();
        outputLog.createNewFile();
        AbstractStage.outputStream = new OutputMultiStream();
        AbstractStage.outputStream.subscribe(new FileOutputStream(outputLog));
        AbstractStage.outputStream.subscribe(AbstractStage.systemOut);
        System.setOut(new PrintStream(AbstractStage.outputStream));

        final File errorLog = AbstractStage.getRootStageErrorLog();
        errorLog.createNewFile();
        AbstractStage.errorStream = new OutputMultiStream();
        AbstractStage.errorStream.subscribe(new FileOutputStream(errorLog));
        AbstractStage.errorStream.subscribe(AbstractStage.systemErr);
        System.setErr(new PrintStream(AbstractStage.errorStream));

        final File accessLog = AbstractStage.getRootStageAccessLog();
        accessLog.createNewFile();
        AbstractStage.accessStream = new OutputMultiStream();
        AbstractStage.accessStream.subscribe(new FileOutputStream(accessLog));
        AbstractStage.accessStreamSynchronize = new Thread() {

            @Override
            public void run() {
                try (final FileInputStream input = new FileInputStream(accessLog)) {
                    final byte[] bytes = new byte[65535];
                    while (true) {
                        try {Thread.sleep(10);
                        } catch (InterruptedException exception) {
                            break;
                        }
                        final int size = input.read(bytes);
                        if (size > 0)
                            AbstractStage.accessStream.write(bytes, 0, size);
                    }
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            }
            {
                this.setDaemon(true);
                this.start();
            }};

        Service.main(new String[] {"start", AbstractStage.getRootStageProgramConfiguration().toString()});
        
        AbstractStage.await();
    }

    /**
     * Cleans a stage.
     * A locking mechanism prevents unintentional repeated setup.
     * So the method can be called always with {@link AfterClass}.
     * @throws Exception
     */
    static void cleanStage()
            throws Exception {

        if (--AbstractStage.lock > 0)
            return;
        AbstractStage.lock = Math.max(0, AbstractStage.lock);
        
        Service.destroy();

        AbstractStage.accessStreamSynchronize.interrupt();
        AbstractStage.outputStream.unsubscribe(AbstractStage.systemOut);
        System.setOut(AbstractStage.systemOut);
        AbstractStage.outputStream.close();
        AbstractStage.errorStream.unsubscribe(AbstractStage.systemErr);
        System.setOut(AbstractStage.systemOut);
        AbstractStage.errorStream.close();
        AbstractStage.accessStream.close();
    }

    /** 
     * Prepares the stage for the next test without rebuilding the stage. 
     * @throws InterruptedException
     */
    static void prepareTest()
            throws InterruptedException {
        
        AbstractStage.await();
        
        AbstractStage.outputStreamCapture = AbstractStage.outputStream.capture();
        AbstractStage.errorStreamCapture  = AbstractStage.errorStream.capture();
        AbstractStage.accessStreamCapture = AbstractStage.accessStream.capture();
    }
    
    /** 
     * Cleans up the stage from the last test without rebuilding the stage.
     * The method waits until all data streams are idle. 
     * @throws InterruptedException
     */
    static void cleanTest()
            throws InterruptedException {

        AbstractStage.await();
        
        try {AbstractStage.outputStreamCapture.close();
        } catch (Exception exception) {
        }
        try {AbstractStage.errorStreamCapture.close();
        } catch (Exception exception) {
        }
        try {AbstractStage.accessStreamCapture.close();
        } catch (Exception exception) {
        } 
    }
}