@file:Suppress("OPT_IN_USAGE")

package org.ton.api.adnl

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.tl.*
import kotlin.jvm.JvmName

@JsonClassDiscriminator("@type")
public interface AdnlProxy {
    public val id: ByteString

    public companion object : TlCombinator<AdnlProxy>(
        AdnlProxy::class,
        AdnlProxyNone::class to AdnlProxyNone,
        AdnlProxyFast::class to AdnlProxyFast
    )
}

@SerialName("adnl.proxy.none")
@Serializable
public data class AdnlProxyNone(
    @get:JvmName("id")
    @Serializable(ByteStringBase64Serializer::class)
    override val id: ByteString
) : AdnlProxy {
    public companion object : TlConstructor<AdnlProxyNone>(
        schema = "adnl.proxy.none id:int256 = adnl.Proxy"
    ) {
        override fun encode(writer: TlWriter, value: AdnlProxyNone) {
            writer.writeRaw(value.id)
        }

        override fun decode(reader: TlReader): AdnlProxyNone {
            val id = reader.readByteString(32)
            return AdnlProxyNone(id)
        }
    }
}

@SerialName("adnl.proxy.fast")
@Serializable
public data class AdnlProxyFast(
    @get:JvmName("id")
    @Serializable(ByteStringBase64Serializer::class)
    override val id: ByteString,

    @get:JvmName("sharedSecret")
    @Serializable(ByteStringBase64Serializer::class)
    val sharedSecret: ByteString
) : AdnlProxy {
    public companion object : TlConstructor<AdnlProxyFast>(
        schema = "adnl.proxy.fast id:int256 shared_secret:bytes = adnl.Proxy"
    ) {
        override fun encode(writer: TlWriter, value: AdnlProxyFast) {
            writer.writeRaw(value.id)
            writer.writeBytes(value.sharedSecret)
        }

        override fun decode(reader: TlReader): AdnlProxyFast {
            val id = reader.readByteString(32)
            val sharedSecret = reader.readByteString()
            return AdnlProxyFast(id, sharedSecret)
        }
    }
}
