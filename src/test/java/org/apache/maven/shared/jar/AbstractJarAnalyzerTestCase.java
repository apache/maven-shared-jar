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
package org.apache.maven.shared.jar;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Abstract JarAnalyzer TestCase
 */
public abstract class AbstractJarAnalyzerTestCase {

    protected File getSampleJar(String filename) throws UnsupportedEncodingException {
        String path = getClass().getResource("/jars/" + filename).getPath();
        // URLDecoder.decode necessary for JDK 1.5+, where spaces are escaped to %20
        return new File(URLDecoder.decode(path, UTF_8.name()));
    }

    protected void assertNotContainsRegex(String msg, String regex, Collection<String> coll) {
        List<String> failures = new ArrayList<>();
        Pattern pat = Pattern.compile(regex);
        for (String value : coll) {
            Matcher mat = pat.matcher(value);
            if (mat.find()) {
                failures.add(value);
            }
        }

        if (!failures.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(msg)
                    .append(" collection has illegal regex \"")
                    .append(regex)
                    .append("\"");
            for (String failure : failures) {
                sb.append("\n   - \"").append(failure).append("\"");
            }
            fail(sb.toString());
        }
    }
}
