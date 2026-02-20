/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization

import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.util.NaiveSourceBasedFileEntryImpl
import org.jetbrains.kotlin.backend.common.serialization.proto.FileEntry as ProtoFileEntry

// This class is needed solely to have generated `equals()` and `hashCode()` for `FileEntry`, to compare objects by value.
// For correct deduplication, it must have the same fields as `FileEntry` in `KotlinIr.proto`.
data class ProtoFileEntryDeduplicationKey(
    val name: Any,
    val lineStartOffsetList: List<Int>,
    val firstRelevantLineIndex: Int
)

internal class FileEntryCache {
    private val protoIrFileEntryMap = hashMapOf<ProtoFileEntryDeduplicationKey, NaiveSourceBasedFileEntryImpl>()

    internal fun IrLibraryFile.deserializeFileEntry(fileEntryProto: ProtoFileEntry, irInterner: IrInterningService): IrFileEntry {
        val lineStartOffsets: IntArray
        if (fileEntryProto.lineStartOffsetDeltaCount > 0) {
            lineStartOffsets = IntArray(fileEntryProto.lineStartOffsetDeltaCount)
            var offset = 0
            for ((index, delta) in fileEntryProto.lineStartOffsetDeltaList.withIndex()) {
                offset += delta
                lineStartOffsets[index] = offset
            }
        } else {
            lineStartOffsets = fileEntryProto.lineStartOffsetList.toIntArray()
        }

        val name = irInterner.string(deserializeFileEntryName(fileEntryProto))
        return protoIrFileEntryMap.getOrPut(
            ProtoFileEntryDeduplicationKey(
                name,
                if (fileEntryProto.lineStartOffsetDeltaCount > 0) fileEntryProto.lineStartOffsetDeltaList else fileEntryProto.lineStartOffsetList,
                fileEntryProto.firstRelevantLineIndex
            )
        ) {
            NaiveSourceBasedFileEntryImpl(
                name = name,
                lineStartOffsets = lineStartOffsets,
                firstRelevantLineIndex = fileEntryProto.firstRelevantLineIndex
            )
        }
    }
}