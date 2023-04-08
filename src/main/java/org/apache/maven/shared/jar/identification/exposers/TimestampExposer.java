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

import javax.inject.Named;
import javax.inject.Singleton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.identification.JarIdentification;
import org.apache.maven.shared.jar.identification.JarIdentificationExposer;
import org.codehaus.plexus.util.StringUtils;

/**
 * Exposer that examines a a JAR and uses the most recent timestamp as a potential version.
 */
@Singleton
@Named("timestamp")
public class TimestampExposer implements JarIdentificationExposer {
    @Override
    public void expose(JarIdentification identification, JarAnalyzer jarAnalyzer) {
        List<JarEntry> entries = jarAnalyzer.getEntries();
        SimpleDateFormat tsformat = new SimpleDateFormat("yyyyMMdd", Locale.US); // $NON-NLS-1$
        Bag<String> timestamps = new HashBag<>();
        for (JarEntry entry : entries) {
            long time = entry.getTime();
            String timestamp = tsformat.format(new Date(time));
            timestamps.add(timestamp);
        }

        String ts = "";
        int tsmax = 0;
        for (String timestamp : timestamps) {
            int count = timestamps.getCount(timestamp);
            if (count > tsmax) {
                ts = timestamp;
                tsmax = count;
            }
        }

        if (StringUtils.isNotEmpty(ts)) {
            identification.addVersion(ts);
        }
    }
}
