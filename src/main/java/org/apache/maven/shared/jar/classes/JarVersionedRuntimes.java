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

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;

/**
 * Gathered facts about the runtime versions contained within a Multi-Release JAR file.
 *
 * @see org.apache.maven.shared.jar.classes.JarClassesAnalysis#analyze(org.apache.maven.shared.jar.JarAnalyzer)
 */
public class JarVersionedRuntimes {

    /**
     * Information about the JAR's Multi-Release entries
     */
    private NavigableMap<Integer, JarVersionedRuntime> versionedRuntimeMap;

    public JarVersionedRuntimes(NavigableMap<Integer, JarVersionedRuntime> versionedRuntimeMap) {
        this.versionedRuntimeMap = versionedRuntimeMap;
    }

    /**
     * @return the versionedRuntimeMap
     */
    public NavigableMap<Integer, JarVersionedRuntime> getVersionedRuntimeMap() {
        return versionedRuntimeMap;
    }

    public JarVersionedRuntime getJarVersionedRuntime(Integer version) {
        return versionedRuntimeMap.get(version);
    }

    /**
     * Return the JarClasses associated to the release.
     * @param version the release version.
     * @return the JarClasses.
     */
    public JarClasses getJarClasses(Integer version) {
        return versionedRuntimeMap.get(version).getJarClasses();
    }

    /**
     * Get a set of release versions included in the JAR file.
     * @return a set with the Java versions as Strings.
     */
    public Set<Integer> getRuntimeVersionsAsSet() {
        return versionedRuntimeMap.keySet();
    }

    /**
     * Return the highest the JarClasses of the Jdk version that would be executed if they would be executed on a JVM given by the release parameter.
     * @param version the Jdk version number of the executing JVM.
     * @return The fittest JarClasses object matching if found one, or null otherwise.
     * @throws NullPointerException if release is null.
     */
    public JarVersionedRuntime getBestFitJarVersionedRuntime(Integer version) {
        Objects.requireNonNull(version, "version cannot be null");
        Entry<Integer, JarVersionedRuntime> entry = versionedRuntimeMap.floorEntry(version);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    /**
     * Return the highest the JarClasses of the Jdk version that would be executed if they would be executed given a System property.
     * Example values: "java.version.specification" or "java.vm.specification.version".
     * @param key the System property.
     * @return The best fitting JarClasses object matching if found one, or null otherwise.
     * @throws NullPointerException if key is null
     * @throws IllegalStateException if system property value of key is null
     * @throws IllegalStateException if system property cannot be converted to Integer
     */
    public JarVersionedRuntime getBestFitJarVersionedRuntimeBySystemProperty(String key) {
        Objects.requireNonNull(key, "key cannot null");
        String property = System.getProperty(key);
        if (property == null) {
            throw new IllegalStateException("The value of the system property '" + key + "' is null");
        }
        try {
            Integer release = Integer.parseInt(property);
            return getBestFitJarVersionedRuntime(release);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "The value of the system property '" + key + "' [" + property
                            + "] cannot be converted to an Integer",
                    e);
        }
    }
}
