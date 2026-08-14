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

import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.shared.jar.AbstractJarAnalyzerTestCase;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JarFileHashAnalyzerTest extends AbstractJarAnalyzerTestCase {

    private final JarFileHashAnalyzer analyzer = new JarFileHashAnalyzer();

    @Test
    void computeHashReturnsTheHashOnFirstCall() throws Exception {
        JarAnalyzer jarAnalyzer = new JarAnalyzer(getSampleJar("codec.jar"));
        try {
            String expected;
            try (InputStream in =
                    Files.newInputStream(jarAnalyzer.getJarData().getFile().toPath())) {
                expected = DigestUtils.sha1Hex(in);
            }

            String actual = analyzer.computeHash(jarAnalyzer);

            assertNotNull(actual, "first call must return the hash it just computed, not null");
            assertEquals(expected, actual, "file hash must be the SHA-1 of the jar file");
        } finally {
            jarAnalyzer.closeQuietly();
        }
    }

    @Test
    void computeHashIsStableAcrossCalls() throws Exception {
        JarAnalyzer jarAnalyzer = new JarAnalyzer(getSampleJar("codec.jar"));
        try {
            String first = analyzer.computeHash(jarAnalyzer);
            // Second call is served from the JarData cache rather than recomputed.
            String second = analyzer.computeHash(jarAnalyzer);

            assertEquals(first, second, "cached call must agree with the computed one");
        } finally {
            jarAnalyzer.closeQuietly();
        }
    }

    @Test
    void computeHashPopulatesJarData() throws Exception {
        JarAnalyzer jarAnalyzer = new JarAnalyzer(getSampleJar("codec.jar"));
        try {
            String returned = analyzer.computeHash(jarAnalyzer);

            assertEquals(
                    returned,
                    jarAnalyzer.getJarData().getFileHash(),
                    "returned hash and the value cached on JarData must agree");
        } finally {
            jarAnalyzer.closeQuietly();
        }
    }
}
