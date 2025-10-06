package org.ton.kotlin.crypto

public interface SignatureVerifier {
    public fun verifySignature(
        source: ByteArray,
        signature: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): Boolean
}
