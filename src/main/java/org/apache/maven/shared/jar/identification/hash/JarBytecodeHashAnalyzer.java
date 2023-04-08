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
package org.apache.maven.shared.jar.identification.hash;

import javax.inject.Named;
import javax.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarEntry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.JarData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analyzer that calculates the hash code for the entire file. Can be used to detect an exact copy of the file's class
 * data. Useful to see thru a recompile, recompression, or timestamp change.
 */
@Singleton
@Named("bytecode")
public class JarBytecodeHashAnalyzer implements JarHashAnalyzer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public String computeHash(JarAnalyzer jarAnalyzer) {
        JarData jarData = jarAnalyzer.getJarData();

        String result = jarData.getBytecodeHash();
        if (result == null) {
            List<JarEntry> entries = jarAnalyzer.getClassEntries();

            try {
                for (JarEntry entry : entries) {
                    try (InputStream is = jarAnalyzer.getEntryInputStream(entry)) {
                        result = DigestUtils.sha1Hex(is);
                    }
                }
                jarData.setBytecodeHash(result);
            } catch (IOException e) {
                logger.warn("Unable to calculate the hashcode.", e);
            }
        }
        return result;
    }
}
