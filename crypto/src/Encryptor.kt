package org.ton.kotlin.crypto

public interface Encryptor {
    public fun encrypt(data: ByteArray): ByteArray
    public fun verify(message: ByteArray, signature: ByteArray?): Boolean
}

public object EncryptorNone : Encryptor {
    override fun encrypt(data: ByteArray): ByteArray = data
    override fun verify(message: ByteArray, signature: ByteArray?): Boolean = true
}

public object EncryptorFail : Encryptor {
    override fun encrypt(data: ByteArray): ByteArray =
        throw IllegalStateException("Fail encryptor")

    override fun verify(message: ByteArray, signature: ByteArray?): Boolean =
        throw IllegalStateException("Fail encryptor")
}
