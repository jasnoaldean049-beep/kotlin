/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.internal.abi.operations

import org.jetbrains.kotlin.abi.tools.AbiTools
import org.jetbrains.kotlin.buildtools.api.ExecutionPolicy
import org.jetbrains.kotlin.buildtools.api.KotlinLogger
import org.jetbrains.kotlin.buildtools.api.ProjectId
import org.jetbrains.kotlin.buildtools.api.abi.AbiFilters
import org.jetbrains.kotlin.buildtools.api.abi.operations.AbiDumpJvmToStringOperation
import org.jetbrains.kotlin.buildtools.internal.BaseOptionWithDefault
import org.jetbrains.kotlin.buildtools.internal.BuildOperationImpl
import org.jetbrains.kotlin.buildtools.internal.Options
import org.jetbrains.kotlin.buildtools.internal.UseFromImplModuleRestricted
import org.jetbrains.kotlin.buildtools.internal.abi.AbiValidationUtils
import java.nio.file.Path

internal class AbiDumpJvmToStringOperationImpl(
    private val appendable: Appendable,
    override val inputFiles: Iterable<Path>,
    private val abiTools: AbiTools,
) : BuildOperationImpl<Unit>(), AbiDumpJvmToStringOperation, AbiDumpJvmToStringOperation.Builder {

    override val options: Options = Options(AbiDumpJvmToStringOperation::class)

    override fun executeImpl(projectId: ProjectId, executionPolicy: ExecutionPolicy, logger: KotlinLogger?) {
        val filters: AbiFilters = options[PATTERN_FILTERS]
        abiTools.printJvmDump(appendable, inputFiles.map { it.toFile() }, AbiValidationUtils.convert(filters))
    }


    @UseFromImplModuleRestricted
    override fun <V> get(key: AbiDumpJvmToStringOperation.Option<V>): V {
        return options[key]
    }

    @UseFromImplModuleRestricted
    override fun <V> set(key: AbiDumpJvmToStringOperation.Option<V>, value: V) {
        options[key] = value
    }

    override fun build(): AbiDumpJvmToStringOperation {
        return this
    }

    class Option<V> : BaseOptionWithDefault<V> {
        constructor(id: String) : super(id)
        constructor(id: String, default: V) : super(id, default = default)
    }

    companion object {
        /**
         * Filters with declarations of patterns containing `**`, `*` and `?` wildcards.
         */
        @JvmField
        public val PATTERN_FILTERS: Option<AbiFilters> = Option("PATTERN_FILTERS", AbiFilters.EMPTY)
    }
}