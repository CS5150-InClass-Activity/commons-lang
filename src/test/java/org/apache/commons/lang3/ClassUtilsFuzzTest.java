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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.code_intelligence.jazzer.junit.FuzzTest;
import org.junit.jupiter.params.provider.ValueSource;
/*
 * This is a Jazzer fuzz test for ClassUtils.getClass.
 *
 * A fuzz test is like a unit test, but instead of giving the method
 * one hand-written input, Jazzer keeps generating many inputs automatically.
 *
 * The goal here is to see whether ClassUtils.getClass behaves reasonably
 * for many possible class-name strings.
 */
class ClassUtilsFuzzTest {

    private static final Method GET_CANONICAL_NAME_METHOD = getCanonicalNameMethod();

    private static Method getCanonicalNameMethod() {
        try {
            final Method method = ClassUtils.class.getDeclaredMethod("getCanonicalName", String.class);
            method.setAccessible(true);
            return method;
        } catch (final ReflectiveOperationException ex) {
            throw new IllegalStateException("Unable to access ClassUtils#getCanonicalName(String)", ex);
        }
    }

    private static int countLeading(final String input, final char expected) {
        int count = 0;
        while (count < input.length() && input.charAt(count) == expected) {
            count++;
        }
        return count;
    }

    private static int countArraySuffixDimensions(final String canonicalName) {
        int dimensions = 0;
        int index = canonicalName.length();
        while (index >= 2 && canonicalName.charAt(index - 2) == '[' && canonicalName.charAt(index - 1) == ']') {
            dimensions++;
            index -= 2;
        }
        return dimensions;
    }

    private static boolean isValidJvmArrayDescriptor(final String normalized) {
        if (normalized == null || normalized.isEmpty() || normalized.charAt(0) != '[') {
            return false;
        }
        final int dimensions = countLeading(normalized, '[');
        if (dimensions >= normalized.length()) {
            return false;
        }
        final String componentDescriptor = normalized.substring(dimensions);
        if (componentDescriptor.length() == 1) {
            return "BCDFIJSZV".indexOf(componentDescriptor.charAt(0)) >= 0;
        }
        return componentDescriptor.length() >= 3
                && componentDescriptor.charAt(0) == 'L'
                && componentDescriptor.charAt(componentDescriptor.length() - 1) == ';'
                && componentDescriptor.indexOf('[', 1) < 0;
    }

    @ValueSource(strings = {"[Ljava.lang.String;"})
    @FuzzTest(maxDuration = "10s")
    void fuzzGetClass(final String className) {
        if (className == null) {
            return;
        }
        try {
            ClassUtils.getClass(className);
        } catch (final ClassNotFoundException | IllegalArgumentException ex) {
            // Expected for malformed/non-existing class names.
        }
    }

    @ValueSource(strings = {" [ [ Ljava.lang.String; ", "\t[[[I\n", "[[Ljava.util.Map$Entry;"})
    @FuzzTest(maxDuration = "10s")
    void fuzzGetCanonicalNameArrayDimensions(final String className) throws Throwable {
        if (className == null) {
            return;
        }
        final String normalized = StringUtils.deleteWhitespace(className);
        if (normalized == null || normalized.isEmpty()) {
            return;
        }
        final int descriptorDimensions = countLeading(normalized, '[');
        try {
            final String canonicalName = (String) GET_CANONICAL_NAME_METHOD.invoke(null, className);
            if (descriptorDimensions > 0 && isValidJvmArrayDescriptor(normalized)) {
                // For descriptor arrays, output dimensions should match descriptor leading '[' count.
                assertEquals(descriptorDimensions, countArraySuffixDimensions(canonicalName));
            }
        } catch (final InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof IllegalArgumentException) {
                return;
            }
            throw cause;
        }
    }

    @ValueSource(strings = {"x", " [ Ljava.lang.String; ", "[[Lorg.apache.commons.lang3.ClassUtils;"})
    @FuzzTest(maxDuration = "10s")
    void fuzzGetPackageCanonicalName(final String className) {
        if (className == null) {
            return;
        }
        try {
            ClassUtils.getPackageCanonicalName(className);
        } catch (final IllegalArgumentException ex) {
            // Malformed class names are acceptable; fuzzing focuses on unexpected runtime failures.
        }
    }
}