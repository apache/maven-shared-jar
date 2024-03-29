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
package org.apache.maven.shared.jar.identification.repository;

import javax.inject.Named;
import javax.inject.Singleton;

import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;

/**
 * Empty repository hash search.  Always returns an empty list.
 *
 * Used for local only implementation of a RepositoryHashSearch. It is expected for the users of this library to provide
 * an implementation of a {@link org.apache.maven.shared.jar.identification.repository.RepositoryHashSearch} against a
 * real repository.
 */
@Singleton
@Named("empty")
public class EmptyRepositoryHashSearch implements RepositoryHashSearch {
    public List<Artifact> searchBytecodeHash(String hash) {
        return Collections.emptyList();
    }

    public List<Artifact> searchFileHash(String hash) {
        return Collections.emptyList();
    }
}
