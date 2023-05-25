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
 * Gathered facts about the releases contained within a Multi-Release JAR file.
 *
 * @see org.apache.maven.shared.jar.classes.JarClassesAnalysis#analyze(org.apache.maven.shared.jar.JarAnalyzer)
 */
public class JarReleases {

    /**
     * Information about the JAR's Multi-Release entries
     */
    private NavigableMap<Integer, JarClasses> releases;

    public JarReleases(NavigableMap<Integer, JarClasses> releases) {
        this.releases = releases;
    }

    /**
     * @return the releases Map
     */
    public NavigableMap<Integer, JarClasses> getReleasesMap() {
        return releases;
    }

    /**
     * Return the JarClasses associated to the release.
     * @param release the release version.
     * @return the JarClasses.
     */
    public JarClasses getJarClasses(Integer release) {
        return releases.get(release);
    }

    /**
     * Get a set of release versions included in the JAR file.
     * @return a set with the Java versions as Strings.
     */
    public Set<Integer> getReleaseVersionsAsSet() {
        return releases.keySet();
    }

    /**
     * Return the highest the JarClasses of the Jdk version that would be executed if they would be executed on a JVM given by the release parameter.
     * @param release the Jdk version number of the executing JVM.
     * @return The fittest JarClasses object matching if found one, or null otherwise.
     * @throws NullPointerException if release is null.
     */
    public JarClasses getBestFitRelease(Integer release) {
        Objects.requireNonNull(release, "The release parameter is null");
        Entry<Integer, JarClasses> entry = releases.floorEntry(release);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    /**
     * Return the highest the JarClasses of the Jdk version that would be executed if they would be executed given a System property.
     * Example values: "java.version.specification" or "java.vm.specification.version".
     * @param key the System property.
     * @return The fittest JarClasses object matching if found one, or null otherwise.
     * @throws NullPointerException if key is null or the System property value is null.
     * @throws IllegalArgumentException if the system property is not convertible to Integer.
     */
    public JarClasses getBestFitRelaseBySystemProperty(String key) {
        Objects.requireNonNull(key, "The value of the System property key is null!");
        String property = System.getProperty(key);
        Objects.requireNonNull(property, "The value of the System property '" + key + "' is null!");
        try {
            Integer release = Integer.parseInt(property);
            return getBestFitRelease(release);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "The value of the System property '" + key + "' [" + property
                            + "] can not be converted to an Integer!",
                    e);
        }
    }
}
