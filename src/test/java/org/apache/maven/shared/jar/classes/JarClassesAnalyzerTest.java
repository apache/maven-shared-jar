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
package org.apache.maven.shared.jar.classes;

import javax.inject.Inject;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;

import org.apache.maven.shared.jar.AbstractJarAnalyzerTestCase;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.JarData;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JarAnalyzer Classes Test Case
 */
@PlexusTest
class JarClassesAnalyzerTest extends AbstractJarAnalyzerTestCase {

    @Inject
    private JarClassesAnalysis analyzer;

    @Test
    void analyzeJXR() throws Exception {
        JarClasses jclass = getJarClasses("jxr.jar");

        assertFalse(jclass.getImports().isEmpty(), "classes.imports.length > 0");
        assertFalse(jclass.getPackages().isEmpty(), "classes.packages.length > 0");
        assertFalse(jclass.getMethods().isEmpty(), "classes.methods.length > 0");

        assertNotContainsRegex("Import List", "[\\[\\)\\(\\;]", jclass.getImports());

        // TODO: test for classes, methods, etc.

        assertTrue(jclass.getImports().contains("org.apache.maven.jxr.JXR"), "classes.imports");
        assertTrue(jclass.getImports().contains("org.apache.oro.text.perl.Perl5Util"), "classes.imports");
        assertTrue(jclass.getPackages().contains("org.apache.maven.jxr.pacman"), "classes.packages");
    }

    @Test
    void analyzeANT() throws Exception {
        JarClasses jclass = getJarClasses("ant.jar");

        assertFalse(jclass.getImports().isEmpty(), "classes.imports.length > 0");
        assertFalse(jclass.getPackages().isEmpty(), "classes.packages.length > 0");
        assertFalse(jclass.getMethods().isEmpty(), "classes.methods.length > 0");

        assertNotContainsRegex("Import List", "[\\[\\)\\(\\;]", jclass.getImports());

        assertTrue(jclass.getImports().contains("java.util.zip.GZIPInputStream"), "classes.imports");
        assertTrue(jclass.getImports().contains("org.apache.tools.ant.XmlLogger$TimedElement"), "classes.imports");
        assertTrue(jclass.getImports().contains("org.apache.tools.mail.MailMessage"), "classes.imports");
        assertTrue(jclass.getPackages().contains("org.apache.tools.ant"), "classes.packages");
        assertTrue(jclass.getPackages().contains("org.apache.tools.bzip2"), "classes.packages");
    }

    @Test
    void analyzeJarWithInvalidClassFile() throws Exception {
        JarClasses jclass = getJarClasses("invalid-class-file.jar");

        // Doesn't fail, as exceptions are ignored.
        assertTrue(jclass.getClassNames().isEmpty());
        assertTrue(jclass.getPackages().isEmpty());
        assertTrue(jclass.getImports().isEmpty());
        assertNull(jclass.getJdkRevision());
        assertTrue(jclass.getMethods().isEmpty());
    }

    @Test
    void analyzeJarWithDebug() throws Exception {
        JarClasses jclass = getJarClasses("helloworld-1.4-debug.jar");

        assertTrue(jclass.isDebugPresent(), "has debug");
    }

    @Test
    void analyzeJarWithoutDebug() throws Exception {
        JarClasses jclass = getJarClasses("helloworld-1.4.jar");

        assertFalse(jclass.isDebugPresent(), "no debug present");
    }

