/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.api.abi.operations

import org.jetbrains.kotlin.buildtools.api.BuildOperation
import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.buildtools.api.abi.AbiFilters
import org.jetbrains.kotlin.buildtools.api.internal.BaseOption
import java.nio.file.Path

/**
 * Prints an ABI dump for JVM from [Builder.inputFiles] into some appendable.
 * It is possible to pass class-files or jar files in [Builder.inputFiles].
 *
 * To control which declarations are passed to the dump, the option [AbiDumpJvmToStringOperation.PATTERN_FILTERS] could be used. By default, no filters will be applied.
 *
 * @since 2.4.0
 */
@ExperimentalBuildToolsApi
public interface AbiDumpJvmToStringOperation : BuildOperation<Unit> {

    public interface Builder : BuildOperation.Builder {
        public val inputFiles: Iterable<Path>

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
         * Creates an immutable instance of [AbiDumpJvmToStringOperation] based on the configuration of this builder.
         *
         * @since 2.4.0
         */
        public fun build(): AbiDumpJvmToStringOperation

    }

    public companion object {
        @JvmField
        public val PATTERN_FILTERS: Option<AbiFilters> = Option("PATTERN_FILTERS")
    }

    /**
     * An option for configuring a [AbiDumpJvmToStringOperation].
     *
     * @see get
     * @see set
     * @see AbiDumpJvmToStringOperation.Companion
     */
    public class Option<V> internal constructor(id: String) : BaseOption<V>(id)
}
