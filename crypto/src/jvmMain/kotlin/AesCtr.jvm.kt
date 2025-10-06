package org.ton.kotlin.crypto

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

public actual class AesCtr : AutoCloseable {
    private val cipher: Cipher

    public actual constructor(key: ByteArray, iv: ByteArray) {
        cipher = Cipher.getInstance("AES/CTR/NoPadding").also {
            it.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        }
    }

    public actual fun processBytes(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int,
        startIndex: Int,
        endIndex: Int
    ): Int {
        return cipher.update(source, startIndex, endIndex - startIndex, destination, destinationOffset)
    }

    actual override fun close() {
    }
}
