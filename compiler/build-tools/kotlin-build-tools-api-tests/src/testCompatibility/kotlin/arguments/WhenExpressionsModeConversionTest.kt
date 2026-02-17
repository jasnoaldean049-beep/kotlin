/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.tests.arguments

import org.jetbrains.kotlin.buildtools.api.arguments.ExperimentalCompilerArgument
import org.jetbrains.kotlin.buildtools.api.arguments.JvmCompilerArguments.Companion.X_WHEN_EXPRESSIONS
import org.jetbrains.kotlin.buildtools.api.arguments.enums.WhenExpressionsMode
import org.jetbrains.kotlin.buildtools.api.jvm.JvmPlatformToolchain.Companion.jvm
import org.jetbrains.kotlin.buildtools.tests.CompilerExecutionStrategyConfiguration
import org.jetbrains.kotlin.buildtools.tests.compilation.BaseCompilationTest
import org.jetbrains.kotlin.buildtools.tests.compilation.model.DefaultStrategyAgnosticCompilationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import java.nio.file.Paths

@OptIn(ExperimentalCompilerArgument::class)
internal class WhenExpressionsModeConversionTest : BaseCompilationTest() {

    @DisplayName("Test Xwhen-expressions is converted to a compiler argument correctly")
    @DefaultStrategyAgnosticCompilationTest
    fun testWhenExpressionsModeToArgumentString(strategyConfig: CompilerExecutionStrategyConfiguration) {
        val toolchain = strategyConfig.first
        val whenExpressionsMode = WhenExpressionsMode.INDY
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).apply {
            compilerArguments[X_WHEN_EXPRESSIONS] = whenExpressionsMode
        }.build()

        val argumentStrings = jvmOperation.compilerArguments.toArgumentStrings()
        val valueString = argumentStrings.firstOrNull { it.startsWith("-Xwhen-expressions") }?.removePrefix("-Xwhen-expressions=")

        assertEquals(whenExpressionsMode.stringValue, valueString)
    }

    @DisplayName("Test that Xwhen-expressions defaults to null")
    @DefaultStrategyAgnosticCompilationTest
    fun testWhenExpressionsModeDefaultValue(strategyConfig: CompilerExecutionStrategyConfiguration) {
        val toolchain = strategyConfig.first
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).build()

        val argumentStrings = jvmOperation.compilerArguments.toArgumentStrings()
        val valueString = argumentStrings.firstOrNull { it.startsWith("-Xwhen-expressions") }?.removePrefix("-Xwhen-expressions=")

        // see: https://github.com/JetBrains/kotlin/blob/8757a28e1d65b567a9310e05d844ea318e77e58c/build-common/src/org/jetbrains/kotlin/compilerRunner/argumentsToStrings.kt#L40-L43
        assertEquals(null, valueString)
    }

    @DisplayName("Test Xwhen-expressions is set and retrieved correctly")
    @DefaultStrategyAgnosticCompilationTest
    fun testWhenExpressionsModeGetWhenSet(strategyConfig: CompilerExecutionStrategyConfiguration) {
        val toolchain = strategyConfig.first
        val expectedWhenExpressionsMode = WhenExpressionsMode.INLINE
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).apply {
            compilerArguments[X_WHEN_EXPRESSIONS] = expectedWhenExpressionsMode
        }.build()

        val actualWhenExpressionsMode = jvmOperation.compilerArguments[X_WHEN_EXPRESSIONS]

        assertEquals(expectedWhenExpressionsMode, actualWhenExpressionsMode)
    }

    @DisplayName("Test Xwhen-expressions returns default value null when not explicitly set")
    @DefaultStrategyAgnosticCompilationTest
    fun testWhenExpressionsModeGetDefault(strategyConfig: CompilerExecutionStrategyConfiguration) {
        val toolchain = strategyConfig.first
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).build()

        val whenExpressionsMode = jvmOperation.compilerArguments[X_WHEN_EXPRESSIONS]

        assertEquals(null, whenExpressionsMode)
    }
}
