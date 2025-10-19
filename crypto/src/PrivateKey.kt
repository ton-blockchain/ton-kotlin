package org.ton.kotlin.crypto

import kotlinx.io.bytestring.ByteString

public sealed interface PrivateKey : Decryptor, Signer {
    public fun publicKey(): PublicKey

    public fun computeShortId(): ByteString {
        return publicKey().computeShortId()
    }
}

public interface Signer {
    public fun signToByteArray(
        source: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): ByteArray

    public fun signIntoByteArray(
        source: ByteArray,
        destination: ByteArray,
        destinationOffset: Int = 0,
        startIndex: Int = 0,
        endIndex: Int = source.size
    )
}
