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

class ClassUtilsFuzzTest {

    @ValueSource(strings = {" [ Ljava.lang.String; "})
    @FuzzTest(maxDuration = "10s")
    void fuzzGetClass(final String className) {
        if (className == null) {
            return;
        }
        try {
            ClassUtils.getClass(className);
        } catch (final ClassNotFoundException | IllegalArgumentException ex) {
            // Invalid or non-existent type names are valid fuzz outcomes.
        }
    }

    @ValueSource(strings = {"[[Ljava.lang.String;", "[I", "java.lang.String"})
    @FuzzTest(maxDuration = "10s")
    void fuzzGetPackageCanonicalName(final String className) {
        try {
            ClassUtils.getPackageCanonicalName(className);
        } catch (final IllegalArgumentException ex) {
            // Invalid class descriptors are valid fuzz outcomes.
        }
    }
}