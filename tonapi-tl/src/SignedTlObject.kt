package org.ton.api

import kotlinx.io.bytestring.ByteString
import org.ton.api.pk.PrivateKey
import org.ton.api.pub.PublicKey
import org.ton.kotlin.tl.TlObject

public interface SignedTlObject<T : TlObject<T>> : TlObject<T> {
    public val signature: ByteString?

    public fun signed(privateKey: PrivateKey): T

    public fun verify(publicKey: PublicKey): Boolean
}
