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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.classes.JarClasses;
import org.apache.maven.shared.jar.classes.JarClassesAnalysis;
import org.apache.maven.shared.jar.identification.JarIdentification;
import org.apache.maven.shared.jar.identification.JarIdentificationExposer;

import static java.util.Objects.requireNonNull;

/**
 * Exposer that examines a JAR file to derive Maven metadata from the classes in a JAR. It will currently identify
 * potential group IDs from the class packages.
 */
@Singleton
@Named("jarClasses")
public class JarClassesExposer implements JarIdentificationExposer {
    private final JarClassesAnalysis analyzer;

    @Inject
    public JarClassesExposer(JarClassesAnalysis analyzer) {
        this.analyzer = requireNonNull(analyzer);
    }

    @Override
    public void expose(JarIdentification identification, JarAnalyzer jarAnalyzer) {
        JarClasses jarclasses = analyzer.analyze(jarAnalyzer);

        for (String packagename : jarclasses.getPackages()) {
            identification.addGroupId(packagename);
        }
    }
}
