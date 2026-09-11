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
package org.apache.maven.shared.jar.identification.exposers;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.identification.JarIdentification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Case for Filename Taxon Data.
 */
class FilenameExposerTest {

    private JarIdentification expose(String jarName) throws Exception {
        Path dir = Files.createTempDirectory("filename-exposer");
        File jarFile = new File(dir.toFile(), jarName);
        jarFile.deleteOnExit();

        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))) {
            jos.putNextEntry(new JarEntry("README"));
            jos.write(new byte[] {1});
            jos.closeEntry();
        }

        JarAnalyzer analyzer = new JarAnalyzer(jarFile);
        JarIdentification identification = new JarIdentification();
        try {
            new FilenameExposer().expose(identification, analyzer);
        } finally {
            analyzer.closeQuietly();
        }
        return identification;
    }

    @Test
    void versionWithMultiDigitPrefixIsNotTruncated() throws Exception {
        JarIdentification identification = expose("my-lib-12.3.4.jar");

        assertEquals(1, identification.getPotentialArtifactIds().size());
        assertEquals("my-lib", identification.getPotentialArtifactIds().get(0));
        assertEquals(1, identification.getPotentialVersions().size());
        assertEquals("12.3.4", identification.getPotentialVersions().get(0));
    }

    @Test
    void versionWithMultiDigitPrefixNoMinorIsNotTruncated() throws Exception {
        JarIdentification identification = expose("my-lib-10.jar");

        assertEquals("my-lib", identification.getPotentialArtifactIds().get(0));
        assertEquals("10", identification.getPotentialVersions().get(0));
    }

    @Test
    void versionWithMultiDigitPrefixIsNotTruncatedWhenOtherSegmentsFollow() throws Exception {
        JarIdentification identification = expose("my-lib-21.5.7.jar");

        assertEquals("my-lib", identification.getPotentialArtifactIds().get(0));
        assertEquals("21.5.7", identification.getPotentialVersions().get(0));
    }

    @Test
    void singleDigitVersionIsPreserved() throws Exception {
        JarIdentification identification = expose("my-lib-9.0.jar");

        assertEquals("my-lib", identification.getPotentialArtifactIds().get(0));
        assertEquals("9.0", identification.getPotentialVersions().get(0));
    }

    @Test
    void filenameWithoutVersionHasNoVersion() throws Exception {
        JarIdentification identification = expose("my-lib.jar");

        assertEquals("my-lib", identification.getPotentialArtifactIds().get(0));
        assertTrue(identification.getPotentialVersions().isEmpty());
    }
}
