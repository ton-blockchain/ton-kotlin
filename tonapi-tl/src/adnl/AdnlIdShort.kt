@file:Suppress("NOTHING_TO_INLINE")

package org.ton.api.adnl

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.overlay.OverlayNode
import org.ton.api.overlay.OverlayNodeToSign
import org.ton.kotlin.tl.*
import kotlin.jvm.JvmStatic

@Serializable
@SerialName("adnl.id.short")
public data class AdnlIdShort(
    @Serializable(ByteStringBase64Serializer::class)
    val id: ByteString
) : Comparable<AdnlIdShort>, TlObject<AdnlIdShort> {
    public fun verify(node: OverlayNode): Boolean {
        if (node.overlay != id) return false
        val key = node.id
        val peerId = key.toAdnlIdShort()
        val nodeToSign = OverlayNodeToSign(
            id = peerId,
            overlay = node.overlay,
            version = node.version
        )
        return key.checkSignature(nodeToSign.toByteArray(), node.signature.toByteArray())
    }

    override fun compareTo(other: AdnlIdShort): Int =
        id.compareTo(other.id)

    override fun tlCodec(): TlCodec<AdnlIdShort> = AdnlIdShortTlConstructor

    public companion object : TlCodec<AdnlIdShort> by AdnlIdShortTlConstructor {
        public const val SIZE_BYTES: Int = 32

        @JvmStatic
        public fun tlConstructor(): TlConstructor<AdnlIdShort> = AdnlIdShortTlConstructor
    }
}

private object AdnlIdShortTlConstructor : TlConstructor<AdnlIdShort>(
    schema = "adnl.id.short id:int256 = adnl.id.Short"
) {
    override fun decode(reader: TlReader): AdnlIdShort {
        val id = reader.readByteString(32)
        return AdnlIdShort(id)
    }

    override fun encode(writer: TlWriter, value: AdnlIdShort) {
        writer.writeRaw(value.id)
    }
}
