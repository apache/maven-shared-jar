package org.apache.maven.shared.jar.classes;

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

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.maven.shared.jar.AbstractJarAnalyzerTestCase;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Import Visitor Test
 */
public class ImportVisitorTest
    extends AbstractJarAnalyzerTestCase
{
    public void testImportsJxr()
        throws ClassFormatException, IOException
    {
        File jxrjar = getSampleJar( "jxr.jar" );
        String classname = "org/apache/maven/jxr/DirectoryIndexer.class";
        ClassParser classParser = new ClassParser( jxrjar.getAbsolutePath(), classname );
        JavaClass javaClass = classParser.parse();

        ImportVisitor importVisitor = new ImportVisitor( javaClass );
        DescendingVisitor descVisitor = new DescendingVisitor( javaClass, importVisitor );
        javaClass.accept( descVisitor );

        List imports = importVisitor.getImports();
        assertNotNull( "Import List", imports );

        assertNotContainsRegex( "Import List", "[\\[\\)\\(\\;]", imports );

        assertTrue( "imports", imports.contains( "org.apache.maven.jxr.pacman.PackageType" ) );
        assertTrue( "imports", imports.contains( "org.codehaus.plexus.util.IOUtil" ) );
        assertTrue( "imports", imports.contains( "org.apache.oro.text.perl.Perl5Util" ) );
    }

    public void testImportsAnt()
        throws ClassFormatException, IOException
    {
        File jxrjar = getSampleJar( "ant.jar" );
        String classname = "org/apache/tools/ant/Target.class";
        ClassParser classParser = new ClassParser( jxrjar.getAbsolutePath(), classname );
        JavaClass javaClass = classParser.parse();

        ImportVisitor importVisitor = new ImportVisitor( javaClass );
        DescendingVisitor descVisitor = new DescendingVisitor( javaClass, importVisitor );
        javaClass.accept( descVisitor );

        List imports = importVisitor.getImports();
        assertNotNull( "Import List", imports );

        assertNotContainsRegex( "Import List", "[\\[\\)\\(\\;]", imports );

        assertTrue( "imports", imports.contains( "org.apache.tools.ant.Location" ) );
        assertTrue( "imports", imports.contains( "org.apache.tools.ant.Task" ) );
    }
}
