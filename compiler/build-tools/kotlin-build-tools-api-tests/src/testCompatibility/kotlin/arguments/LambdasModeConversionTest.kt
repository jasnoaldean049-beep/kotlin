/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.tests.arguments

import org.jetbrains.kotlin.buildtools.api.arguments.ExperimentalCompilerArgument
import org.jetbrains.kotlin.buildtools.api.arguments.JvmCompilerArguments.Companion.X_LAMBDAS
import org.jetbrains.kotlin.buildtools.api.arguments.enums.LambdasMode
import org.jetbrains.kotlin.buildtools.api.jvm.JvmPlatformToolchain.Companion.jvm
import org.jetbrains.kotlin.buildtools.tests.CompilerExecutionStrategyConfiguration
import org.jetbrains.kotlin.buildtools.tests.compilation.BaseCompilationTest
import org.jetbrains.kotlin.buildtools.tests.compilation.model.DefaultStrategyAgnosticCompilationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.DisplayName
import java.nio.file.Paths

@OptIn(ExperimentalCompilerArgument::class)
internal class LambdasModeConversionTest : BaseCompilationTest() {

    @DisplayName("Test Xlambdas is converted to a compiler argument correctly")
    @DefaultStrategyAgnosticCompilationTest
    fun testLambdasModeToArgumentString(strategyConfig: CompilerExecutionStrategyConfiguration) {
        assumeLambdasSupported(strategyConfig)
        val toolchain = strategyConfig.first
        val lambdasMode = LambdasMode.INDY
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).apply {
            compilerArguments[X_LAMBDAS] = lambdasMode
        }.build()

        val argumentStrings = jvmOperation.compilerArguments.toArgumentStrings()
        val valueString = argumentStrings.first { it.startsWith("-Xlambdas=") }.removePrefix("-Xlambdas=")

        assertEquals(lambdasMode.stringValue, valueString)
    }

    @DisplayName("Test that Xlambdas is not set by default")
    @DefaultStrategyAgnosticCompilationTest
    fun testLambdasModeNotSetByDefault(strategyConfig: CompilerExecutionStrategyConfiguration) {
        val toolchain = strategyConfig.first
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).build()

        val argumentStrings = jvmOperation.compilerArguments.toArgumentStrings()
        val valueString = argumentStrings.firstOrNull { it.startsWith("-Xlambdas=") }?.removePrefix("-Xlambdas=")

        assertEquals(null, valueString)
    }

    @DisplayName("Test Xlambdas is set and retrieved correctly")
    @DefaultStrategyAgnosticCompilationTest
    fun testLambdasModeGetWhenSet(strategyConfig: CompilerExecutionStrategyConfiguration) {
        assumeLambdasSupported(strategyConfig)
        val toolchain = strategyConfig.first
        val expectedLambdasMode = LambdasMode.INDY
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).apply {
            compilerArguments[X_LAMBDAS] = expectedLambdasMode
        }.build()

        val actualLambdasMode = jvmOperation.compilerArguments[X_LAMBDAS]

        assertEquals(expectedLambdasMode, actualLambdasMode)
    }

    @DisplayName("Test Xlambdas is retrieved correctly when it is not set")
    @DefaultStrategyAgnosticCompilationTest
    fun testLambdasModeGetWhenNull(strategyConfig: CompilerExecutionStrategyConfiguration) {
        assumeLambdasSupported(strategyConfig)
        val toolchain = strategyConfig.first
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).build()

        val lambdasMode = jvmOperation.compilerArguments[X_LAMBDAS]

        assertEquals(null, lambdasMode)
    }

    private fun assumeLambdasSupported(strategyConfig: CompilerExecutionStrategyConfiguration) {
        assumeTrue(
            strategyConfig.first.getCompilerVersion() >= X_LAMBDAS.availableSinceVersion.toString(),
            "Test requires compiler version >= ${X_LAMBDAS.availableSinceVersion}"
        )
    }
}
