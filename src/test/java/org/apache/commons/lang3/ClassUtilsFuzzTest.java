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
    @ValueSource(strings = {"[Ljava.lang.String;"}) // Give Jazzer one example input to start from.
    @FuzzTest(maxDuration = "10s") // Tell Jazzer to fuzz this test for up to 10 seconds.
    void fuzzGetClass(final String className) { // className is the fuzz input.

        // Defensive check: if the generated input is null, skip this run.
        if (className == null) {
            return;
        }

        try {
            // fuzz target
            // Try to resolve the given string as a Java class name.
            ClassUtils.getClass(className);

        } catch (final ClassNotFoundException | IllegalArgumentException ex) {}
        // These exceptions are treated as acceptable outcomes for bad inputs.
        //
        // ClassNotFoundException:
        //   The class name does not refer to a real class.
        //
        // IllegalArgumentException:
        //   The input format is not valid for this API.
    }

    /**
     * Fuzz test for ClassUtils.getCanonicalName(Object, String).
     *
     * Bug: When the input class name string contains whitespace within an array
     * descriptor (e.g., "[ [I"), the hasWhitespaceDescriptorArray logic causes
     * one array dimension to be dropped from the canonical name. For example,
     * "[ [I" (int[][]) is incorrectly returned as "int[]" instead of "int[][]".
     *
     * This test asserts that adding whitespace to a valid array descriptor
     * must not change the result, exposing the dimension-dropping bug.
     */
    @ValueSource(strings = {"[[I", "[ [I", "[[ I", "[ [ I", "[[Ljava.lang.String;"})
    @FuzzTest(maxDuration = "10s")
    void fuzzGetCanonicalName(final String className) {
        if (className == null) {
            return;
        }

        try {
            // Fuzz target: the private getCanonicalName(String) is called indirectly
            // through the public getShortCanonicalName(String).
            final String result = ClassUtils.getShortCanonicalName(className);

            // Invariant: whitespace should not affect the result.
            // Removing all whitespace and calling again should give the same answer.
            final String stripped = className.replaceAll("\\s", "");
            final String resultNoWhitespace = ClassUtils.getShortCanonicalName(stripped);
            if (!result.equals(resultNoWhitespace)) {
                throw new AssertionError(
                    "Whitespace changed result: input='" + className +
                    "' result='" + result + "' vs stripped='" + stripped +
                    "' result='" + resultNoWhitespace + "'");
            }
        } catch (final IllegalArgumentException ex) {
            // Expected for malformed class names.
        }
    }

    /**
     * Fuzz test for ClassUtils.getPackageCanonicalName(String).
     *
     * Bug: When the canonical name contains a null character (\0), the method
     * computes an incorrect substring index via:
     *   canonicalName.lastIndexOf('.', nullIdx) - nullIdx
     * This can produce a negative index, causing StringIndexOutOfBoundsException.
     */
    @ValueSource(strings = {"java.lang.String", "[Ljava.lang.String;", "java.lang\0.String"})
    @FuzzTest(maxDuration = "10s")
    void fuzzGetPackageCanonicalName(final String className) {
        if (className == null) {
            return;
        }

        try {
            // Fuzz target
            ClassUtils.getPackageCanonicalName(className);
        } catch (final IllegalArgumentException ex) {
            // Expected for malformed class names.
        }
    }
}
