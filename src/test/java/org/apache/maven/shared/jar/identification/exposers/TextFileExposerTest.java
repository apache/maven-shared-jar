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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.identification.JarIdentification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test Case for {@link TextFileExposer}.
 */
class TextFileExposerTest {

    @Test
    void readsAllLinesOfVersionFile() throws Exception {
        File jar = createJarWithVersionFile("META-INF/VERSION", "1.2.3\nrelease\n");

        JarIdentification identification = new JarIdentification();
        TextFileExposer exposer = new TextFileExposer();
        exposer.expose(identification, new JarAnalyzer(jar));

        List<String> versions = identification.getPotentialVersions();
        assertEquals(Arrays.asList("1.2.3", "release"), versions);
    }

    @Test
    void readsAllLinesOfVersionPropertiesFile() throws Exception {
        File jar = createJarWithVersionFile(
                "META-INF/version.properties", "Implementation-Version=2.0.0\nBuild-Number=42\n");

        JarIdentification identification = new JarIdentification();
        TextFileExposer exposer = new TextFileExposer();
        exposer.expose(identification, new JarAnalyzer(jar));

        List<String> versions = identification.getPotentialVersions();
        assertEquals(Arrays.asList("Implementation-Version=2.0.0", "Build-Number=42"), versions);
    }

    private File createJarWithVersionFile(String entryName, String content) throws IOException {
        File file = File.createTempFile("text-file-exposer-test", ".jar");
        file.deleteOnExit();
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(file))) {
            jos.putNextEntry(new JarEntry(entryName));
            jos.write(content.getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
        }
        return file;
    }
}
