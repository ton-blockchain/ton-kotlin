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
@SerialName("adnl.proxyToFastHash")
public data class AdnlProxyTo(
    @get:JvmName("ip")
    val ip: Int,

    @get:JvmName("port")
    val port: Int,

    @get:JvmName("date")
    val date: Int,

    @SerialName("date_hash")
    @get:JvmName("dateHash")
    @Serializable(ByteStringBase64Serializer::class)
    val dateHash: ByteString,

    @SerialName("shared_secret")
    @get:JvmName("sharedSecret")
    @Serializable(ByteStringBase64Serializer::class)
    val sharedSecret: ByteString
) {
    public companion object : TlConstructor<AdnlProxyTo>(
        schema = "adnl.proxyToFastHash ip:int port:int date:int data_hash:int256 shared_secret:int256 = adnl.ProxyTo"
    ) {
        override fun encode(writer: TlWriter, value: AdnlProxyTo) {
            writer.writeInt(value.ip)
            writer.writeInt(value.port)
            writer.writeInt(value.date)
            writer.writeRaw(value.dateHash.toByteArray())
            writer.writeRaw(value.sharedSecret.toByteArray())
        }

        override fun decode(reader: TlReader): AdnlProxyTo {
            val ip = reader.readInt()
            val port = reader.readInt()
            val date = reader.readInt()
            val dateHash = reader.readByteString(32)
            val sharedSecret = reader.readByteString(32)
            return AdnlProxyTo(ip, port, date, dateHash, sharedSecret)
        }
    }
}
