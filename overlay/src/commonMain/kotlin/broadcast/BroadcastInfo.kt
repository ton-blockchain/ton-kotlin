@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.overlay.broadcast

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.adnl.AdnlIdFull
import org.ton.kotlin.fec.FecType
import org.ton.kotlin.overlay.sertificate.OverlayCertificate
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
sealed interface BroadcastInfo {
    @Serializable
    @SerialName("overlay.unicast")
    @TlConstructorId(0x33534e24)
    data class Unicast(
        val data: ByteString
    ) : BroadcastInfo

    @Serializable
    @SerialName("overlay.broadcast")
    @TlConstructorId(0xb15a2b6b)
    data class Simple(
        val src: AdnlIdFull,
        val certificate: OverlayCertificate,
        val flags: Int,
        val data: ByteString,
        val date: Int,
        val signature: ByteString
    ) : BroadcastInfo

    @Serializable
    @SerialName("overlay.broadcastFec")
    @TlConstructorId(0xbad7c36a)
    data class Fec(
        val src: AdnlIdFull,
        val certificate: OverlayCertificate,
        val dataHash: ByteString,
        val dataSize: Int,
        val flags: Int,
        val data: ByteString,
        val seqno: Int,
        val fec: FecType,
        val date: Int,
        val signature: ByteString
    ) : BroadcastInfo

    @Serializable
    @SerialName("overlay.broadcastFecShort")
    @TlConstructorId(0xf1881342)
    data class FecShort(
        val src: AdnlIdFull,
        val certificate: OverlayCertificate,
        val broadcastHash: BroadcastIdShort,
        val partDataHash: ByteString,
        val seqno: Int,
        val signature: ByteString
    )

    @Serializable
    @SerialName("overlay.broadcastNotFound")
    @TlConstructorId(0x95863624)
    data object NotFound : BroadcastInfo
}

@Serializable
@SerialName("overlay.broadcastList")
@TlConstructorId(0x18d1dedf)
data class BroadcastInfoList(
    val list: List<BroadcastInfo> = emptyList()
) : List<BroadcastInfo> by list {
    constructor(vararg infos: BroadcastInfo) : this(infos.toList())
    constructor(list: Collection<BroadcastInfo>) : this(list.toList())
}
