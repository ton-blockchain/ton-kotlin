package org.ton.api.liteserver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.sdk.crypto.PublicKey
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteserver.desc")
public data class LiteServerDesc(
    @get:JvmName("id")
    val id: PublicKey,
    @get:JvmName("ip")
    val ip: Int,
    @get:JvmName("port")
    val port: Int
) {
    override fun toString(): String = "$ip:$port:$id"
}
