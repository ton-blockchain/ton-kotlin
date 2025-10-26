package org.ton.sdk.provider.liteapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.sdk.crypto.PublicKey

@Serializable
@SerialName("liteserver.desc")
public data class LiteServerDesc(
    val id: PublicKey,
    val ip: Int,
    val port: Int
) {
    override fun toString(): String = "$ip:$port:$id"
}
