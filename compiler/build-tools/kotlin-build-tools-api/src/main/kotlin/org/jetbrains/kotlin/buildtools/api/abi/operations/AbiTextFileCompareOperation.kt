/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.api.abi.operations

import org.jetbrains.kotlin.buildtools.api.BuildOperation
import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import java.nio.file.Path

/**
 * Compares two files line-by-line.
 *
 * If files are equal, nothing is written to diff.
 *
 * @since 2.4.0
 */
@ExperimentalBuildToolsApi
public interface AbiTextFileCompareOperation : BuildOperation<Unit> {
    public interface Builder : BuildOperation.Builder {
        public val expectedDumpFile: Path
        public val actualDumpFile: Path

        /**
         * Creates an immutable instance of [AbiTextFileCompareOperation] based on the configuration of this builder.
         *
         * @since 2.4.0
         */
        public fun build(): AbiTextFileCompareOperation
    }
}
