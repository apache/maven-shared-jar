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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.maven.shared.jar.AbstractJarAnalyzerTestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Import Visitor Test
 */
class ImportVisitorTest extends AbstractJarAnalyzerTestCase {

    @Test
    void testImportsJxr() throws ClassFormatException, IOException {
        File jxrjar = getSampleJar("jxr.jar");
        String classname = "org/apache/maven/jxr/DirectoryIndexer.class";
        ClassParser classParser = new ClassParser(jxrjar.getAbsolutePath(), classname);
        JavaClass javaClass = classParser.parse();

        ImportVisitor importVisitor = new ImportVisitor(javaClass);
        DescendingVisitor descVisitor = new DescendingVisitor(javaClass, importVisitor);
        javaClass.accept(descVisitor);

        List<String> imports = importVisitor.getImports();
        assertNotNull(imports, "Import List");

        assertNotContainsRegex("Import List", "[\\[\\)\\(\\;]", imports);

        assertTrue(imports.contains("org.apache.maven.jxr.pacman.PackageType"), "imports");
        assertTrue(imports.contains("org.apache.oro.text.perl.Perl5Util"), "imports");
    }

    @Test
    void testImportsAnt() throws ClassFormatException, IOException {
        File jxrjar = getSampleJar("ant.jar");
        String classname = "org/apache/tools/ant/Target.class";
        ClassParser classParser = new ClassParser(jxrjar.getAbsolutePath(), classname);
        JavaClass javaClass = classParser.parse();

        ImportVisitor importVisitor = new ImportVisitor(javaClass);
        DescendingVisitor descVisitor = new DescendingVisitor(javaClass, importVisitor);
        javaClass.accept(descVisitor);

        List<String> imports = importVisitor.getImports();
        assertNotNull(imports, "Import List");

        assertNotContainsRegex("Import List", "[\\[\\)\\(\\;]", imports);

        assertTrue(imports.contains("org.apache.tools.ant.Location"), "imports");
        assertTrue(imports.contains("org.apache.tools.ant.Task"), "imports");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "helloworld-1.1.jar",
                "helloworld-1.2.jar",
                "helloworld-1.3.jar",
                "helloworld-1.4.jar",
                "helloworld-1.5.jar",
                "helloworld-1.6.jar",
                "helloworld-1.7.jar",
                "helloworld-1.8.jar",
                "helloworld-9.jar",
                "helloworld-10.jar",
                "helloworld-11.jar",
                "helloworld-12.jar",
                "helloworld-13.jar",
                "helloworld-14.jar",
                "helloworld-15.jar",
                "helloworld-16.jar",
                "helloworld-17.jar",
                "helloworld-18.jar",
                "helloworld-19.jar",
                "helloworld-20.jar",
                "helloworld-21.jar",
                "helloworld-22.jar",
                "helloworld-23.jar",
                "helloworld-24.jar",
                "helloworld-25.jar"
            })
    void testImportByJDKVersion(String jarName) throws IOException, ClassFormatException {
        File jarFile = getSampleJar(jarName);

        ClassParser classParser = new ClassParser(jarFile.getAbsolutePath(), "net/test/HelloWorld.class");
        JavaClass javaClass = classParser.parse();

        ImportVisitor importVisitor = new ImportVisitor(javaClass);
        DescendingVisitor descVisitor = new DescendingVisitor(javaClass, importVisitor);
        javaClass.accept(descVisitor);

        List<String> imports = importVisitor.getImports();
        assertNotNull(imports, "Import List");
        assertTrue(imports.size() >= 4);
    }
}
