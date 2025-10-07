package org.ton.api.dht.db

import kotlinx.serialization.Serializable
import org.ton.tl.*

@Serializable
public data class DhtDbKey(
    val id: Int
) : TlObject<DhtDbKey> {
    override fun tlCodec(): TlCodec<DhtDbKey> = Companion

    public companion object : TlConstructor<DhtDbKey>(
        schema = "dht.db.key.bucket id:int = dht.db.Key"
    ) {
        override fun encode(output: TlWriter, value: DhtDbKey) {
            output.writeInt(value.id)
        }

        override fun decode(input: TlReader): DhtDbKey {
            val id = input.readInt()
            return DhtDbKey(id)
        }
    }
}
