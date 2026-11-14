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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.shared.jar.AbstractJarAnalyzerTestCase;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.identification.exposers.RepositorySearchExposer;
import org.apache.maven.shared.jar.identification.hash.JarHashAnalyzer;
import org.apache.maven.shared.jar.identification.repository.RepositoryHashSearch;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private JarIdentification analyzeWithExposer(JarIdentificationExposer exposer) throws Exception {
        JarAnalyzer jarAnalyzer = new JarAnalyzer(getSampleJar("jxr.jar"));
        try {
            return new JarIdentificationAnalysis(Collections.singletonList(exposer)).analyze(jarAnalyzer);
        } finally {
            jarAnalyzer.closeQuietly();
        }
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

        assertNull(taxon.getGroupId(), "ambiguous identification.groupId");
        assertTrue(taxon.getPotentialGroupIds().contains("org.apache.commons.codec"), "potential groupId");
        assertEquals("codec", taxon.getArtifactId(), "identification.artifactId");
        assertNull(taxon.getVersion(), "ambiguous identification.version");
        assertTrue(taxon.getPotentialVersions().contains("20030519"), "potential version");
        assertEquals("codec", taxon.getName(), "identification.name");
        assertNull(taxon.getVendor(), "identification.vendor");

        // TODO assert potentials too
    }

    @Test
    void taxonAnalyzerWithANT() throws Exception {
        JarIdentification taxon = getJarTaxon("ant.jar");

        assertNull(taxon.getGroupId(), "ambiguous identification.groupId");
        assertTrue(taxon.getPotentialGroupIds().contains("org.apache.tools.ant"), "potential groupId");
        assertEquals("ant", taxon.getArtifactId(), "identification.artifactId");
        assertNull(taxon.getVersion(), "ambiguous identification.version");
        assertTrue(taxon.getPotentialVersions().contains("1.6.5"), "potential version");
        // TODO fix assertion
        // assertEquals( "identification.name", "Apache Ant", identification.getName() );
        assertEquals("Apache Software Foundation", taxon.getVendor(), "identification.vendor");

        // TODO assert potentials too
    }

    @Test
    void leavesAmbiguousPotentialValuesUnset() throws Exception {
        JarIdentification identification = analyzeWithExposer((result, ignored) -> {
            result.addGroupId("org.example");
            result.addGroupId("com.example");
            result.addArtifactId("jxr");
            result.addArtifactId("maven-jxr");
            result.addVersion("1.0");
            result.addVersion("1.0.3");
            result.addName("jxr");
            result.addName("Maven JXR");
            result.addVendor("Apache");
            result.addVendor("Apache Software Foundation");
        });

        assertNull(identification.getGroupId(), "ambiguous groupId");
        assertNull(identification.getArtifactId(), "ambiguous artifactId");
        assertNull(identification.getVersion(), "ambiguous version");
        assertNull(identification.getName(), "ambiguous name");
        assertNull(identification.getVendor(), "ambiguous vendor");
        assertEquals(2, identification.getPotentialGroupIds().size(), "potential groupIds");
        assertEquals(2, identification.getPotentialArtifactIds().size(), "potential artifactIds");
        assertEquals(2, identification.getPotentialVersions().size(), "potential versions");
        assertEquals(2, identification.getPotentialNames().size(), "potential names");
        assertEquals(2, identification.getPotentialVendors().size(), "potential vendors");
    }

    @Test
    void infersValuesFromSinglePotentialCandidates() throws Exception {
        JarIdentification identification = analyzeWithExposer((result, ignored) -> {
            result.addGroupId("org.example");
            result.addArtifactId("example-artifact");
            result.addVersion("1.2.3");
            result.addName("Example Artifact");
            result.addVendor("Example Org");
        });

        assertEquals("org.example", identification.getGroupId(), "groupId");
        assertEquals("example-artifact", identification.getArtifactId(), "artifactId");
        assertEquals("1.2.3", identification.getVersion(), "version");
        assertEquals("Example Artifact", identification.getName(), "name");
        assertEquals("Example Org", identification.getVendor(), "vendor");
    }

    @Test
    void retainsExplicitValuesWhenPotentialCandidatesConflict() throws Exception {
        JarIdentification identification = analyzeWithExposer((result, ignored) -> {
            result.addGroupId("org.example");
            result.addGroupId("com.example");
            result.addAndSetGroupId("org.example");
            result.addVersion("1.0");
            result.addVersion("1.0.3");
            result.addAndSetVersion("1.0.3");
        });

        assertEquals("org.example", identification.getGroupId(), "explicit groupId");
        assertEquals("1.0.3", identification.getVersion(), "explicit version");
    }

    @Test
    void usesTheOnlyRepositoryMatch() throws Exception {
        Artifact match = artifact("org.example", "example", "1.2.3");
        JarIdentification identification = analyzeWithExposer(repositoryExposer(Collections.singletonList(match)));

        assertEquals("org.example", identification.getGroupId(), "groupId");
        assertEquals("example", identification.getArtifactId(), "artifactId");
        assertEquals("1.2.3", identification.getVersion(), "version");
    }

    @Test
    void leavesConflictingRepositoryMatchesUnselected() throws Exception {
        List<Artifact> matches = Arrays.asList(
                artifact("org.example", "example", "1.2.3"), artifact("com.example", "example-legacy", "2.0"));
        JarIdentification identification = analyzeWithExposer(repositoryExposer(matches));

        assertNull(identification.getGroupId(), "ambiguous groupId");
        assertNull(identification.getArtifactId(), "ambiguous artifactId");
        assertNull(identification.getVersion(), "ambiguous version");
        assertTrue(identification.getPotentialGroupIds().contains("org.example"), "first potential groupId");
        assertTrue(identification.getPotentialGroupIds().contains("com.example"), "second potential groupId");
    }

    private static Artifact artifact(String groupId, String artifactId, String version) {
        return new DefaultArtifact(groupId, artifactId, version, Artifact.SCOPE_COMPILE, "jar", "", null);
    }

    private RepositorySearchExposer repositoryExposer(List<Artifact> matches) {
        RepositoryHashSearch search = new RepositoryHashSearch() {
            @Override
            public List<Artifact> searchFileHash(String hash) {
                return matches;
            }

            @Override
            public List<Artifact> searchBytecodeHash(String hash) {
                return Collections.emptyList();
            }
        };
        JarHashAnalyzer hashes = ignored -> "hash";
        return new RepositorySearchExposer(search, hashes, hashes);
    }
}
