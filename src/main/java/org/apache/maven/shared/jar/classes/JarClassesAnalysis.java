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

import javax.inject.Named;
import javax.inject.Singleton;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;
import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.JarData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analyze the classes in a JAR file. This class is thread safe and immutable as it retains no state.
 *
 * Note that you must first create an instance of {@link org.apache.maven.shared.jar.JarAnalyzer} - see its Javadoc for
 * a typical use.
 *
 * @see #analyze(org.apache.maven.shared.jar.JarAnalyzer)
 */
@Singleton
@Named
@SuppressWarnings("checkstyle:MagicNumber")
public class JarClassesAnalysis {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Constant representing the root content of a Multi-Release JAR file, thus outside of
     * any given META-INF/versions/N/... entry.
     */
    private static final Integer ROOT = 0;

    private static final Pattern ENTRY_FILTER_MULTI_RELEASE = Pattern.compile("^META-INF/versions/([1-9]\\d*)/.*$");

    private static final Map<Double, String> JAVA_CLASS_VERSIONS;

    static {
        HashMap<Double, String> aMap = new HashMap<>();
        aMap.put(69.0, "25");
        aMap.put(68.0, "24");
        aMap.put(67.0, "23");
        aMap.put(66.0, "22");
        aMap.put(65.0, "21");
        aMap.put(64.0, "20");
        aMap.put(63.0, "19");
        aMap.put(62.0, "18");
        aMap.put(61.0, "17");
        aMap.put(60.0, "16");
        aMap.put(59.0, "15");
        aMap.put(58.0, "14");
        aMap.put(57.0, "13");
        aMap.put(56.0, "12");
        aMap.put(55.0, "11");
        aMap.put(54.0, "10");
        aMap.put(53.0, "9");
        aMap.put(52.0, "1.8");
        aMap.put(51.0, "1.7");
        aMap.put(50.0, "1.6");
        aMap.put(49.0, "1.5");
        aMap.put(48.0, "1.4");
        aMap.put(47.0, "1.3");
        aMap.put(46.0, "1.2");
        aMap.put(45.3, "1.1");
        JAVA_CLASS_VERSIONS = Collections.unmodifiableMap(aMap);
    }

    /**
     * Analyze a JAR and find any classes and their details. Note that if the provided JAR analyzer has previously
     * analyzed the JAR, the cached results will be returned. You must obtain a new JAR analyzer to the re-read the
     * contents of the file.
     *
     * @param jarAnalyzer the JAR to analyze. This must not yet have been closed.
     * @return the details of the classes found
     */
    public JarClasses analyze(JarAnalyzer jarAnalyzer) {
        JarData jarData = jarAnalyzer.getJarData();
        JarClasses classes = jarData.getJarClasses();
        if (classes == null) {
            if (jarData.isMultiRelease()) {
                classes = analyzeMultiRelease(jarAnalyzer);
            } else {
                classes = analyzeRoot(jarAnalyzer);
            }
        }
        return classes;
    }

    private Integer jarEntryVersion(JarEntry entry) {
        Matcher matcher = ENTRY_FILTER_MULTI_RELEASE.matcher(entry.getName());
        if (matcher.matches()) {
            return Integer.valueOf(matcher.group(1));
        }
        return ROOT;
    }

