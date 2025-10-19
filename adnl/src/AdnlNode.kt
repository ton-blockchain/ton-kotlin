package org.ton.kotlin.adnl

import kotlinx.serialization.Serializable
import org.ton.kotlin.crypto.PublicKey
import org.ton.kotlin.tl.TlConstructorId

@Serializable
@TlConstructorId(0x6b561285)
public class AdnlNode(
    public val id: AdnlIdFull,
    public val addresses: AdnlAddressList
) {
    public constructor(publicKey: PublicKey, vararg addresses: AdnlAddress) : this(
        AdnlIdFull(publicKey),
        AdnlAddressList(*addresses)
    )

    public val shortId: AdnlIdShort get() = id.shortId
    public val publicKey: PublicKey get() = id.publicKey

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AdnlNode) return false
        if (id != other.id) return false
        if (addresses != other.addresses) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + addresses.hashCode()
        return result
    }
}
