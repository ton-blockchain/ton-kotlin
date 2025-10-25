package org.ton.api

import kotlinx.io.bytestring.ByteString
import org.ton.sdk.crypto.SignatureVerifier
import org.ton.sdk.crypto.Signer
import org.ton.tl.TlObject

public interface SignedTlObject<T : TlObject<T>> : TlObject<T> {
    public val signature: ByteString?

    public fun signed(signer: Signer): T

    public fun verify(signatureVerifier: SignatureVerifier): Boolean
}
