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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.jar.JarEntry;

import org.apache.maven.api.di.Inject;
import org.apache.maven.api.di.Named;
import org.apache.maven.api.di.Singleton;
import org.apache.maven.api.model.Model;
import org.apache.maven.api.model.Organization;
import org.apache.maven.api.services.xml.ModelXmlFactory;
import org.apache.maven.api.services.xml.XmlReaderException;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.identification.JarIdentification;
import org.apache.maven.shared.jar.identification.JarIdentificationExposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Exposer that examines a JAR file for any embedded Maven metadata for identification.
 */
@Singleton
@Named("embeddedMavenModel")
public class EmbeddedMavenModelExposer implements JarIdentificationExposer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ModelXmlFactory modelXmlFactory;

    @Inject
    public EmbeddedMavenModelExposer(ModelXmlFactory modelXmlFactory) {
        this.modelXmlFactory = requireNonNull(modelXmlFactory);
    }

    @Override
    public void expose(JarIdentification identification, JarAnalyzer jarAnalyzer) {
        List<JarEntry> entries = jarAnalyzer.getMavenPomEntries();
        if (entries.isEmpty()) {
            return;
        }

        if (entries.size() > 1) {
            logger.warn("More than one Maven model entry was found in the JAR, using only the first of: " + entries);
        }

        JarEntry pom = entries.get(0);
        try (InputStream is = jarAnalyzer.getEntryInputStream(pom);
                InputStreamReader isreader = new InputStreamReader(is)) {
            Model model = modelXmlFactory.read(isreader, true);

            if (model.getParent() != null) {
                // use parent values only if project values not exists
                if (model.getGroupId() == null) {
                    identification.addAndSetGroupId(model.getParent().getGroupId());
                }
                if (model.getVersion() == null) {
                    identification.addAndSetVersion(model.getParent().getVersion());
                }
            }

            identification.addAndSetGroupId(model.getGroupId());
            identification.addAndSetArtifactId(model.getArtifactId());
            identification.addAndSetVersion(model.getVersion());
            identification.addAndSetName(model.getName());

            // TODO: suboptimal - we are reproducing Maven's built in default
            if (model.getName() == null) {
                identification.addAndSetName(model.getArtifactId());
            }

            Organization org = model.getOrganization();
            if (org != null) {
                identification.addAndSetVendor(org.getName());
            }
        } catch (IOException e) {
            logger.error("Unable to read model " + pom.getName() + " in " + jarAnalyzer.getFile() + ".", e);
        } catch (XmlReaderException e) {
            logger.error("Unable to parse model " + pom.getName() + " in " + jarAnalyzer.getFile() + ".", e);
        }
    }
}
