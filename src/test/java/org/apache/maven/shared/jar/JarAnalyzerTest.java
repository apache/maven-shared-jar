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
import java.io.IOException;
import java.util.zip.ZipException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
    void testSealed() throws Exception {
        JarData jarData = getJarData("evil-sealed-regex-1.0.jar");
        assertTrue(jarData.isSealed());
    }

    @Test
    void testNotSealed() throws Exception {
        JarData jarData = getJarData("codec.jar");
        assertFalse(jarData.isSealed());
    }

    @Test
    void testMissingFile() {
        assertThrows(IOException.class, () -> new JarAnalyzer(new File("foo-bar-this-should-not-exist.jar")));
    }

    @Test
    void testInvalidJarFile() throws Exception {
        assertThrows(ZipException.class, () -> getJarAnalyzer("invalid.jar"));
    }

    @Test
    void testCloseTwice() throws Exception {
        JarAnalyzer jarAnalyzer = getJarAnalyzer("codec.jar");

        // no exception should be thrown
        Assertions.assertDoesNotThrow(() -> {
            jarAnalyzer.closeQuietly();
            jarAnalyzer.closeQuietly();
        });
    }
}
