/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.code_intelligence.jazzer.junit.FuzzTest;

import org.junit.jupiter.params.provider.ValueSource;

/**
 * Jazzer fuzz tests for {@link ClassUtils} string-based name handling.
 * <p>
 * Run with {@code JAZZER_FUZZ=1 mvn -Drat.skip=true -Dtest=ClassUtilsFuzzTest test} for coverage-guided fuzzing.
 * Without {@code JAZZER_FUZZ=1}, seed inputs run once as ordinary JUnit tests.
 * </p>
 */
class ClassUtilsFuzzTest {

    /**
     * Fuzz target: resolve arbitrary strings as Java class names (same idea as a unit test, many inputs).
     */
    @ValueSource(strings = {"[Ljava.lang.String;"})
    @FuzzTest(maxDuration = "10s")
    void fuzzGetClass(final String className) {
        if (className == null) {
            return;
        }
        try {
            ClassUtils.getClass(className);
        } catch (final ClassNotFoundException | IllegalArgumentException ex) {
            // Acceptable for malformed or missing types
        }
    }

    /**
     * Fuzz target: {@link ClassUtils#getShortCanonicalName(String)} must agree with the name derived from
     * {@link Class#forName(String, boolean, ClassLoader)} whenever the class resolves — including JNI descriptors
     * where whitespace was removed (exercises {@code ClassUtils} canonicalization).
     */
    @ValueSource(strings = {
        "[Ljava.lang.String;",
        "[[Ljava.lang.String;",
        "[ [ Ljava.lang.String; ",
        "[I",
        "[[I",
    })
    @FuzzTest(maxDuration = "10s")
    void fuzzShortCanonicalNameConsistentWithForName(final String className) {
        if (className == null) {
            return;
        }
        final String cleaned = StringUtils.deleteWhitespace(className);
        if (StringUtils.isEmpty(cleaned)) {
            return;
        }
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            return;
        }
        try {
            final Class<?> cls = Class.forName(cleaned, false, loader);
            final String expected = ClassUtils.getShortCanonicalName(cls);
            final String actual = ClassUtils.getShortCanonicalName(className);
            assertEquals(expected, actual, () -> "short canonical name mismatch for input: " + className);
        } catch (final ClassNotFoundException | LinkageError | IllegalArgumentException ok) {
            // Not loadable or not a valid descriptor — skip
        }
    }

    /**
     * Fuzz target: {@link ClassUtils#getPackageCanonicalName(String)} must agree with the package of the resolved class,
     * and must stay consistent with stripping the package from {@link ClassUtils#getShortCanonicalName(String)}.
     */
    @ValueSource(strings = {
        "org.apache.commons.lang3.ClassUtils",
        "[Lorg.apache.commons.lang3.ClassUtils;",
        "[[Lorg.apache.commons.lang3.ClassUtils;",
        " [ Lorg.apache.commons.lang3.ClassUtils; ",
    })
    @FuzzTest(maxDuration = "10s")
    void fuzzPackageCanonicalNameConsistentWithForName(final String className) {
        if (className == null) {
            return;
        }
        final String cleaned = StringUtils.deleteWhitespace(className);
        if (StringUtils.isEmpty(cleaned)) {
            return;
        }
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            return;
        }
        try {
            final Class<?> cls = Class.forName(cleaned, false, loader);
            final String expected = ClassUtils.getPackageCanonicalName(cls);
            final String actual = ClassUtils.getPackageCanonicalName(className);
            assertEquals(expected, actual, () -> "package canonical name mismatch for input: " + className);

            final String shortName = ClassUtils.getShortCanonicalName(className);
            final String expectedShort = ClassUtils.getShortCanonicalName(cls);
            assertEquals(expectedShort, shortName);
        } catch (final ClassNotFoundException | LinkageError | IllegalArgumentException ok) {
            // Skip
        }
    }
}
