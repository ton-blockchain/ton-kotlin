package org.ton.kotlin.crypto

import internal.AesCtr256Impl

public actual class AesCtr actual constructor(
    key: ByteArray,
    iv: ByteArray
) {
    private val aes = AesCtr256Impl(key, iv)

    public actual fun update(input: ByteArray, output: ByteArray): ByteArray =
        aes.encrypt(input, output)

    public actual fun doFinal(output: ByteArray): ByteArray = byteArrayOf()

    public actual fun doFinal(): ByteArray = byteArrayOf()
}
