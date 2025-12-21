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
package org.apache.maven.shared.jar.identification;

import javax.inject.Inject;

import java.io.File;

import org.apache.maven.shared.jar.AbstractJarAnalyzerTestCase;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * JarAnalyzer Taxon Analyzer Test Case
 * TODO test the exposers individually instead of in aggregate here (and test the normalize, etc. methods here instead with controlled exposers)
 */
@PlexusTest
class JarIdentificationAnalyzerTest extends AbstractJarAnalyzerTestCase {

    @Inject
    JarIdentificationAnalysis analyzer;

    private JarIdentification getJarTaxon(String filename) throws Exception {
        File jarfile = getSampleJar(filename);
        JarIdentification taxon = analyzer.analyze(new JarAnalyzer(jarfile));
        assertNotNull(taxon, "JarIdentification");
        return taxon;
    }

    @Test
    void taxonAnalyzerWithJXR() throws Exception {
        JarIdentification taxon = getJarTaxon("jxr.jar");

        assertEquals("org.apache.maven", taxon.getGroupId(), "identification.groupId");
        assertEquals("maven-jxr", taxon.getArtifactId(), "identification.artifactId");
        assertEquals("1.1-SNAPSHOT", taxon.getVersion(), "identification.version");
        assertEquals("Maven JXR", taxon.getName(), "identification.name");
        assertEquals("Apache Software Foundation", taxon.getVendor(), "identification.vendor");

        // TODO assert potentials too
    }

    /**
     * Tests JarAnalyzer with No embedded pom, and no useful manifest.mf information.
     *
     * @throws Exception failures
     */
    @Test
    void taxonAnalyzerWithCODEC() throws Exception {
        JarIdentification taxon = getJarTaxon("codec.jar");

        assertEquals("org.apache.commons.codec", taxon.getGroupId(), "identification.groupId");
        assertEquals("codec", taxon.getArtifactId(), "identification.artifactId");
        // TODO fix assertion
        // assertEquals( "identification.version", "codec_release_1_0_0_interim_20030519095102_build",
        // identification.getVersion() );
        assertEquals("20030519", taxon.getVersion(), "identification.version");
        assertEquals("codec", taxon.getName(), "identification.name");
        assertNull(taxon.getVendor(), "identification.vendor");

        // TODO assert potentials too
    }

    @Test
    void taxonAnalyzerWithANT() throws Exception {
        JarIdentification taxon = getJarTaxon("ant.jar");

        assertEquals("org.apache.tools.ant", taxon.getGroupId(), "identification.groupId");
        assertEquals("ant", taxon.getArtifactId(), "identification.artifactId");
        assertEquals("1.6.5", taxon.getVersion(), "identification.version");
        // TODO fix assertion
        // assertEquals( "identification.name", "Apache Ant", identification.getName() );
        assertEquals("Apache Software Foundation", taxon.getVendor(), "identification.vendor");

        // TODO assert potentials too
    }
}
