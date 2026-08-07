/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.shared.jar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the JarAnalyzer class.
 */
class JarAnalyzerTest extends AbstractJarAnalyzerTestCase {
    private JarAnalyzer jarAnalyzer;

    @AfterEach
    protected void tearDown() throws Exception {
        if (jarAnalyzer != null) {
            jarAnalyzer.closeQuietly();
        }
    }

    private JarData getJarData(String filename) throws Exception {
        jarAnalyzer = getJarAnalyzer(filename);
        return jarAnalyzer.getJarData();
    }

    private JarAnalyzer getJarAnalyzer(String filename) throws IOException {
        return new JarAnalyzer(getSampleJar(filename));
    }

    @Test
    void sealed() throws Exception {
        JarData jarData = getJarData("evil-sealed-regex-1.0.jar");
        assertTrue(jarData.isSealed());
    }

    @Test
    void notSealed() throws Exception {
        JarData jarData = getJarData("codec.jar");
        assertFalse(jarData.isSealed());
    }

    @Test
    void multiRelease() throws Exception {
        JarData jarData = getJarData("multi-release-test-0.0.1.jar");
        assertTrue(jarData.isMultiRelease());
    }

    @Test
    void notMultiRelease() throws Exception {
        JarData jarData = getJarData("codec.jar");
        assertFalse(jarData.isMultiRelease());
    }

    @Test
    void missingFile() {
        assertThrows(IOException.class, () -> new JarAnalyzer(new File("foo-bar-this-should-not-exist.jar")));
    }

    @Test
    void invalidJarFile() throws Exception {
        assertThrows(ZipException.class, () -> getJarAnalyzer("invalid.jar"));
    }

    @Test
    void closeTwice() throws Exception {
        JarAnalyzer jarAnalyzer = getJarAnalyzer("codec.jar");

        // no exception should be thrown
        assertDoesNotThrow(() -> {
            jarAnalyzer.closeQuietly();
            jarAnalyzer.closeQuietly();
        });
    }

    @Test
    void getClassEntriesFindsClassesWithUnderscores() throws Exception {
        File jarFile = File.createTempFile("jar-analyzer-underscore", ".jar");
        jarFile.deleteOnExit();

        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))) {
            for (String entryName : new String[] {
                "org/example/My_Helper.class",
                "org/example/some_test.class",
                "org/example/UPPER_CASE.class",
                "org/example/MyHelper.class"
            }) {
                jos.putNextEntry(new JarEntry(entryName));
                jos.write(new byte[] {1, 2, 3});
                jos.closeEntry();
            }
        }

        jarAnalyzer = new JarAnalyzer(jarFile);

        List<String> classNames =
                jarAnalyzer.getClassEntries().stream().map(JarEntry::getName).collect(Collectors.toList());

        assertTrue(classNames.contains("org/example/My_Helper.class"), "Missing underscore class: " + classNames);
        assertTrue(classNames.contains("org/example/some_test.class"), "Missing underscore class: " + classNames);
        assertTrue(classNames.contains("org/example/UPPER_CASE.class"), "Missing underscore class: " + classNames);
        assertTrue(classNames.contains("org/example/MyHelper.class"), "Missing regular class: " + classNames);
    }
}
