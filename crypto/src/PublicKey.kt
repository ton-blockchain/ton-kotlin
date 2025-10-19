package org.ton.kotlin.crypto

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TL

@Serializable
public sealed interface PublicKey : SignatureVerifier, Encryptor {
    public fun computeShortId(): ByteString {
        val value = TL.Boxed.encodeToByteArray(serializer(), this)
        return ByteString(*sha256(value))
    }
}

public interface SignatureVerifier {
    public fun verifySignature(
        source: ByteArray,
        signature: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): Boolean
}
