package org.ton.kotlin.crypto

public sealed interface PrivateKey {
    public fun createDecryptor(): Decryptor
    public fun publicKey(): PublicKey
}
