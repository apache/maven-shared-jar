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

import java.util.Collections;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link JarVersionedRuntimes}.
 */
class JarVersionedRuntimesTest {

    @Test
    void getJarClassesForExistingVersion() {
        JarClasses jarClasses = new JarClasses();
        jarClasses.setJdkRevision("11");
        JarVersionedRuntime versionedRuntime = new JarVersionedRuntime(Collections.emptyList(), jarClasses);
        JarVersionedRuntimes runtimes =
                new JarVersionedRuntimes(new TreeMap<>(Collections.singletonMap(11, versionedRuntime)));

        assertEquals(jarClasses, runtimes.getJarClasses(11));
    }

    /**
     * Exposes issue #135: {@link JarVersionedRuntimes#getJarClasses(Integer)} must not throw
     * a NullPointerException when the requested version is not present in the map.
     */
    @Test
    void getJarClassesForMissingVersionReturnsNull() {
        JarVersionedRuntime versionedRuntime = new JarVersionedRuntime(Collections.emptyList(), new JarClasses());
        JarVersionedRuntimes runtimes =
                new JarVersionedRuntimes(new TreeMap<>(Collections.singletonMap(11, versionedRuntime)));

        assertNull(runtimes.getJarClasses(9));
    }
}
