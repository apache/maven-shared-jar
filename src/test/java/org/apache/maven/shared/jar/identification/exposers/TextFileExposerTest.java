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

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.identification.JarIdentification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextFileExposerTest {
    @Test
    void exposesUtf8Text(@TempDir Path tempDir) throws Exception {
        Path jar = tempDir.resolve("utf8.jar");
        try (OutputStream output = Files.newOutputStream(jar);
                JarOutputStream jarOutput = new JarOutputStream(output)) {
            jarOutput.putNextEntry(new JarEntry("version.txt"));
            jarOutput.write("1.é\n".getBytes(StandardCharsets.UTF_8));
            jarOutput.closeEntry();
        }

        JarAnalyzer analyzer = new JarAnalyzer(jar.toFile());
        try {
            JarIdentification identification = new JarIdentification();
            new TextFileExposer().expose(identification, analyzer);

            assertEquals(Collections.singletonList("1.é"), identification.getPotentialVersions());
        } finally {
            analyzer.closeQuietly();
        }
    }
}
