/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.tests.arguments

import org.jetbrains.kotlin.buildtools.api.arguments.ExperimentalCompilerArgument
import org.jetbrains.kotlin.buildtools.api.arguments.JvmCompilerArguments.Companion.X_STRING_CONCAT
import org.jetbrains.kotlin.buildtools.api.arguments.enums.StringConcatMode
import org.jetbrains.kotlin.buildtools.api.jvm.JvmPlatformToolchain.Companion.jvm
import org.jetbrains.kotlin.buildtools.tests.CompilerExecutionStrategyConfiguration
import org.jetbrains.kotlin.buildtools.tests.compilation.BaseCompilationTest
import org.jetbrains.kotlin.buildtools.tests.compilation.model.DefaultStrategyAgnosticCompilationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import java.nio.file.Paths

@OptIn(ExperimentalCompilerArgument::class)
internal class StringConcatModeConversionTest : BaseCompilationTest() {

    @DisplayName("Test Xstring-concat is converted to a compiler argument correctly")
    @DefaultStrategyAgnosticCompilationTest
    fun testStringConcatModeToArgumentString(strategyConfig: CompilerExecutionStrategyConfiguration) {
        val toolchain = strategyConfig.first
        val stringConcatMode = StringConcatMode.INDY
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).apply {
            compilerArguments[X_STRING_CONCAT] = stringConcatMode
        }.build()

        val argumentStrings = jvmOperation.compilerArguments.toArgumentStrings()
        val valueString = argumentStrings.first { it.startsWith("-Xstring-concat") }.removePrefix("-Xstring-concat=")

        assertEquals(stringConcatMode.stringValue, valueString)
    }

    @DisplayName("Test that Xstring-concat is not set by default")
    @DefaultStrategyAgnosticCompilationTest
    fun testStringConcatModeNotSetByDefault(strategyConfig: CompilerExecutionStrategyConfiguration) {
        val toolchain = strategyConfig.first
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).build()

        val argumentStrings = jvmOperation.compilerArguments.toArgumentStrings()
        val valueString = argumentStrings.firstOrNull { it.startsWith("-Xstring-concat") }?.removePrefix("-Xstring-concat=")

        assertEquals(null, valueString)
    }

    @DisplayName("Test Xstring-concat is set and retrieved correctly")
    @DefaultStrategyAgnosticCompilationTest
    fun testStringConcatModeGetWhenSet(strategyConfig: CompilerExecutionStrategyConfiguration) {
        val toolchain = strategyConfig.first
        val expectedStringConcatMode = StringConcatMode.INDY_WITH_CONSTANTS
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).apply {
            compilerArguments[X_STRING_CONCAT] = expectedStringConcatMode
        }.build()

        val actualStringConcatMode = jvmOperation.compilerArguments[X_STRING_CONCAT]

        assertEquals(expectedStringConcatMode, actualStringConcatMode)
    }

    @DisplayName("Test Xstring-concat is retrieved correctly when it is not set")
    @DefaultStrategyAgnosticCompilationTest
    fun testStringConcatModeGetWhenNull(strategyConfig: CompilerExecutionStrategyConfiguration) {
        val toolchain = strategyConfig.first
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).build()

        val stringConcatMode = jvmOperation.compilerArguments[X_STRING_CONCAT]

        assertEquals(null, stringConcatMode)
    }
}
