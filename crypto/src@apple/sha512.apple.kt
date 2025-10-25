package org.ton.sdk.crypto

import kotlinx.cinterop.*
import platform.CoreCrypto.*

@OptIn(ExperimentalForeignApi::class)
public actual class Sha512 : Digest {
    private var ctx = init()

    actual override val digestSize: Int
        get() = CC_SHA512_DIGEST_LENGTH
    actual override val blockSize: Int
        get() = CC_SHA512_BLOCK_BYTES

    public actual override fun update(
        source: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): Sha512 = apply {
        if (endIndex - startIndex == 0) return this
        source.usePinned { sourcePinned ->
            CC_SHA512_Update(ctx.ptr, sourcePinned.addressOf(startIndex), (endIndex - startIndex).convert())
        }
    }

    public actual override fun digest(destination: ByteArray, destinationOffset: Int) {
        destination.asUByteArray().usePinned { destinationPinned ->
            CC_SHA512_Final(destinationPinned.addressOf(destinationOffset), ctx.ptr)
        }
        reset()
    }

    public actual override fun reset() {
        close()
        ctx = init()
    }

    actual override fun close() {
        nativeHeap.free(ctx.ptr)
    }

    private fun init(): CC_SHA512_CTX {
        return nativeHeap.alloc<CC_SHA512_CTX>().apply {
            CC_SHA512_Init(ptr)
        }
    }
}
