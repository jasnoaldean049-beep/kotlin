/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.api.abi.operations

import org.jetbrains.kotlin.buildtools.api.BuildOperation
import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.buildtools.api.abi.AbiFilters
import org.jetbrains.kotlin.buildtools.api.abi.KlibTargetId
import org.jetbrains.kotlin.buildtools.api.internal.BaseOption
import java.nio.file.Path


/**
 * Prints an ABI dump for klib targets from [Builder.klibs] into some appendable.
 * Compressed and unpacked klibs are supported.
 *
 * If [Builder.targetsToInfer] is not empty, for the specified targets the ABI will be inferred from the [Builder.referenceDumpFile].
 * The inference works as follows:
 * - for each target from [Builder.targetsToInfer], the ABI is inferred from the [Builder.referenceDumpFile], if it exists, not empty, and this target is present in it
 * - all the non-inferred targets that belong to the group that this target belongs to are found. Then all declarations are added that are present in all of them.
 *
 * The inference is used in cases where the host compiler cannot compile some targets, but there is a need to build an ABI dump,
 * even if with some inaccuracies.
 *
 * To control which declarations are passed to the dump, the option [AbiDumpKlibToStringOperation.PATTERN_FILTERS] could be used. By default, no filters will be applied.
 *
 * @since 2.4.0
 */
@ExperimentalBuildToolsApi
public interface AbiDumpKlibToStringOperation : BuildOperation<Unit> {
    public interface Builder : BuildOperation.Builder {
        public val referenceDumpFile: Path

        public val klibs: Map<KlibTargetId, Path>

        public val targetsToInfer: Set<KlibTargetId>

        /**
         * Get the value for option specified by [key] if it was previously [set] or if it has a default value.
         *
         * @return the previously set value for an option
         * @throws IllegalStateException if the option was not set and has no default value
         *
         * @since 2.4.0
         */
        public operator fun <V> get(key: Option<V>): V

        /**
         * Set the [value] for option specified by [key], overriding any previous value for that option.
         *
         * @since 2.4.0
         */
        public operator fun <V> set(key: Option<V>, value: V)

        /**
         * Creates an immutable instance of [AbiDumpKlibToStringOperation] based on the configuration of this builder.
         *
         * @since 2.4.0
         */
        public fun build(): AbiDumpKlibToStringOperation
    }

    public companion object {
        @JvmField
        public val PATTERN_FILTERS: Option<AbiFilters> = Option("PATTERN_FILTERS")
    }

    /**
     * An option for configuring a [AbiDumpKlibToStringOperation].
     *
     * @see get
     * @see set
     * @see AbiDumpKlibToStringOperation.Companion
     */
    public class Option<V> internal constructor(id: String) : BaseOption<V>(id)
}
