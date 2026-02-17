/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.tests.arguments

import org.jetbrains.kotlin.buildtools.api.arguments.ExperimentalCompilerArgument
import org.jetbrains.kotlin.buildtools.api.arguments.JvmCompilerArguments.Companion.X_SUPPORT_COMPATQUAL_CHECKER_FRAMEWORK_ANNOTATIONS
import org.jetbrains.kotlin.buildtools.api.arguments.enums.CompatqualAnnotationsMode
import org.jetbrains.kotlin.buildtools.api.jvm.JvmPlatformToolchain.Companion.jvm
import org.jetbrains.kotlin.buildtools.tests.CompilerExecutionStrategyConfiguration
import org.jetbrains.kotlin.buildtools.tests.compilation.BaseCompilationTest
import org.jetbrains.kotlin.buildtools.tests.compilation.model.DefaultStrategyAgnosticCompilationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.DisplayName
import java.nio.file.Paths

@OptIn(ExperimentalCompilerArgument::class)
internal class CompatqualAnnotationsModeConversionTest : BaseCompilationTest() {

    @DisplayName("Test Xsupport-compatqual-checker-framework-annotations is converted to a compiler argument correctly")
    @DefaultStrategyAgnosticCompilationTest
    fun testCompatqualAnnotationsModeToArgumentString(strategyConfig: CompilerExecutionStrategyConfiguration) {
        assumeCompatqualAnnotationsSupported(strategyConfig)
        val toolchain = strategyConfig.first
        val compatqualAnnotationsMode = CompatqualAnnotationsMode.DISABLE
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).apply {
            compilerArguments[X_SUPPORT_COMPATQUAL_CHECKER_FRAMEWORK_ANNOTATIONS] = compatqualAnnotationsMode
        }.build()

        val argumentStrings = jvmOperation.compilerArguments.toArgumentStrings()
        val valueString = argumentStrings.first{ it.startsWith("-Xsupport-compatqual-checker-framework-annotations") }.removePrefix("-Xsupport-compatqual-checker-framework-annotations=")

        assertEquals(compatqualAnnotationsMode.stringValue, valueString)
    }

    @DisplayName("Test that Xsupport-compatqual-checker-framework-annotations defaults to ENABLE")
    @DefaultStrategyAgnosticCompilationTest
    fun testCompatqualAnnotationsModeDefaultValue(strategyConfig: CompilerExecutionStrategyConfiguration) {
        assumeCompatqualAnnotationsSupported(strategyConfig)
        val toolchain = strategyConfig.first
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).build()

        val argumentStrings = jvmOperation.compilerArguments.toArgumentStrings()
        val valueString = argumentStrings.firstOrNull { it.startsWith("-Xsupport-compatqual-checker-framework-annotations") }?.removePrefix("-Xsupport-compatqual-checker-framework-annotations=")

        // see: https://github.com/JetBrains/kotlin/blob/8757a28e1d65b567a9310e05d844ea318e77e58c/build-common/src/org/jetbrains/kotlin/compilerRunner/argumentsToStrings.kt#L40-L43
        assertEquals(null, valueString)
    }

    @DisplayName("Test Xsupport-compatqual-checker-framework-annotations is set and retrieved correctly")
    @DefaultStrategyAgnosticCompilationTest
    fun testCompatqualAnnotationsModeGetWhenSet(strategyConfig: CompilerExecutionStrategyConfiguration) {
        assumeCompatqualAnnotationsSupported(strategyConfig)
        val toolchain = strategyConfig.first
        val expectedCompatqualAnnotationsMode = CompatqualAnnotationsMode.DISABLE
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).apply {
            compilerArguments[X_SUPPORT_COMPATQUAL_CHECKER_FRAMEWORK_ANNOTATIONS] = expectedCompatqualAnnotationsMode
        }.build()

        val actualCompatqualAnnotationsMode = jvmOperation.compilerArguments[X_SUPPORT_COMPATQUAL_CHECKER_FRAMEWORK_ANNOTATIONS]

        assertEquals(expectedCompatqualAnnotationsMode, actualCompatqualAnnotationsMode)
    }

    @DisplayName("Test Xsupport-compatqual-checker-framework-annotations returns default value ENABLE when not explicitly set")
    @DefaultStrategyAgnosticCompilationTest
    fun testCompatqualAnnotationsModeGetDefault(strategyConfig: CompilerExecutionStrategyConfiguration) {
        assumeCompatqualAnnotationsSupported(strategyConfig)
        val toolchain = strategyConfig.first
        val jvmOperation = toolchain.jvm.jvmCompilationOperationBuilder(emptyList(), Paths.get(".")).build()

        val compatqualAnnotationsMode = jvmOperation.compilerArguments[X_SUPPORT_COMPATQUAL_CHECKER_FRAMEWORK_ANNOTATIONS]

        assertEquals(CompatqualAnnotationsMode.ENABLE, compatqualAnnotationsMode)
    }

    private fun assumeCompatqualAnnotationsSupported(strategyConfig: CompilerExecutionStrategyConfiguration) {
        assumeTrue(
            strategyConfig.first.getCompilerVersion() >= X_SUPPORT_COMPATQUAL_CHECKER_FRAMEWORK_ANNOTATIONS.availableSinceVersion.toString(),
            "Test requires compiler version >= ${X_SUPPORT_COMPATQUAL_CHECKER_FRAMEWORK_ANNOTATIONS.availableSinceVersion}"
        )
    }
}
