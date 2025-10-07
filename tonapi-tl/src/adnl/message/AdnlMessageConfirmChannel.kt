@file:Suppress("PropertyName")

package org.ton.api.adnl.message

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.ByteStringBase64Serializer
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter
import kotlin.jvm.JvmName

@SerialName("adnl.message.confirmChannel")
@Serializable
public data class AdnlMessageConfirmChannel(
    @get:JvmName("key")
    @Serializable(ByteStringBase64Serializer::class)
    val key: ByteString,

    @SerialName("peer_key")
    @get:JvmName("peerKey")
    @Serializable(ByteStringBase64Serializer::class)
    val peerKey: ByteString,

    @get:JvmName("date")
    val date: Int
) : AdnlMessage {
    public companion object : TlConstructor<AdnlMessageConfirmChannel>(
        schema = "adnl.message.confirmChannel key:int256 peer_key:int256 date:int = adnl.Message",
    ) {
        public const val SIZE_BYTES: Int = (256 / Byte.SIZE_BYTES) * 2 + Int.SIZE_BYTES

        override fun encode(output: TlWriter, value: AdnlMessageConfirmChannel) {
            output.writeRaw(value.key)
            output.writeRaw(value.peerKey)
            output.writeInt(value.date)
        }

        override fun decode(input: TlReader): AdnlMessageConfirmChannel {
            val key = input.readByteString(32)
            val peerKey = input.readByteString(32)
            val date = input.readInt()
            return AdnlMessageConfirmChannel(key, peerKey, date)
        }
    }
}
