/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.internal.abi.operations

import org.jetbrains.kotlin.abi.tools.AbiTools
import org.jetbrains.kotlin.buildtools.api.ExecutionPolicy
import org.jetbrains.kotlin.buildtools.api.KotlinLogger
import org.jetbrains.kotlin.buildtools.api.ProjectId
import org.jetbrains.kotlin.buildtools.api.abi.operations.AbiTextFileCompareOperation
import org.jetbrains.kotlin.buildtools.internal.BuildOperationImpl
import org.jetbrains.kotlin.buildtools.internal.Options
import java.nio.file.Path

internal class AbiTextFileCompareOperationImpl(
    private val appendable: Appendable,
    override val expectedDumpFile: Path,
    override val actualDumpFile: Path,
    private val abiTools: AbiTools,
) : BuildOperationImpl<Unit>(), AbiTextFileCompareOperation, AbiTextFileCompareOperation.Builder {
    override val options: Options = Options(AbiTextFileCompareOperation::class)

    override fun executeImpl(projectId: ProjectId, executionPolicy: ExecutionPolicy, logger: KotlinLogger?) {
        val diff = abiTools.filesDiff(
            expectedDumpFile.toFile(),
            actualDumpFile.toFile(),
        )
        if (diff != null) {
            appendable.append(diff)
        }
    }

    override fun build(): AbiTextFileCompareOperation {
        return this
    }
}