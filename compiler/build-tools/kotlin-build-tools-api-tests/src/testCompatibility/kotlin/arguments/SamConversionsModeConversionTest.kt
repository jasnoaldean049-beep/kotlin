/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.tests.arguments

import org.jetbrains.kotlin.buildtools.api.arguments.ExperimentalCompilerArgument
import org.jetbrains.kotlin.buildtools.api.arguments.JvmCompilerArguments.Companion.X_SAM_CONVERSIONS
import org.jetbrains.kotlin.buildtools.api.arguments.enums.SamConversionsMode
import org.jetbrains.kotlin.buildtools.api.jvm.JvmPlatformToolchain.Companion.jvm
import org.jetbrains.kotlin.buildtools.tests.CompilerExecutionStrategyConfiguration
import org.jetbrains.kotlin.buildtools.tests.compilation.BaseCompilationTest
import org.jetbrains.kotlin.buildtools.tests.compilation.model.DefaultStrategyAgnosticCompilationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import java.nio.file.Paths

@OptIn(ExperimentalCompilerArgument::class)
internal class SamConversionsModeConversionTest : BaseCompilationTest() {

    @DisplayName("Test Xsam-conversions is converted to a compiler argument correctly")
    @DefaultStrategyAgnosticCompilationTest
    fun testSamConversionsModeToArgumentString(strategyConfig: CompilerExecutionStrategyConfiguration) {
        val toolchain = strategyConfig.first
        val samConversionsMode = SamConversionsMode.CLASS
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).apply {
            compilerArguments[X_SAM_CONVERSIONS] = samConversionsMode
        }.build()

        val argumentStrings = jvmOperation.compilerArguments.toArgumentStrings()
        val valueString = argumentStrings.first { it.startsWith("-Xsam-conversions") }.removePrefix("-Xsam-conversions=")

        assertEquals(samConversionsMode.stringValue, valueString)
    }

    @DisplayName("Test that Xsam-conversions defaults to INDY")
    @DefaultStrategyAgnosticCompilationTest
    fun testSamConversionsModeDefaultValue(strategyConfig: CompilerExecutionStrategyConfiguration) {
        val toolchain = strategyConfig.first
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).build()

        val argumentStrings = jvmOperation.compilerArguments.toArgumentStrings()
        val valueString = argumentStrings.firstOrNull { it.startsWith("-Xsam-conversions") }?.removePrefix("-Xsam-conversions=")

        // see: https://github.com/JetBrains/kotlin/blob/8757a28e1d65b567a9310e05d844ea318e77e58c/build-common/src/org/jetbrains/kotlin/compilerRunner/argumentsToStrings.kt#L40-L43
        assertEquals(null, valueString)
    }

    @DisplayName("Test Xsam-conversions is set and retrieved correctly")
    @DefaultStrategyAgnosticCompilationTest
    fun testSamConversionsModeGetWhenSet(strategyConfig: CompilerExecutionStrategyConfiguration) {
        val toolchain = strategyConfig.first
        val expectedSamConversionsMode = SamConversionsMode.CLASS
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).apply {
            compilerArguments[X_SAM_CONVERSIONS] = expectedSamConversionsMode
        }.build()

        val actualSamConversionsMode = jvmOperation.compilerArguments[X_SAM_CONVERSIONS]

        assertEquals(expectedSamConversionsMode, actualSamConversionsMode)
    }

    @DisplayName("Test Xsam-conversions returns default value INDY when not explicitly set")
    @DefaultStrategyAgnosticCompilationTest
    fun testSamConversionsModeGetDefault(strategyConfig: CompilerExecutionStrategyConfiguration) {
        val toolchain = strategyConfig.first
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).build()

        val samConversionsMode = jvmOperation.compilerArguments[X_SAM_CONVERSIONS]

        assertEquals(SamConversionsMode.INDY, samConversionsMode)
    }
}