    static String[][] testAnalyzeJarVersion() {
        return new String[][] {
            {"helloworld-1.1.jar", "1.1"},
            {"helloworld-1.2.jar", "1.2"},
            {"helloworld-1.3.jar", "1.3"},
            {"helloworld-1.4.jar", "1.4"},
            {"helloworld-1.5.jar", "1.5"},
            {"helloworld-1.6.jar", "1.6"},
            {"helloworld-1.7.jar", "1.7"},
            {"helloworld-1.8.jar", "1.8"},
            {"helloworld-9.jar", "9"},
            {"helloworld-10.jar", "10"},
            {"helloworld-11.jar", "11"},
            {"helloworld-12.jar", "12"},
            {"helloworld-13.jar", "13"},
            {"helloworld-14.jar", "14"},
            {"helloworld-15.jar", "15"},
            {"helloworld-16.jar", "16"},
            {"helloworld-17.jar", "17"},
            {"helloworld-18.jar", "18"},
            {"helloworld-19.jar", "19"},
            {"helloworld-20.jar", "20"},
            {"helloworld-21.jar", "21"},
            {"helloworld-23.jar", "23"},
            {"helloworld-22.jar", "22"},
            {"helloworld-24.jar", "24"},
            {"helloworld-25.jar", "25"}
        };
    }

    @ParameterizedTest
    @MethodSource
    void testAnalyzeJarVersion(String jarName, String expectedRevision) throws Exception {
        JarClasses jclass = getJarClasses(jarName);

        assertEquals(expectedRevision, jclass.getJdkRevision());
    }

    @Test
    void analyzeJarWithModuleInfoClass() throws Exception {
        JarData jarData = getJarData("tomcat-jni-9.0.75.jar");
        JarClasses jclass = jarData.getJarClasses();
        assertEquals("1.8", jclass.getJdkRevision());
    }

    @Test
    void analyzeJarWithOnlyModuleInfoClass() throws Exception {
        JarData jarData = getJarData("module-info-only-test-0.0.1.jar");
        assertEquals(10, jarData.getNumEntries());
        // root level information
        assertEquals(8, jarData.getNumRootEntries());
        JarClasses jclass = jarData.getJarClasses();
        assertTrue(jclass.getImports().isEmpty());
        assertTrue(jclass.getPackages().isEmpty());
        assertTrue(jclass.getClassNames().isEmpty());
        assertTrue(jclass.getMethods().isEmpty());
        assertNull(jclass.getJdkRevision());

        JarVersionedRuntimes jarVersionedRuntimes = jarData.getVersionedRuntimes();
        assertNotNull(jarVersionedRuntimes);
        Map<Integer, JarVersionedRuntime> jarVersionedRuntimeMap = jarVersionedRuntimes.getVersionedRuntimeMap();
        assertNotNull(jarVersionedRuntimeMap);
        assertEquals(1, jarVersionedRuntimeMap.size()); // 11

        JarVersionedRuntime jarVersionedRuntime11 = jarVersionedRuntimes.getJarVersionedRuntime(11);
        JarClasses jarClasses11 = jarVersionedRuntime11.getJarClasses();
        assertEquals("11", jarClasses11.getJdkRevision());
        assertTrue(jarClasses11.getImports().isEmpty());
        assertEquals(1, jarClasses11.getPackages().size());
        assertEquals("", jarClasses11.getPackages().get(0));
        assertEquals(1, jarClasses11.getClassNames().size());
        assertTrue(jarClasses11.getMethods().isEmpty());
        assertEquals(2, jarVersionedRuntime11.getNumEntries());
        assertEntriesContains(jarVersionedRuntime11.getEntries(), "META-INF/versions/11/module-info.class");
    }

