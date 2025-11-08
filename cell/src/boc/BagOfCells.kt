package org.ton.sdk.cell.boc

import org.ton.sdk.cell.Cell
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
    }
}
