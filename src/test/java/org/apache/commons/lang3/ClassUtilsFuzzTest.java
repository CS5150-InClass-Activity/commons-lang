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
 * Jazzer fuzz tests for {@link ClassUtils} name resolution and canonicalization.
 */
class ClassUtilsFuzzTest {

    /**
     * Fuzz target: {@link ClassUtils#getClass(String)} should not throw for unexpected inputs beyond documented types.
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
            // Acceptable outcomes for invalid or unavailable types.
        }
    }

    /**
     * When a class name resolves, the short canonical name derived from the raw string must match the loaded class.
     * This catches mistakes in array dimension handling for JVM descriptors (including whitespace-stripped forms).
     */
    @ValueSource(strings = {"[Ljava.lang.String;", " [ Ljava.lang.String; ", " [ [ Ljava.lang.String; ", "[[Ljava.lang.String;"})
    @FuzzTest(maxDuration = "15s")
    void fuzzShortCanonicalNameConsistentWithGetClass(final String className) {
        if (className == null) {
            return;
        }
        try {
            final Class<?> clazz = ClassUtils.getClass(className);
            final String expected = ClassUtils.getShortCanonicalName(clazz.getCanonicalName());
            assertEquals(expected, ClassUtils.getShortCanonicalName(className));
        } catch (final ClassNotFoundException | IllegalArgumentException ex) {
            // Acceptable for malformed names or unloadable types.
        } catch (final LinkageError ex) {
            // Acceptable when the resolved type fails to load.
        }
    }

    /**
     * Package name from the descriptor string must match the package of the resolved class when loading succeeds.
     */
    @ValueSource(strings = {"[Lorg.apache.commons.lang3.ClassUtils;", " [ Lorg.apache.commons.lang3.ClassUtils; "})
    @FuzzTest(maxDuration = "15s")
    void fuzzPackageCanonicalNameConsistentWithGetClass(final String className) {
        if (className == null) {
            return;
        }
        try {
            final Class<?> clazz = ClassUtils.getClass(className);
            final String expected = clazz.getPackage() != null ? clazz.getPackage().getName() : "";
            assertEquals(expected, ClassUtils.getPackageCanonicalName(className));
        } catch (final ClassNotFoundException | IllegalArgumentException ex) {
            // Acceptable for malformed names or unloadable types.
        } catch (final LinkageError ex) {
            // Acceptable when the resolved type fails to load.
        }
    }
}
