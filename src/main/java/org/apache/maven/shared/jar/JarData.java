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
package org.apache.maven.shared.jar;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

import org.apache.maven.shared.jar.classes.JarClasses;
import org.apache.maven.shared.jar.classes.JarVersionedRuntimes;
import org.apache.maven.shared.jar.identification.JarIdentification;

/**
 * Class that contains details of a single JAR file and it's entries.
 */
public final class JarData {

    private static final Name ATTR_MULTI_RELEASE = new Attributes.Name("Multi-Release");

    /**
     * The JAR file.
     */
    private final File file;

    /**
     * Whether the JAR file is sealed.
     */
    private final boolean aSealed;

    /**
     * Whether the JAR file is Multi-Release.
     */
    private boolean multiRelease;

    /**
     * The hashcode for the entire file's contents.
     */
    private String fileHash;

    /**
     * The hashcode for the file's class data contents.
     */
    private String bytecodeHash;

    /**
     * The JAR's manifest.
     */
    private final Manifest manifest;

    /**
     * Information about the JAR's classes.
     */
    private JarClasses jarClasses;

    /**
     * The JAR entries.
     */
    private final List<JarEntry> entries;

    /**
     * The JAR entries of the root content, when it is a multi-release JAR
     */
    private List<JarEntry> rootEntries;

    /**
     * Information about the JAR's identifying features.
     */
    private JarIdentification jarIdentification;

    /**
     * Information about the JAR's Multi-Release entries
     */
    private JarVersionedRuntimes versionedRuntimes;

    /**
     * Constructor.
     *
     * @param file     the JAR file
     * @param manifest the JAR manifest
     * @param entries  the JAR entries
     */
    public JarData(File file, Manifest manifest, List<JarEntry> entries) {
        this.file = file;

        this.manifest = manifest;

        this.entries = Collections.unmodifiableList(entries);

        this.aSealed = isAttributePresent(Attributes.Name.SEALED);
        this.multiRelease = isAttributePresent(ATTR_MULTI_RELEASE);
    }

    public List<JarEntry> getEntries() {
        return entries;
    }

    public List<JarEntry> getRootEntries() {
        return rootEntries;
    }

    public void setRootEntries(List<JarEntry> rootEntries) {
        this.rootEntries = rootEntries;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public File getFile() {
        return file;
    }

    public boolean isSealed() {
        return aSealed;
    }

    public boolean isMultiRelease() {
        return multiRelease;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setBytecodeHash(String bytecodeHash) {
        this.bytecodeHash = bytecodeHash;
    }

    public String getBytecodeHash() {
        return bytecodeHash;
    }

    public boolean isDebugPresent() {
        return jarClasses.isDebugPresent();
    }

    public void setJarClasses(JarClasses jarClasses) {
        this.jarClasses = jarClasses;
    }

    public int getNumEntries() {
        return entries.size();
    }

    public int getNumRootEntries() {
        return rootEntries.size();
    }

    public int getNumClasses() {
        return jarClasses.getClassNames().size();
    }

    public int getNumPackages() {
        return jarClasses.getPackages().size();
    }

    public String getJdkRevision() {
        return jarClasses.getJdkRevision();
    }

    public void setJarIdentification(JarIdentification jarIdentification) {
        this.jarIdentification = jarIdentification;
    }

    public JarIdentification getJarIdentification() {
        return jarIdentification;
    }

    public JarClasses getJarClasses() {
        return jarClasses;
    }

    public void setVersionedRuntimes(JarVersionedRuntimes versionedRuntimes) {
        this.versionedRuntimes = versionedRuntimes;
    }

    public JarVersionedRuntimes getVersionedRuntimes() {
        return this.versionedRuntimes;
    }

    private boolean isAttributePresent(Attributes.Name attrName) {
        if (this.manifest != null) {
            String sval = this.manifest.getMainAttributes().getValue(attrName);
            return sval != null && "true".equalsIgnoreCase(sval.trim());
        }
        return false;
    }
}
