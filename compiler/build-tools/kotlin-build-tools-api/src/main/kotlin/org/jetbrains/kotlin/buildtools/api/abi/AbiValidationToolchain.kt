/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.api.abi

import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.buildtools.api.KotlinToolchains
import org.jetbrains.kotlin.buildtools.api.abi.operations.AbiDumpJvmToStringOperation
import org.jetbrains.kotlin.buildtools.api.abi.operations.AbiDumpKlibToStringOperation
import org.jetbrains.kotlin.buildtools.api.abi.operations.AbiTextFileCompareOperation
import org.jetbrains.kotlin.buildtools.api.getToolchain
import java.nio.file.Path
import java.util.function.Function

/**
 * A toolchains to use Application Binary Interface (ABI) validation.
 *
 * ABI validation is a part of the Kotlin toolset designed to control which declarations are available to other modules.
 * You can use this tool to control the binary compatibility of your library or shared module.
 *
 * @since 2.4.0
 */
@ExperimentalBuildToolsApi
public interface AbiValidationToolchain : KotlinToolchains.Toolchain {
    /**
     * Prints an ABI dump for JVM from [inputFiles] into the specified [appendable].
     * It is possible to pass class-files or jar files in [inputFiles].
     *
     * To control which declarations are passed to the dump, the option [AbiDumpJvmToStringOperation.PATTERN_FILTERS] could be used. By default, no filters will be applied.
     *
     * @since 2.4.0
     */
    public fun dumpJvmAbiToString(
        appendable: Appendable,
        inputFiles: Iterable<Path>,
        builderAction: Function<AbiDumpJvmToStringOperation.Builder, Unit> = Function<AbiDumpJvmToStringOperation.Builder, Unit> { },
    ): AbiDumpJvmToStringOperation

    /**
     * Prints an ABI dump for klib targets from [klibs] into the specified [appendable].
     * Compressed and unpacked klibs are supported.
     *
     * If [targetsToInfer] is not empty, for the specified targets the ABI will be inferred from the [referenceDumpFile].
     * The inference works as follows:
     * - for each target from [targetsToInfer], the ABI is inferred from the [referenceDumpFile], if it exists, not empty, and this target is present in it
     * - all the non-inferred targets that belong to the group that this target belongs to are found. Then all declarations are added that are present in all of them.
     *
     * The inference is used in cases where the host compiler cannot compile some targets, but there is a need to build an ABI dump,
     * even if with some inaccuracies.
     *
     * To control which declarations are passed to the dump, the option [AbiDumpKlibToStringOperation.PATTERN_FILTERS] could be used. By default, no filters will be applied.
     *
     * @since 2.4.0
     */
    public fun dumpKlibAbiToString(
        appendable: Appendable,
        referenceDumpFile: Path,
        klibs: Map<KlibTargetId, Path>,
        targetsToInfer: Set<KlibTargetId>,
        builderAction: Function<AbiDumpKlibToStringOperation.Builder, Unit> = Function<AbiDumpKlibToStringOperation.Builder, Unit> {},
    ): AbiDumpKlibToStringOperation

    /**
     * Compares two files line-by-line.
     *
     * If files are equal, nothing is written to [diff].
     *
     * @since 2.4.0
     */
    public fun compareDumpFiles(diff: Appendable, expectedDumpFile: Path, actualDumpFile: Path): AbiTextFileCompareOperation


    public companion object {
        /**
         * Gets a [AbiValidationToolchain] instance from [KotlinToolchains].
         *
         * Equivalent to `kotlinToolchains.getToolchain<AbiValidationToolchain>()`
         *
         * @since 2.4.0
         */
        @JvmStatic
        @get:JvmName("from")
        public inline val KotlinToolchains.abiValidation: AbiValidationToolchain get() = getToolchain<AbiValidationToolchain>()
    }
}

