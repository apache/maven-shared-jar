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
package org.apache.maven.shared.jar.identification.hash;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.shared.jar.AbstractJarAnalyzerTestCase;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests for the {@link JarBytecodeHashAnalyzer}.
 */
class JarBytecodeHashAnalyzerTest extends AbstractJarAnalyzerTestCase {

    private final JarBytecodeHashAnalyzer analyzer = new JarBytecodeHashAnalyzer();

    @Test
    void computeHashCoversAllClassEntries() throws Exception {
        JarAnalyzer jarAnalyzer = new JarAnalyzer(getSampleJar("codec.jar"));
        try {
            ByteArrayOutputStream allClasses = new ByteArrayOutputStream();
            for (JarEntry entry : jarAnalyzer.getClassEntries()) {
                try (InputStream is = jarAnalyzer.getEntryInputStream(entry)) {
                    allClasses.write(readAll(is));
                }
            }
            String expected = DigestUtils.sha1Hex(allClasses.toByteArray());

            String actual = analyzer.computeHash(jarAnalyzer);

            assertEquals(expected, actual, "bytecode hash must cover every class entry");
        } finally {
            jarAnalyzer.closeQuietly();
        }
    }

    @Test
    void distinctClassSetsProduceDistinctHashes() throws Exception {
        JarAnalyzer jarA = new JarAnalyzer(createJar("org/foo/A.class", "org/zshared/Z.class"));
        JarAnalyzer jarB = new JarAnalyzer(createJar("org/bar/B.class", "org/zshared/Z.class"));
        try {
            assertNotEquals(
                    analyzer.computeHash(jarA),
                    analyzer.computeHash(jarB),
                    "jars sharing only the last-sorted class entry must not produce the same bytecode hash");
        } finally {
            jarA.closeQuietly();
            jarB.closeQuietly();
        }
    }

    private File createJar(String... entryNames) throws IOException {
        File file = File.createTempFile("bytecode-hash-test", ".jar");
        file.deleteOnExit();
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(file))) {
            for (String entryName : entryNames) {
                jos.putNextEntry(new JarEntry(entryName));
                jos.write(entryName.getBytes(StandardCharsets.UTF_8));
                jos.closeEntry();
            }
        }
        return file;
    }

    private static byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = is.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
