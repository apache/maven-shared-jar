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

import org.apache.maven.shared.jar.AbstractJarAnalyzerTestCase;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.identification.JarIdentification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Case for Embedded Maven Model Taxon Data.
 */
class EmbeddedMavenModelExposerTest extends AbstractJarAnalyzerTestCase {

    @Test
    void testExposerWithParent() throws Exception {
        File file = getSampleJar("test1.jar");

        JarIdentification identification = new JarIdentification();

        EmbeddedMavenModelExposer exposer = new EmbeddedMavenModelExposer();
        exposer.expose(identification, new JarAnalyzer(file));

        assertEquals(1, identification.getPotentialGroupIds().size());
        assertEquals("test", identification.getPotentialGroupIds().get(0));

        assertEquals(1, identification.getPotentialArtifactIds().size());
        assertEquals("test1", identification.getPotentialArtifactIds().get(0));

        assertEquals(1, identification.getPotentialVersions().size());
        assertEquals("1.1-SNAPSHOT", identification.getPotentialVersions().get(0));
    }

    @Test
    void testExposerWithJXR() throws Exception {
        File file = getSampleJar("jxr.jar");

        JarIdentification identification = new JarIdentification();

        EmbeddedMavenModelExposer exposer = new EmbeddedMavenModelExposer();
        exposer.expose(identification, new JarAnalyzer(file));

        assertFalse(identification.getPotentialGroupIds().isEmpty(), "exposer.groupIds");
        assertFalse(identification.getPotentialArtifactIds().isEmpty(), "exposer.artifactIds");
        assertFalse(identification.getPotentialVersions().isEmpty(), "exposer.versions");

        // TODO test others
    }

    @Test
    void testExposerWithANT() throws Exception {
        File file = getSampleJar("ant.jar");

        JarIdentification identification = new JarIdentification();

        EmbeddedMavenModelExposer exposer = new EmbeddedMavenModelExposer();
        exposer.expose(identification, new JarAnalyzer(file));

        assertTrue(identification.getPotentialGroupIds().isEmpty(), "exposer.groupIds");
        assertTrue(identification.getPotentialArtifactIds().isEmpty(), "exposer.artifactIds");
        assertTrue(identification.getPotentialVersions().isEmpty(), "exposer.versions");

        // TODO test others
    }
}
