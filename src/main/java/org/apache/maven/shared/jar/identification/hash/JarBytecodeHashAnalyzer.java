package org.apache.maven.shared.jar.identification.hash;

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

import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.JarData;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.digest.DigesterException;
import org.codehaus.plexus.digest.StreamingDigester;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarEntry;

/**
 * Analyzer that calculates the hash code for the entire file. Can be used to detect an exact copy of the file's class
 * data. Useful to see thru a recompile, recompression, or timestamp change.
 * <p/>
 * If you are not using Plexus, you must call {@link #setDigester(org.codehaus.plexus.digest.StreamingDigester)} before
 * use
 */
@Component( role = JarHashAnalyzer.class, hint = "bytecode" )
public class JarBytecodeHashAnalyzer
    extends AbstractLogEnabled
    implements JarHashAnalyzer
{
    /**
     * The streaming digester to use for computing the hash. Under Plexus, the default is SHA-1.
     */
    @Requirement( hint = "sha1" )
    private StreamingDigester digester;

    public String computeHash( JarAnalyzer jarAnalyzer )
    {
        JarData jarData = jarAnalyzer.getJarData();

        String result = jarData.getBytecodeHash();
        if ( result == null )
        {
            List<JarEntry> entries = jarAnalyzer.getClassEntries();

            try
            {
                digester.reset();
                for ( JarEntry entry : entries )
                {
                    try ( InputStream is = jarAnalyzer.getEntryInputStream( entry ) )
                    {
                        digester.update( is );
                    }
                }
                result = digester.calc();
                jarData.setBytecodeHash( result );
            }
            catch ( DigesterException | IOException e )
            {
                getLogger().warn( "Unable to calculate the hashcode.", e );
            }
        }
        return result;
    }

    public void setDigester( StreamingDigester digester )
    {
        this.digester = digester;
    }
}
