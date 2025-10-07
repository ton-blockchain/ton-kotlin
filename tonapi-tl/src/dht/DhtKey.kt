package org.ton.api.dht

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import org.ton.api.adnl.AdnlIdShort
import org.ton.tl.*
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

@Serializable
public data class DhtKey
@JvmOverloads constructor(
    @get:JvmName("id")
    @Serializable(ByteStringBase64Serializer::class)
    val id: ByteString,

    @get:JvmName("name")
    val name: String,

    @get:JvmName("idx")
    val idx: Int = 0
) : TlObject<DhtKey> {
    override fun tlCodec(): TlCodec<DhtKey> = DhtKey

    public companion object : TlConstructor<DhtKey>(
        schema = "dht.key id:int256 name:bytes idx:int = dht.Key"
    ) {
        @JvmStatic
        public fun address(adnlIdShort: AdnlIdShort): DhtKey = DhtKey(adnlIdShort.id, "address")

        @JvmStatic
        public fun nodes(adnlIdShort: AdnlIdShort): DhtKey = DhtKey(adnlIdShort.id, "nodes")

        override fun encode(writer: TlWriter, value: DhtKey) {
            writer.writeRaw(value.id)
            writer.writeString(value.name)
            writer.writeInt(value.idx)
        }

        override fun decode(reader: TlReader): DhtKey {
            val id = reader.readByteString(32)
            val name = reader.readString()
            val idx = reader.readInt()
            return DhtKey(id, name, idx)
        }
    }
}
