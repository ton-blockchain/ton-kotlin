package org.ton.kotlin.crypto

import kotlinx.cinterop.*
import platform.CoreCrypto.*
import platform.posix.size_tVar

@OptIn(ExperimentalForeignApi::class)
public actual class AesCtr : AutoCloseable {
    private var cryptor: CCCryptorRef? = null

    @OptIn(UnsafeNumber::class)
    public actual constructor(key: ByteArray, iv: ByteArray) {
        require(key.size == 16 || key.size == 24 || key.size == 32) {
            "AES key must be 16/24/32 bytes"
        }
        require(iv.size == kCCBlockSizeAES128.toInt()) {
            "IV must be 16 bytes"
        }

        memScoped {
            val cryptorVar = alloc<CCCryptorRefVar>()
            val status = key.usePinned { keyPinned ->
                iv.usePinned { ivPinned ->
                    CCCryptorCreateWithMode(
                        op = kCCEncrypt,
                        mode = kCCModeCTR,
                        alg = kCCAlgorithmAES,
                        padding = ccNoPadding,
                        iv = ivPinned.addressOf(0),
                        key = keyPinned.addressOf(0),
                        keyLength = key.size.convert(),
                        tweak = null,
                        tweakLength = 0.convert(),
                        numRounds = 0,
                        options = 0u,
                        cryptorRef = cryptorVar.ptr
                    )
                }
            }
            check(status == kCCSuccess) { "CCCryptorCreateWithMode failed: $status" }
            cryptor = cryptorVar.value
        }
    }

    @OptIn(UnsafeNumber::class)
    public actual fun processBytes(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ): Int {
        val local = cryptor ?: error("Cipher is closed")

        require(startIndex in 0..source.size)
        require(endIndex in 0..source.size && endIndex >= startIndex)
        val inLen = endIndex - startIndex
        require(destinationOffset >= 0 && destinationOffset + inLen <= destination.size)

        return memScoped {
            val outMoved = alloc<size_tVar>()
            val status = source.usePinned { s ->
                destination.usePinned { d ->
                    CCCryptorUpdate(
                        cryptorRef = local,
                        dataIn = s.addressOf(startIndex),
                        dataInLength = inLen.convert(),
                        dataOut = d.addressOf(destinationOffset),
                        dataOutAvailable = inLen.convert(),
                        dataOutMoved = outMoved.ptr
                    )
                }
            }
            check(status == kCCSuccess) { "CCCryptorUpdate failed: $status" }
            outMoved.value.toInt()
        }
    }

    actual override fun close() {
        cryptor?.let { CCCryptorRelease(it) }
        cryptor = null
    }
}
