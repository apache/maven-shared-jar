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
import java.security.MessageDigest;
import java.util.List;
import java.util.jar.JarEntry;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.JarData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analyzer that calculates the hash code for the entire file. Can be used to detect an exact copy of the file's class
 * data. Useful to see through a recompile, recompression, or timestamp change.
 */
@Singleton
@Named("bytecode")
public class JarBytecodeHashAnalyzer implements JarHashAnalyzer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Computes the bytecode hash while retaining the historical null-on-error behaviour.
     *
     * @param jarAnalyzer the JAR analyzer to use
     * @return the hash, or {@code null} when the hash cannot be computed
     * @deprecated use {@link #computeHashWithIOException(JarAnalyzer)} when the failure must be handled by the caller
     */
    @Deprecated
    @Override
    public String computeHash(JarAnalyzer jarAnalyzer) {
        try {
            return computeHashWithIOException(jarAnalyzer);
        } catch (IOException e) {
            logger.warn("Unable to calculate the hashcode.", e);
            return null;
        }
    }

    /**
     * Computes the bytecode hash and reports I/O failures to the caller.
     *
     * @param jarAnalyzer the JAR analyzer to use
     * @return the computed hash
     * @throws IOException if a class entry cannot be read
     */
    public String computeHashWithIOException(JarAnalyzer jarAnalyzer) throws IOException {
        JarData jarData = jarAnalyzer.getJarData();

        String result = jarData.getBytecodeHash();
        if (result == null) {
            List<JarEntry> entries = jarAnalyzer.getClassEntries();

            MessageDigest sha1 = DigestUtils.getSha1Digest();
            for (JarEntry entry : entries) {
                try (InputStream is = jarAnalyzer.getEntryInputStream(entry)) {
                    DigestUtils.updateDigest(sha1, is);
                }
            }
            result = Hex.encodeHexString(sha1.digest());
            jarData.setBytecodeHash(result);
        }
        return result;
    }
}
