@file:Suppress("OPT_IN_USAGE")

package org.ton.api.adnl

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.sdk.crypto.PublicKey
import org.ton.sdk.tl.TL
import org.ton.tl.*
import kotlin.jvm.JvmName

@Polymorphic
@Serializable
@JsonClassDiscriminator("@type")
public sealed interface AdnlAddress : TlObject<AdnlAddress> {
    override fun tlCodec(): TlCodec<out AdnlAddress> = Companion

    public companion object : TlCombinator<AdnlAddress>(
        AdnlAddress::class,
        AdnlAddressUdp::class to AdnlAddressUdp,
        AdnlAddressUdp6::class to AdnlAddressUdp6,
        AdnlAddressTunnel::class to AdnlAddressTunnel,
    )
}

public sealed interface AdnlIp : AdnlAddress {
    public val ip: Int
    public val port: Int
}

public sealed interface AdnlIp6 : AdnlAddress {
    public val ip: ByteString
    public val port: Int
}

@SerialName("adnl.address.udp")
@Serializable
public data class AdnlAddressUdp(
    override val ip: Int,
    override val port: Int
) : AdnlAddress, AdnlIp {
    public companion object : TlConstructor<AdnlAddressUdp>(
        schema = "adnl.address.udp ip:int port:int = adnl.Address"
    ) {
        override fun encode(writer: TlWriter, value: AdnlAddressUdp) {
            writer.writeInt(value.ip)
            writer.writeInt(value.port)
        }

        override fun decode(reader: TlReader): AdnlAddressUdp {
            val ip = reader.readInt()
            val port = reader.readInt()
            return AdnlAddressUdp(ip, port)
        }
    }

    override fun toString(): String = Json.encodeToString(this)
}

@JsonClassDiscriminator("@type")
@SerialName("adnl.address.udp6")
@Serializable
public data class AdnlAddressUdp6(
    @Serializable(ByteStringBase64Serializer::class)
    override val ip: ByteString,
    override val port: Int
) : AdnlAddress, AdnlIp6 {
    public companion object : TlConstructor<AdnlAddressUdp6>(
        schema = "adnl.address.udp6 ip:int128 port:int = adnl.Address"
    ) {
        override fun decode(reader: TlReader): AdnlAddressUdp6 {
            val ip = reader.readByteString(16)
            val port = reader.readInt()
            return AdnlAddressUdp6(ip, port)
        }

        override fun encode(writer: TlWriter, value: AdnlAddressUdp6) {
            writer.writeRaw(value.ip)
            writer.writeInt(value.port)
        }
    }
}

@JsonClassDiscriminator("@type")
@SerialName("adnl.address.tunnel")
@Serializable
public data class AdnlAddressTunnel(
    @get:JvmName("to")
    @Serializable(ByteStringBase64Serializer::class)
    val to: ByteString,

    @get:JvmName("pubKey")
    val pubKey: PublicKey
) : AdnlAddress {
    public companion object : TlConstructor<AdnlAddressTunnel>(
        schema = "adnl.address.tunnel to:int256 pubkey:PublicKey = adnl.Address"
    ) {
        override fun encode(writer: TlWriter, value: AdnlAddressTunnel) {
            writer.writeRaw(value.to)
            TL.Boxed.encodeIntoSink(PublicKey.serializer(), value.pubKey, writer.output)
        }

        override fun decode(reader: TlReader): AdnlAddressTunnel {
            val to = reader.readByteString(32)
            val pubKey = TL.Boxed.decodeFromSource(PublicKey.serializer(), reader.input)
            return AdnlAddressTunnel(to, pubKey)
        }
    }
}
