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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.seanox.test.StreamUtils;

/** Test cases for {@link com.seanox.devwex.Loader}. */
public class LoaderTest {

    private static File createZip(final Map<String, byte[]> entries)
            throws Exception {
        final File zipFile = File.createTempFile(LoaderTest.class.getName(), ".zip");
        zipFile.deleteOnExit();
        try (final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (final Map.Entry<String, byte[]> entry : entries.entrySet()) {
                final ZipEntry zipEntry = new ZipEntry(entry.getKey());
                outputStream.putNextEntry(zipEntry);
                outputStream.write(entry.getValue());
                outputStream.closeEntry();
            }
        }
        return zipFile;
    }

    @Test
    public void testGetResourceAsStream_null() {
        final Loader loader = new Loader(Collections.emptyList());
        Assert.assertNull(loader.getResourceAsStream(null));
        Assert.assertNull(loader.getResourceAsStream("   "));
    }

    @Test
    public void testGetResourceAsStream_parent_exists() {
        final Loader loader = new Loader(this.getClass().getClassLoader(), Collections.emptyList());
        Assert.assertNotNull(loader.getResourceAsStream("java/lang/String.class"));
    }

    @Test
    public void testGetResourceAsStream_zip_exists()
            throws Exception {
        final File zip = LoaderTest.createZip(
                Collections.singletonMap("test.txt", ("Hello").getBytes()));
        final Loader loader = new Loader(Collections.singletonList(zip));
        final InputStream in = loader.getResourceAsStream("test.txt");
        Assert.assertNotNull(in);
        Assert.assertEquals("Hello", new String(StreamUtils.read(in)));
    }

    @Test
    public void testGetResourceAsStream_zip_not_exists()
            throws Exception {
        final File zip = LoaderTest.createZip(Collections.emptyMap());
        final Loader loader = new Loader(Collections.singletonList(zip));
        Assert.assertNull(loader.getResourceAsStream("missing.txt"));
    }

    @Test
    public void testGetResource_null() {
        final Loader loader = new Loader(Collections.emptyList());
        Assert.assertNull(loader.getResource(null));
        Assert.assertNull(loader.getResource("   "));
    }

    @Test
    public void testGetResource_parent_exists() {
        final Loader loader = new Loader(this.getClass().getClassLoader(), Collections.emptyList());
        Assert.assertNotNull(loader.getResource("java/lang/String.class"));
    }

    @Test
    public void testGetResource_zip_exists()
            throws Exception {
        final File zip = LoaderTest.createZip(
                Collections.singletonMap("a/b/c.txt", "X".getBytes()));
        final Loader loader = new Loader(Collections.singletonList(zip));
        final URL url = loader.getResource("a/b/c.txt");
        Assert.assertNotNull(url);
        Assert.assertTrue(url.toString().startsWith("jar:file:"));
    }

    @Test
    public void testGetResource_zip_not_exists()
            throws Exception {
        final File zip = LoaderTest.createZip(Collections.emptyMap());
        final Loader loader = new Loader(Collections.singletonList(zip));
        Assert.assertNull(loader.getResource("missing.txt"));
    }

    @Test
    public void testLoadClass_null()
            throws Exception {
        final Loader loader = new Loader(Collections.emptyList());
        Assert.assertNull(loader.loadClass(null, false));
        Assert.assertNull(loader.loadClass("   ", false));
    }

    @Test
    public void testLoadClass_parent_exists()
            throws Exception {
        final Loader loader = new Loader(this.getClass().getClassLoader(), Collections.emptyList());
        final Class<?> source = loader.loadClass("java.lang.String", false);
        Assert.assertEquals(String.class, source);
    }

    @Test
    public void testLoadClass_parent_exists_resolve()
            throws Exception {
        final Loader loader = new Loader(this.getClass().getClassLoader(), Collections.emptyList());
        final Class<?> source = loader.loadClass("java.lang.Integer", true);
        Assert.assertEquals(Integer.class, source);
    }

    @Test
    public void testLoadClass_zip_exists()
            throws Exception {
        final byte[] bytes = StreamUtils.read(
                Loader.class.getResourceAsStream("Loader.class"));
        final File zip = LoaderTest.createZip(
                Collections.singletonMap("com/seanox/devwex/WorkerBenchmark.class", bytes));
        final Loader loader = new Loader(Collections.singletonList(zip));
        final Class<?> source = loader.loadClass("com.seanox.devwex.WorkerBenchmark", false);
        Assert.assertNotNull(source);
        Assert.assertEquals("com.seanox.devwex.WorkerBenchmark", source.getName());
    }

    @Test(expected = ClassNotFoundException.class)
    public void testLoadClass_notFound()
            throws Exception {
        final Loader loader = new Loader(Collections.emptyList());
        loader.loadClass("does.not.Exist", false);
    }

    @Test
    public void testLoadClass_cached()
            throws Exception {
        final Loader loader = new Loader(this.getClass().getClassLoader(), Collections.emptyList());
        final Class<?> a = loader.loadClass("java.lang.String", false);
        final Class<?> b = loader.loadClass("java.lang.String", false);
        Assert.assertSame(a, b);
    }
}