    private JarClasses analyzeMultiRelease(JarAnalyzer jarAnalyzer) {
        String jarFilename = jarAnalyzer.getFile().getAbsolutePath();

        Map<Integer, List<JarEntry>> mapEntries =
                jarAnalyzer.getEntries().stream().collect(Collectors.groupingBy(this::jarEntryVersion));

        // ordered by increasing Java version
        NavigableMap<Integer, JarVersionedRuntime> runtimeVersionsMap = new TreeMap<>();

        for (Map.Entry<Integer, List<JarEntry>> mapEntry : mapEntries.entrySet()) {
            Integer runtimeVersion = mapEntry.getKey();
            List<JarEntry> runtimeVersionEntryList = mapEntry.getValue();

            List<JarEntry> classList = jarAnalyzer.getClassEntries(runtimeVersionEntryList);

            JarClasses classes = analyze(jarFilename, classList);

            runtimeVersionsMap.put(runtimeVersion, new JarVersionedRuntime(runtimeVersionEntryList, classes));
        }

        JarData jarData = jarAnalyzer.getJarData();

        JarVersionedRuntime rootContentVersionedRuntime = runtimeVersionsMap.remove(ROOT);
        jarData.setRootEntries(rootContentVersionedRuntime.getEntries());
        JarClasses rootJarClasses = rootContentVersionedRuntime.getJarClasses();
        jarData.setJarClasses(rootJarClasses);

        jarData.setVersionedRuntimes(new JarVersionedRuntimes(runtimeVersionsMap));

        return rootJarClasses;
    }

    private JarClasses analyzeRoot(JarAnalyzer jarAnalyzer) {
        String jarFilename = jarAnalyzer.getFile().getAbsolutePath();

        List<JarEntry> classList = jarAnalyzer.getClassEntries();

        JarClasses classes = analyze(jarFilename, classList);

        jarAnalyzer.getJarData().setJarClasses(classes);
        return classes;
    }

    private JarClasses analyze(String jarFilename, List<JarEntry> classList) {
        JarClasses classes = new JarClasses();

        classes.setDebugPresent(false);

        double maxVersion = 0.0;
        double moduleInfoVersion = 0.0;

        for (JarEntry entry : classList) {
            String classname = entry.getName();

            try {
                ClassParser classParser = new ClassParser(jarFilename, classname);

                JavaClass javaClass = classParser.parse();

                String classSignature = javaClass.getClassName();

                if (!classes.isDebugPresent()) {
                    if (hasDebugSymbols(javaClass)) {
                        classes.setDebugPresent(true);
                    }
                }

                double classVersion = javaClass.getMajor();
                if (javaClass.getMinor() > 0) {
                    classVersion = classVersion + javaClass.getMinor() / 10.0;
                }

                if ("module-info".equals(classSignature)) {
                    // ignore the module-info.class for computing the maxVersion, since it will always be >= 9
                    moduleInfoVersion = classVersion;
                } else if (classVersion > maxVersion) {
                    maxVersion = classVersion;
                }

                Method[] methods = javaClass.getMethods();
                for (Method method : methods) {
                    classes.addMethod(classSignature + "." + method.getName() + method.getSignature());
                }

                String classPackageName = javaClass.getPackageName();

                classes.addClassName(classSignature);
                classes.addPackage(classPackageName);

                ImportVisitor importVisitor = new ImportVisitor(javaClass);
                DescendingVisitor descVisitor = new DescendingVisitor(javaClass, importVisitor);
                javaClass.accept(descVisitor);

                classes.addImports(importVisitor.getImports());
            } catch (ClassFormatException e) {
                logger.warn("Unable to process class " + classname + " in JarAnalyzer File " + jarFilename, e);
            } catch (IOException e) {
                logger.warn("Unable to process JarAnalyzer File " + jarFilename, e);
            }
        }

        if (maxVersion == 0.0 && moduleInfoVersion > 0.0) {
            // the one and only class file was module-info.class
            maxVersion = moduleInfoVersion;
        }

        Optional.ofNullable(JAVA_CLASS_VERSIONS.get(maxVersion)).ifPresent(classes::setJdkRevision);

        return classes;
    }

    private boolean hasDebugSymbols(JavaClass javaClass) {
        boolean ret = false;
        Method[] methods = javaClass.getMethods();
        for (Method method : methods) {
            LineNumberTable linenumbers = method.getLineNumberTable();
            if (linenumbers != null && linenumbers.getLength() > 0) {
                ret = true;
                break;
            }
        }
        return ret;
    }
}
