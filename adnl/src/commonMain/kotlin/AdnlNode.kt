package org.ton.kotlin.adnl

import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TlConstructorId

@Serializable
@TlConstructorId(0x6b561285)
data class AdnlNode(
    val id: AdnlIdFull,
    val addrList: AdnlAddressList
) {
    val peerId get() = id.idShort
    val publicKey get() = id.publicKey
}
