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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.list.SetUniqueList;

/**
 * Gathered facts about the classes within a JAR file.
 *
 * @see org.apache.maven.shared.jar.classes.JarClassesAnalysis#analyze(org.apache.maven.shared.jar.JarAnalyzer)
 */
public class JarClasses {
    /**
     * The list of imports in the classes in the JAR.
     */
    private final List<String> imports;

    /**
     * A list of packages represented by classes in the JAR.
     */
    private final List<String> packages;

    /**
     * A list of the classes that in the JAR.
     */
    private final List<String> classNames;

    /**
     * A list of methods within the classes in the JAR.
     */
    private final List<String> methods;

    /**
     * Whether the JAR contains any code with debug information. If there is a mix of debug and release code, this will
     * still be true.
     */
    private boolean isDebugPresent;

    /**
     * The highest Java class version available in any class files. While the JAR may work on earlier JVMs if particular
     * classes are not used, this is the minimum JVM version that guarantees compatibility.
     */
    private String maxJavaClassVersion;

    /**
     * Constructor to create an empty instance.
     */
    public JarClasses() {
        // Unique list decorators are used to ensure natural ordering is retained, the list interface is availble, and
        // that duplicates are not entered.
        imports = SetUniqueList.setUniqueList(new ArrayList<>());
        packages = SetUniqueList.setUniqueList(new ArrayList<>());
        classNames = SetUniqueList.setUniqueList(new ArrayList<>());
        methods = SetUniqueList.setUniqueList(new ArrayList<>());
    }

    /**
     * Add a discovered class to the record.
     *
     * @param name the name of the class
     */
    public void addClassName(String name) {
        this.classNames.add(name);
    }

    /**
     * Add a discovered package to the record.
     *
     * @param name the name of the package
     */
    public void addPackage(String name) {
        this.packages.add(name);
    }

    /**
     * Add a discovered method to the record.
     *
     * @param name the name of the method
     */
    public void addMethod(String name) {
        this.methods.add(name);
    }

    /**
     * Add a list of discovered imports to the record.
     *
     * @param imports the imports to add. Each item should be a String to avoid down the line ClassCastExceptions.
     */
    public void addImports(List<String> imports) {
        this.imports.addAll(imports);
    }

    public List<String> getImports() {
        return Collections.unmodifiableList(imports);
    }

    public List<String> getClassNames() {
        return Collections.unmodifiableList(classNames);
    }

    public List<String> getPackages() {
        return Collections.unmodifiableList(packages);
    }

    public boolean isDebugPresent() {
        return isDebugPresent;
    }

    public void setDebugPresent(boolean hasDebugSymbols) {
        this.isDebugPresent = hasDebugSymbols;
    }

    /**
     * Get the highest Java class version available in any class files. While the JAR may work on earlier JVMs if particular
     * classes are not used, this is the minimum JVM version that guarantees compatibility.
     *
     * @return the highest Java class version available in any class files, or null if not known.
     *
     * @since 3.2.1
     */
    public String getMaxJavaClassVersion() {
        return maxJavaClassVersion;
    }

    /**
     * Set the highest Java class version available in any class files. While the JAR may work on earlier JVMs if particular
     * classes are not used, this is the minimum JVM version that guarantees compatibility.
     *
     * @param maxJavaClassVersion the highest Java class version available in any class files.
     *
     * @since 3.2.1
     */
    public void setMaxJavaClassVersion(String maxJavaClassVersion) {
        this.maxJavaClassVersion = maxJavaClassVersion;
    }

    /**
     * The JDK revision of the classes in the JAR.
     * @return the JDK revision of the classes in the JAR, or null if not known.
     * @deprecated since 3.2.1, use {@link #getMaxJavaClassVersion()} instead, which is more accurate name.
     */
    @Deprecated
    public String getJdkRevision() {
        return getMaxJavaClassVersion();
    }

    /**
     * Set the JDK revision of the classes in the JAR.
     * @param jdkRevision the JDK revision of the classes in the JAR.
     * @deprecated since 3.2.1, use {@link #setMaxJavaClassVersion(String)} instead, which is more accurate name.
     */
    @Deprecated
    public void setJdkRevision(String jdkRevision) {
        setMaxJavaClassVersion(jdkRevision);
    }

    public List<String> getMethods() {
        return Collections.unmodifiableList(methods);
    }
}
