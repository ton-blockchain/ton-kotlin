package org.ton.sdk.cell.boc

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellContext
import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

public abstract class BagOfCells {
    public abstract fun getRootCell(index: Int): Cell

    public class DecodeOptions internal constructor(
        public val checkHashes: Boolean,
        public val lazyLoad: Boolean,
        public val checkCrc32c: Boolean,
    ) {
        public class Builder {
            public var checkHashes: Boolean = true

            public var lazyLoad: Boolean = false

            public var checkCrc32c: Boolean = true

            public fun build(): DecodeOptions {
                return DecodeOptions(
                    checkHashes = checkHashes,
                    lazyLoad = lazyLoad,
                    checkCrc32c = checkCrc32c,
                )
            }
        }

        public companion object {
            public val Default: DecodeOptions = DecodeOptions(
                checkHashes = true,
                lazyLoad = false,
                checkCrc32c = true
            )
        }
    }

    public class EncodeOptions internal constructor(
        /**
         * Enables bag-of-cells index creation
         *
         * (useful for lazy deserialization of large bags of cells).
         */
        public val withIndex: Boolean,

        /**
         * includes the CRC32-C of all data into the serialization
         *
         * (useful for checking data integrity).
         */
        public val withCrc32c: Boolean,

        /**
         * Explicitly stores the hash of the root cell into the serialization
         * (so that it can be quickly recovered afterwards without a complete deserialization).
         */
        public val withTopHashes: Boolean,

        /**
         * Stores hashes of some intermediate (non-leaf) cells
         * (useful for lazy deserialization of large bags of cells).
         */
        public val withInternalHashes: Boolean,

        /**
         * Stores cell cache bits to control caching of deserialized cells.
         */
        public val withCacheBits: Boolean
    ) {
        public class Builder {
            public var withIndex: Boolean = false

            public var withCrc32c: Boolean = false

            public var withTopHash: Boolean = false

            public var withInternalHashes: Boolean = false

            public var withCacheBits: Boolean = false

            public fun build(): EncodeOptions {
                return EncodeOptions(
                    withIndex = withIndex,
                    withCrc32c = withCrc32c,
                    withTopHashes = withTopHash,
                    withInternalHashes = withInternalHashes,
                    withCacheBits = withCacheBits
                )
            }
        }

        public companion object {
            @JvmField
            public val Default: EncodeOptions = EncodeOptions(
                withIndex = false,
                withCrc32c = false,
                withTopHashes = false,
                withInternalHashes = false,
                withCacheBits = false
            )
        }
    }

    public companion object {
        @JvmStatic
        @JvmOverloads
        public fun decodeFromByteArray(
            byteArray: ByteArray,
            options: DecodeOptions = DecodeOptions.Default
        ): Array<Cell> {
            val boc = StaticBagOfCells(byteArray, options)
            return Array(boc.header.rootCount) {
                boc.getRootCell(it)
            }
        }

        @JvmStatic
        @JvmOverloads
        public fun encodeToByteArray(
            cell: Cell,
            options: EncodeOptions = EncodeOptions.Default
        ): ByteArray = encodeToByteArray(arrayOf(cell), options)

        @JvmStatic
        @JvmOverloads
        public fun encodeToByteArray(
            rootCells: Array<Cell>,
            options: EncodeOptions = EncodeOptions.Default
        ): ByteArray {
            val serializer = BagOfCellSerializer(CellContext.EMPTY)
            for (cell in rootCells) {
                serializer.addRoot(cell)
            }
            serializer.importCells()
            val buffer = Buffer()
            serializer.serialize(buffer, options)
            return buffer.readByteArray()
        }
    }
}
