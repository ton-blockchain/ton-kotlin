package org.ton.kotlin.adnl

import kotlinx.serialization.Serializable
import org.ton.kotlin.crypto.PublicKey
import org.ton.kotlin.tl.TlConstructorId

@Serializable
@TlConstructorId(0x6b561285)
data class AdnlNode(
    val id: AdnlIdFull,
    val addrList: AdnlAddressList
) {
    constructor(publicKey: PublicKey, vararg addresses: AdnlAddress) : this(
        AdnlIdFull(publicKey),
        AdnlAddressList(*addresses)
    )

    val shortId get() = id.shortId
    val publicKey get() = id.publicKey
}
