package org.ton.kotlin.crypto

public sealed interface PublicKey {
    public fun createEncryptor(): Encryptor
}
