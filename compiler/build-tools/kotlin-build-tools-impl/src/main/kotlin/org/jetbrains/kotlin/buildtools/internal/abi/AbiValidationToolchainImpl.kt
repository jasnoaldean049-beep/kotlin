/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.internal.abi

import org.jetbrains.kotlin.abi.tools.AbiTools
import org.jetbrains.kotlin.buildtools.api.abi.AbiValidationToolchain
import org.jetbrains.kotlin.buildtools.api.abi.KlibTargetId
import org.jetbrains.kotlin.buildtools.api.abi.operations.AbiDumpJvmToStringOperation
import org.jetbrains.kotlin.buildtools.api.abi.operations.AbiDumpKlibToStringOperation
import org.jetbrains.kotlin.buildtools.api.abi.operations.AbiTextFileCompareOperation
import org.jetbrains.kotlin.buildtools.internal.abi.operations.AbiDumpJvmToStringOperationImpl
import org.jetbrains.kotlin.buildtools.internal.abi.operations.AbiDumpKlibToStringOperationImpl
import org.jetbrains.kotlin.buildtools.internal.abi.operations.AbiTextFileCompareOperationImpl
import java.nio.file.Path
import java.util.function.Function

internal class AbiValidationToolchainImpl : AbiValidationToolchain {
    private val abiTools = AbiTools.getInstance()

    override fun dumpJvmAbiToString(
        appendable: Appendable,
        inputFiles: Iterable<Path>,
        builderAction: Function<AbiDumpJvmToStringOperation.Builder, Unit>,
    ): AbiDumpJvmToStringOperation {
        val builder = AbiDumpJvmToStringOperationImpl(appendable, inputFiles, abiTools)
        builderAction.apply(builder)
        return builder.build()
    }

    override fun dumpKlibAbiToString(
        appendable: Appendable,
        referenceDumpFile: Path,
        klibs: Map<KlibTargetId, Path>,
        targetsToInfer: Set<KlibTargetId>,
        builderAction: Function<AbiDumpKlibToStringOperation.Builder, Unit>,
    ): AbiDumpKlibToStringOperation {
        val builder = AbiDumpKlibToStringOperationImpl(appendable, referenceDumpFile, klibs, targetsToInfer, abiTools)
        builderAction.apply(builder)
        return builder.build()
    }

    override fun compareDumpFiles(
        diff: Appendable,
        expectedDumpFile: Path,
        actualDumpFile: Path,
    ): AbiTextFileCompareOperation {
        return AbiTextFileCompareOperationImpl(diff, expectedDumpFile, actualDumpFile, abiTools).build()
    }
}