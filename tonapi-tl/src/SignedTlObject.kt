package org.ton.api

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.crypto.SignatureVerifier
import org.ton.kotlin.crypto.Signer
import org.ton.kotlin.tl.TlObject

public interface SignedTlObject<T : TlObject<T>> : TlObject<T> {
    public val signature: ByteString?

    public fun signed(signer: Signer): T

    public fun verify(signatureVerifier: SignatureVerifier): Boolean
}
