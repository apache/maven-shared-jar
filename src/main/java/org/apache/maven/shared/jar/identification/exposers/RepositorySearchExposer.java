package org.apache.maven.shared.jar.identification.exposers;

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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.identification.JarIdentification;
import org.apache.maven.shared.jar.identification.JarIdentificationExposer;
import org.apache.maven.shared.jar.identification.hash.JarHashAnalyzer;
import org.apache.maven.shared.jar.identification.repository.RepositoryHashSearch;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Exposer that examines a Maven repository for identical files to the JAR being analyzed. It will look for both
 * identical files, and files with identical classes.
 */
@Singleton
@Named( "repositorySearch" )
public class RepositorySearchExposer
    implements JarIdentificationExposer
{
    /**
     * The repository searcher to use.
     *
     * @todo this currently only provides for the 'empty' repository search, which isn't very useful
     */
    private final RepositoryHashSearch repositoryHashSearch;

    /**
     * The hash analyzer for the entire file.
     */
    private final JarHashAnalyzer fileHashAnalyzer;

    /**
     * The hash analyzer for the file's bytecode.
     */
    private final JarHashAnalyzer bytecodeHashAnalyzer;

    @Inject
    public RepositorySearchExposer( RepositoryHashSearch repositoryHashSearch,
                                    @Named( "file" ) JarHashAnalyzer fileHashAnalyzer,
                                    @Named( "bytecode" ) JarHashAnalyzer bytecodeHashAnalyzer )
    {
        this.repositoryHashSearch = requireNonNull( repositoryHashSearch );
        this.fileHashAnalyzer = requireNonNull( fileHashAnalyzer );
        this.bytecodeHashAnalyzer = requireNonNull( bytecodeHashAnalyzer );
    }

    @Override
    public void expose( JarIdentification identification, JarAnalyzer jarAnalyzer )
    {
        List<Artifact> repohits = new ArrayList<>();

        String hash = fileHashAnalyzer.computeHash( jarAnalyzer );
        if ( hash != null )
        {
            repohits.addAll( repositoryHashSearch.searchFileHash( hash ) );
        }

        String bytecodehash = bytecodeHashAnalyzer.computeHash( jarAnalyzer );
        if ( bytecodehash != null )
        {
            repohits.addAll( repositoryHashSearch.searchBytecodeHash( bytecodehash ) );
        }

        // Found hits in the repository.
        for ( Artifact artifact : repohits )
        {
            identification.addAndSetGroupId( artifact.getGroupId() );
            identification.addAndSetArtifactId( artifact.getArtifactId() );
            identification.addAndSetVersion( artifact.getVersion() );
        }
    }
}
