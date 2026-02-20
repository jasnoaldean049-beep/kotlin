/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.internal.abi.operations

import org.jetbrains.kotlin.abi.tools.AbiTools
import org.jetbrains.kotlin.abi.tools.KlibTarget
import org.jetbrains.kotlin.buildtools.api.ExecutionPolicy
import org.jetbrains.kotlin.buildtools.api.KotlinLogger
import org.jetbrains.kotlin.buildtools.api.ProjectId
import org.jetbrains.kotlin.buildtools.api.abi.AbiFilters
import org.jetbrains.kotlin.buildtools.api.abi.KlibTargetId
import org.jetbrains.kotlin.buildtools.api.abi.operations.AbiDumpKlibToStringOperation
import org.jetbrains.kotlin.buildtools.internal.BaseOptionWithDefault
import org.jetbrains.kotlin.buildtools.internal.BuildOperationImpl
import org.jetbrains.kotlin.buildtools.internal.Options
import org.jetbrains.kotlin.buildtools.internal.UseFromImplModuleRestricted
import org.jetbrains.kotlin.buildtools.internal.abi.AbiValidationUtils
import java.nio.file.Path

internal class AbiDumpKlibToStringOperationImpl(
    private val appendable: Appendable,
    override val referenceDumpFile: Path,
    override val klibs: Map<KlibTargetId, Path>,
    override val targetsToInfer: Set<KlibTargetId>,
    private val abiTools: AbiTools,
) : BuildOperationImpl<Unit>(), AbiDumpKlibToStringOperation, AbiDumpKlibToStringOperation.Builder {

    override val options: Options = Options(AbiDumpKlibToStringOperation::class)


    override fun executeImpl(projectId: ProjectId, executionPolicy: ExecutionPolicy, logger: KotlinLogger?) {
        val filters: AbiFilters = options[PATTERN_FILTERS]

        val mergedDump = abiTools.createKlibDump()
        klibs.forEach { (target, klibDir) ->
            val dump = abiTools.extractKlibAbi(
                klibDir.toFile(),
                KlibTarget(target.canonicalName, target.customizedName),
                AbiValidationUtils.convert(filters)
            )
            mergedDump.merge(dump)
        }
        if (targetsToInfer.isNotEmpty()) {
            val reference = referenceDumpFile.toFile()
            val referenceDump = if (reference.exists() && reference.isFile) {
                abiTools.loadKlibDump(reference)
            } else {
                abiTools.createKlibDump()
            }
            targetsToInfer.forEach { unsupportedTarget ->
                val inferredDump = mergedDump.inferAbiForUnsupportedTarget(referenceDump, AbiValidationUtils.convert(unsupportedTarget))
                mergedDump.merge(inferredDump)
            }
        }
        mergedDump.print(appendable)
    }


    @UseFromImplModuleRestricted
    override fun <V> get(key: AbiDumpKlibToStringOperation.Option<V>): V {
        return options[key]
    }

    @UseFromImplModuleRestricted
    override fun <V> set(key: AbiDumpKlibToStringOperation.Option<V>, value: V) {
        options[key] = value
    }

    override fun build(): AbiDumpKlibToStringOperation {
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