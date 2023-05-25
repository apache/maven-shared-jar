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

import java.util.List;
import java.util.jar.JarEntry;

public class JarVersionedRuntime {

    /**
     * The JAR entries.
     */
    private final List<JarEntry> entries;

    private final JarClasses jarClasses;

    public JarVersionedRuntime(List<JarEntry> entries, JarClasses jarClasses) {
        this.entries = entries;
        this.jarClasses = jarClasses;
    }

    /**
     * @return the entries
     */
    public List<JarEntry> getEntries() {
        return entries;
    }

    /**
     * @return the jarClasses
     */
    public JarClasses getJarClasses() {
        return jarClasses;
    }
}
