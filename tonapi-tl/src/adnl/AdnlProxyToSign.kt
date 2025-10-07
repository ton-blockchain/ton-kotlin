package org.ton.api.adnl

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.ByteStringBase64Serializer
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter
import kotlin.jvm.JvmName

@Serializable
@SerialName("adnl.proxyToFast")
public data class AdnlProxyToSign(
    @get:JvmName("ip")
    val ip: Int,

    @get:JvmName("port")
    val port: Int,

    @get:JvmName("date")
    val date: Int,

    @get:JvmName("signature")
    @Serializable(ByteStringBase64Serializer::class)
    val signature: ByteString
) {
    public constructor(
        ip: Int,
        port: Int,
        date: Int,
        signature: ByteArray
    ) : this(ip, port, date, ByteString(signature))

    init {
        require(signature.size == 32)
    }

    public companion object : TlConstructor<AdnlProxyToSign>(
        schema = "adnl.proxyToFast ip:int port:int date:int signature:int256 = adnl.ProxyToSign"
    ) {
        override fun encode(writer: TlWriter, value: AdnlProxyToSign) {
            writer.writeInt(value.ip)
            writer.writeInt(value.port)
            writer.writeInt(value.date)
            writer.writeRaw(value.signature.toByteArray())
        }

        override fun decode(reader: TlReader): AdnlProxyToSign {
            val ip = reader.readInt()
            val port = reader.readInt()
            val date = reader.readInt()
            val signature = reader.readRaw(32)
            return AdnlProxyToSign(ip, port, date, signature)
        }
    }
}
