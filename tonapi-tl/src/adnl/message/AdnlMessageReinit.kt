package org.ton.api.adnl.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter
import kotlin.jvm.JvmName

@SerialName("adnl.message.reinit")
@Serializable
public data class AdnlMessageReinit(
    @get:JvmName("date")
    val date: Int
) : AdnlMessage {
    public companion object : TlConstructor<AdnlMessageReinit>(
        schema = "adnl.message.reinit date:int = adnl.Message",
    ) {
        public const val SIZE_BYTES: Int = Int.SIZE_BYTES

        override fun encode(writer: TlWriter, value: AdnlMessageReinit) {
            writer.writeInt(value.date)
        }

        override fun decode(reader: TlReader): AdnlMessageReinit {
            val date = reader.readInt()
            return AdnlMessageReinit(date)
        }
    }
}
