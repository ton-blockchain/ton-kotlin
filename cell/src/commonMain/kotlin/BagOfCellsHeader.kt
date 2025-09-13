package org.ton.kotlin.cell

import kotlinx.io.Source
import org.ton.kotlin.cell.internal.readLong

public data class BagOfCellsHeader(
    val magic: Int,
    val rootCount: Int,
    val cellCount: Int,
    val absentCount: Int,
    val refByteSize: Int,
    val offsetByteSize: Int,
    val hasIndex: Boolean,
    val hasRoots: Boolean,
    val hasCrc32c: Boolean,
    val hasCacheBits: Boolean,
    val rootsOffset: Long,
    val indexOffset: Long,
    val dataOffset: Long,
    val dataSize: Long,
    val totalSize: Long
) {
    public companion object {
        public const val BOC_GENERIC_MAGIC: Int = 0xB5EE9C72.toInt()
        public const val BOC_INDEXED_MAGIC: Int = 0x68FF65F3
        public const val BOC_INDEXED_CRC32C_MAGIC: Int = 0xACC3A728.toInt()

        public fun parse(source: Source): BagOfCellsHeader {
            val magic = source.readInt()
            val byte = source.readByte().toInt() and 0xFF

            val hasIndex: Boolean
            val hasCrc32c: Boolean
            val hasCacheBits: Boolean
            if (magic == BOC_GENERIC_MAGIC) {
                hasIndex = (byte and 0b1000_0000) != 0
                hasCrc32c = (byte and 0b0100_0000) != 0
                hasCacheBits = (byte and 0b0010_0000) != 0
            } else {
                hasIndex = false
                hasCrc32c = false
                hasCacheBits = false
            }
            check(!hasCacheBits || hasIndex) {
                "Invalid BOC ${magic.toHexString()} header: hasCacheBits=$hasCacheBits, hasIndex=$hasIndex"
            }

            val refByteSize = byte and 0b0001_1111
            check(refByteSize in 1..4) {
                "Invalid BOC header: refByteSize=$refByteSize"
            }

            val offsetByteSize = source.readByte().toInt()
            check(offsetByteSize in 1..8) {
                "Invalid BOC header: offsetByteSize=$offsetByteSize"
            }
            val rootsOffset = 6L + 3 * refByteSize + offsetByteSize

            val cellCount = source.readLong(refByteSize).toInt()
            check(cellCount >= 0) {
                "Invalid BOC header: cellCount=$cellCount"
            }

            val rootCount = source.readLong(refByteSize).toInt()
            check(rootCount > 0) {
                "Invalid BOC header: rootCount=$rootCount"
            }
            var indexOffset = rootsOffset
            var hasRoots = false
            if (magic == BOC_GENERIC_MAGIC) {
                indexOffset += rootCount.toLong() * refByteSize
                hasRoots = true
            } else {
                check(rootCount == 1) {
                    "Invalid BOC ${magic.toHexString()} header: rootCount=$rootCount"
                }
            }
            var dataOffset = indexOffset
            if (hasIndex) {
                dataOffset += cellCount * offsetByteSize
            }

            val absentCount = source.readLong(refByteSize).toInt()
            check(absentCount >= 0 && rootCount + absentCount <= cellCount) {
                "Invalid BOC header: rootCount=$rootCount, absentCount=$absentCount, cellCount=$cellCount"
            }

            val dataSize = source.readLong(offsetByteSize)
            check(dataSize <= (cellCount.toLong() shl 10)) {
                "Invalid BOC header: dataSize=$dataSize, cellCount=$cellCount"
            }
            check(dataSize <= (1L shl 40)) {
                "Invalid BOC header: dataSize more than 1TiB? dataSize=$dataSize"
            }
            check(dataSize >= cellCount * (2L + refByteSize) - refByteSize) {
                "Invalid BOC header: too many cells for this amount of data bytes. dataSize=$dataSize, cellCount=$cellCount, refByteSize=$refByteSize"
            }
            val totalSize = dataOffset + dataSize + (if (hasCrc32c) 4 else 0)

            return BagOfCellsHeader(
                magic = magic,
                rootCount = rootCount,
                cellCount = cellCount,
                absentCount = absentCount,
                refByteSize = refByteSize,
                offsetByteSize = offsetByteSize,
                hasIndex = hasIndex,
                hasRoots = hasRoots,
                hasCrc32c = hasCrc32c,
                hasCacheBits = hasCacheBits,
                rootsOffset = rootsOffset,
                indexOffset = indexOffset,
                dataOffset = dataOffset,
                dataSize = dataSize,
                totalSize = totalSize
            )
        }
    }
}