    @Test
    void analyzeMultiReleaseJarVersion() throws Exception {
        JarData jarData = getJarData("multi-release-test-0.0.1.jar");
        assertEquals(37, jarData.getNumEntries());
        // root level information
        assertEquals(17, jarData.getNumRootEntries());
        JarClasses jclass = jarData.getJarClasses();
        assertEquals("1.8", jclass.getJdkRevision());
        assertFalse(jclass.getImports().isEmpty());
        assertEquals(1, jclass.getPackages().size());
        assertEquals(1, jclass.getClassNames().size());
        assertFalse(jclass.getMethods().isEmpty());
        assertEntriesContains(jarData.getEntries(), "resource.txt");

        JarVersionedRuntimes jarVersionedRuntimes = jarData.getVersionedRuntimes();
        assertNotNull(jarVersionedRuntimes);
        Map<Integer, JarVersionedRuntime> jarVersionedRuntimeMap = jarVersionedRuntimes.getVersionedRuntimeMap();
        assertNotNull(jarVersionedRuntimeMap);
        assertEquals(2, jarVersionedRuntimeMap.size()); // 9 and 11

        JarVersionedRuntime jarVersionedRuntime9 = jarVersionedRuntimes.getJarVersionedRuntime(9);
        JarClasses jarClasses9 = jarVersionedRuntime9.getJarClasses();
        assertEquals("9", jarClasses9.getJdkRevision());
        assertFalse(jarClasses9.getImports().isEmpty());
        assertEquals(1, jarClasses9.getPackages().size());
        assertEquals(1, jarClasses9.getClassNames().size());
        assertFalse(jarClasses9.getMethods().isEmpty());
        assertEquals(10, jarVersionedRuntime9.getNumEntries());
        assertEntriesContains(jarVersionedRuntime9.getEntries(), "META-INF/versions/9/resource.txt");

        JarVersionedRuntime jarVersionedRuntime11 = jarVersionedRuntimes.getJarVersionedRuntime(11);
        JarClasses jarClasses11 = jarVersionedRuntime11.getJarClasses();
        assertEquals("11", jarClasses11.getJdkRevision());
        assertFalse(jarClasses11.getImports().isEmpty());
        assertEquals(1, jarClasses11.getPackages().size());
        assertEquals(1, jarClasses11.getClassNames().size());
        assertFalse(jarClasses11.getMethods().isEmpty());
        assertEquals(10, jarVersionedRuntime11.getNumEntries());
        assertEntriesContains(jarVersionedRuntime11.getEntries(), "META-INF/versions/11/resource.txt");

        // test ordering
        assertEquals("[9, 11]", jarVersionedRuntimeMap.keySet().toString());

        // test best fit
        try {
            jarVersionedRuntimes.getBestFitJarVersionedRuntime(null);
            fail("It should throw an NPE");
        } catch (NullPointerException e) {
        }
        assertNull(jarVersionedRuntimes.getBestFitJarVersionedRuntime(8)); // unreal value but good just for testing
        assertEquals(
                "9",
                jarVersionedRuntimes
                        .getBestFitJarVersionedRuntime(9)
                        .getJarClasses()
                        .getJdkRevision());
        assertEquals(
                "9",
                jarVersionedRuntimes
                        .getBestFitJarVersionedRuntime(10)
                        .getJarClasses()
                        .getJdkRevision());
        assertEquals(
                "11",
                jarVersionedRuntimes
                        .getBestFitJarVersionedRuntime(11)
                        .getJarClasses()
                        .getJdkRevision());
        assertEquals(
                "11",
                jarVersionedRuntimes
                        .getBestFitJarVersionedRuntime(20)
                        .getJarClasses()
                        .getJdkRevision());

        try {
            jarVersionedRuntimes.getBestFitJarVersionedRuntimeBySystemProperty(null);
            fail("It should throw an NPE");
        } catch (NullPointerException e) {
        }

        try {
            getBestFitReleaseBySystemProperty(jarVersionedRuntimes, null);
            fail("It should throw an NPE");
        } catch (NullPointerException e) {
        }

        try {
            getBestFitReleaseBySystemProperty(jarVersionedRuntimes, "xxx");
            fail("It should throw an ISE");
        } catch (IllegalStateException e) {
        }

        assertNull(getBestFitReleaseBySystemProperty(jarVersionedRuntimes, "8"));
        assertEquals(
                "9",
                getBestFitReleaseBySystemProperty(jarVersionedRuntimes, "9")
                        .getJarClasses()
                        .getJdkRevision());
        assertEquals(
                "9",
                getBestFitReleaseBySystemProperty(jarVersionedRuntimes, "10")
                        .getJarClasses()
                        .getJdkRevision());
        assertEquals(
                "11",
                getBestFitReleaseBySystemProperty(jarVersionedRuntimes, "11")
                        .getJarClasses()
                        .getJdkRevision());
        assertEquals(
                "11",
                getBestFitReleaseBySystemProperty(jarVersionedRuntimes, "20")
                        .getJarClasses()
                        .getJdkRevision());
    }

