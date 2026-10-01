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
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.identification.JarIdentification;
import org.apache.maven.shared.jar.identification.hash.JarBytecodeHashAnalyzer;
import org.apache.maven.shared.jar.identification.hash.JarFileHashAnalyzer;
import org.apache.maven.shared.jar.identification.repository.RepositoryHashSearch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositorySearchExposerTest {

    @Test
    void reportsBytecodeReadFailureToCaller() throws Exception {
        File file = createJar();
        JarAnalyzer jarAnalyzer = new JarAnalyzer(file) {
            @Override
            public InputStream getEntryInputStream(JarEntry entry) throws IOException {
                throw new IOException("simulated class-entry read failure");
            }
        };

        RepositoryHashSearch search = new RepositoryHashSearch() {
            @Override
            public List<Artifact> searchFileHash(String hash) {
                return Collections.emptyList();
            }

            @Override
            public List<Artifact> searchBytecodeHash(String hash) {
                return Collections.emptyList();
            }
        };

        RepositorySearchExposer exposer = new RepositorySearchExposer(
                search, new JarFileHashAnalyzer(), new JarBytecodeHashAnalyzer());
        try {
            UncheckedIOException exception = assertThrows(
                    UncheckedIOException.class,
                    () -> exposer.expose(new JarIdentification(), jarAnalyzer));
            assertEquals("simulated class-entry read failure", exception.getCause().getMessage());
        } finally {
            jarAnalyzer.closeQuietly();
        }
    }

    private File createJar() throws IOException {
        File file = File.createTempFile("repository-search-test", ".jar");
        file.deleteOnExit();
        try (JarOutputStream output = new JarOutputStream(new FileOutputStream(file))) {
            output.putNextEntry(new JarEntry("org/example/Example.class"));
            output.write("class".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return file;
    }
}
