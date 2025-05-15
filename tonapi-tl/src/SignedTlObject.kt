package org.ton.kotlin.api

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.api.pk.PrivateKey
import org.ton.kotlin.api.pub.PublicKey
import org.ton.kotlin.tl.TlObject

public interface SignedTlObject<T : TlObject<T>> : TlObject<T> {
    public val signature: ByteString?

    public fun signed(privateKey: PrivateKey): T

    public fun verify(publicKey: PublicKey): Boolean
}
