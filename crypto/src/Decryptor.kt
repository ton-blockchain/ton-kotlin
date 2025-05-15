package org.ton.kotlin.crypto

public interface Decryptor {
    public fun decrypt(data: ByteArray): ByteArray
    public fun sign(message: ByteArray): ByteArray
}

public object DecryptorNone : Decryptor {
    override fun decrypt(data: ByteArray): ByteArray = data
    override fun sign(message: ByteArray): ByteArray = ByteArray(0)
}

public object DecryptorFail : Decryptor {
    override fun decrypt(data: ByteArray): ByteArray =
        throw IllegalStateException("Fail decryptor")

    override fun sign(message: ByteArray): ByteArray =
        throw IllegalStateException("Fail decryptor")
}