    /**
     * Exposes issue MSHARED-1413
     */
    @Test
    void analyzeMultiReleaseJarWithVersion11HasALowerJdkRevisionClass() {
        Assertions.assertDoesNotThrow(() -> {
            // Version 11 has one class compiled to target Java 1.8
            JarData jarData = getJarData("multi-release-version-with-lower-jdk-revision-class-0.0.1.jar");
            JarClasses jclass = jarData.getJarClasses();

            assertNull(jclass.getJdkRevision());

            JarVersionedRuntimes jarVersionedRuntimes = jarData.getVersionedRuntimes();
            assertNotNull(jarVersionedRuntimes);
            Map<Integer, JarVersionedRuntime> jarVersionedRuntimeMap = jarVersionedRuntimes.getVersionedRuntimeMap();
            assertNotNull(jarVersionedRuntimeMap);
            assertEquals(1, jarVersionedRuntimeMap.size()); // 11

            JarVersionedRuntime jarVersionedRuntime11 = jarVersionedRuntimes.getJarVersionedRuntime(11);
            JarClasses jarClasses11 = jarVersionedRuntime11.getJarClasses();
            assertEquals("1.8", jarClasses11.getJdkRevision());
        }, "It should not raise an exception");
    }

    /**
     * Ensures no exceptions are raised when versioned content does not contain classes (just resources)
     */
    @Test
    void analyzeMultiReleaseJarResourcesOnly() {
        Assertions.assertDoesNotThrow(() -> {
            JarData jarData = getJarData("multi-release-resources-only-0.0.1.jar");
            JarClasses jclass = jarData.getJarClasses();

            assertEquals("1.8", jclass.getJdkRevision());

            JarVersionedRuntimes jarVersionedRuntimes = jarData.getVersionedRuntimes();
            assertNotNull(jarVersionedRuntimes);
            Map<Integer, JarVersionedRuntime> jarVersionedRuntimeMap = jarVersionedRuntimes.getVersionedRuntimeMap();
            assertNotNull(jarVersionedRuntimeMap);
            assertEquals(2, jarVersionedRuntimeMap.size()); // 9 and 11

            JarVersionedRuntime jarVersionedRuntime9 = jarVersionedRuntimes.getJarVersionedRuntime(9);
            JarClasses jarClasses9 = jarVersionedRuntime9.getJarClasses();
            // no classes found
            assertNull(jarClasses9.getJdkRevision());

            JarVersionedRuntime jarVersionedRuntime11 = jarVersionedRuntimes.getJarVersionedRuntime(11);
            JarClasses jarClasses11 = jarVersionedRuntime11.getJarClasses();
            // no classes found
            assertNull(jarClasses11.getJdkRevision());
        }, "It should not raise an exception");
    }

    private void assertEntriesContains(List<JarEntry> list, final String entryToFind) {
        assertTrue(list.stream().anyMatch(entry -> entry.getName().equals(entryToFind)));
    }

    private JarVersionedRuntime getBestFitReleaseBySystemProperty(
            JarVersionedRuntimes jarVersionedRuntimes, String value) {
        String key = "maven.shared.jar.test.vm";
        System.setProperty(key, value);
        return jarVersionedRuntimes.getBestFitJarVersionedRuntimeBySystemProperty(key);
    }

    private JarData getJarData(String filename) throws Exception {
        File file = getSampleJar(filename);

        JarAnalyzer jarAnalyzer = new JarAnalyzer(file);
        JarClasses jclass = analyzer.analyze(jarAnalyzer);
        JarData jarData = jarAnalyzer.getJarData();
        assertNotNull(jclass, "JarClasses");
        assertNotNull(jarData, "JarData");

        return jarData;
    }

    private JarClasses getJarClasses(String filename) throws Exception {
        File file = getSampleJar(filename);

        JarClasses jclass = analyzer.analyze(new JarAnalyzer(file));
        assertNotNull(jclass, "JarClasses");

        return jclass;
    }
}
