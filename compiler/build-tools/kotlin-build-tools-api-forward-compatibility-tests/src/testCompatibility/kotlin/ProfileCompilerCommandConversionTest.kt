/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import org.jetbrains.kotlin.buildtools.api.KotlinToolchains
import org.jetbrains.kotlin.buildtools.api.arguments.ExperimentalCompilerArgument
import org.jetbrains.kotlin.buildtools.api.arguments.JvmCompilerArguments.Companion.X_PROFILE
import org.jetbrains.kotlin.buildtools.api.jvm.JvmPlatformToolchain.Companion.jvm
import org.jetbrains.kotlin.buildtools.tests.compilation.BaseCompilationTest
import org.jetbrains.kotlin.buildtools.tests.compilation.model.BtaVersionsOnlyCompilationTest
import org.jetbrains.kotlin.test.KtAssert
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.Path

@OptIn(ExperimentalCompilerArgument::class)
internal class ProfileCompilerCommandConversionTest : BaseCompilationTest() {


    @DisplayName("Test that ProfileCompilerCommand is not set by default")
    @BtaVersionsOnlyCompilationTest
    fun testXprofileNotSetByDefault(toolchain: KotlinToolchains) {
        val jvmOperation = toolchain.jvm.createJvmCompilationOperation(emptyList(), Paths.get("."))

        val valueString =
            jvmOperation.compilerArguments.toArgumentStrings().firstOrNull { it.startsWith("-Xprofile=") }

        assertEquals(null, valueString)
    }

    @DisplayName("Test ProfileCompilerCommand is set and retrieved correctly")
    @BtaVersionsOnlyCompilationTest
    fun testXprofileGetWhenSet(toolchain: KotlinToolchains) {
        val profilerPath = workingDirectory.resolve("path/to/libasyncProfiler.so")
        val command = "event=cpu,interval=1ms,threads,start"
        val outputDir = workingDirectory.resolve("path/to/snapshots")
        val profileCompilerCommand = "${profilerPath.toFile().absolutePath}${File.pathSeparator}${command}${File.pathSeparator}${outputDir.toFile().absolutePath}"
        val jvmOperation = toolchain.jvm.createJvmCompilationOperation(emptyList(), Paths.get(".")).apply {
            compilerArguments[X_PROFILE] = profileCompilerCommand
        }

        val profileCommand = jvmOperation.compilerArguments[X_PROFILE]?.split(File.pathSeparator)
        KtAssert.assertNotNull("X_PROFILE must not be null when reading back from compiler arguments", profileCommand)
        assertEquals(profilerPath, Path(profileCommand[0]))
        assertEquals(command, profileCommand[1])
        assertEquals(outputDir, Path(profileCommand[2]))
    }

    @DisplayName("Test ProfileCompilerCommand is retrieved correctly when it is not set")
    @BtaVersionsOnlyCompilationTest
    fun testXprofileGetWhenNull(toolchain: KotlinToolchains) {
        val jvmOperation = toolchain.jvm.createJvmCompilationOperation(emptyList(), Paths.get("."))

        val profileCommand = jvmOperation.compilerArguments[X_PROFILE]

        assertEquals(null, profileCommand)
    }
}