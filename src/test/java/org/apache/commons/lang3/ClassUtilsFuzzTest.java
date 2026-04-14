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
// package org.apache.commons.lang3;

// import com.code_intelligence.jazzer.junit.FuzzTest;
// import org.junit.jupiter.params.provider.ValueSource;
/*
 * This is a Jazzer fuzz test for ClassUtils.getClass.
 *
 * A fuzz test is like a unit test, but instead of giving the method
 * one hand-written input, Jazzer keeps generating many inputs automatically.
 *
 * The goal here is to see whether ClassUtils.getClass behaves reasonably
 * for many possible class-name strings.
 */
// class ClassUtilsFuzzTest {
//     @ValueSource(strings = {"[Ljava.lang.String;"}) // Give Jazzer one example input to start from.
//     @FuzzTest(maxDuration = "10s") // Tell Jazzer to fuzz this test for up to 10 seconds.
//     void fuzzGetClass(final String className) { // className is the fuzz input.

//         // Defensive check: if the generated input is null, skip this run.
//         if (className == null) {
//             return;
//         }

//         try {
//             // fuzz target
//             // Try to resolve the given string as a Java class name.
//             ClassUtils.getClass(className);

//         } catch (final ClassNotFoundException | IllegalArgumentException ex) {}
//         // These exceptions are treated as acceptable outcomes for bad inputs.
//         //
//         // ClassNotFoundException:
//         //   The class name does not refer to a real class.
//         //
//         // IllegalArgumentException:
//         //   The input format is not valid for this API.
//     }
// }