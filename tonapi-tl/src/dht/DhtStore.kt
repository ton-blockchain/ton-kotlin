package org.ton.api.dht

import org.ton.tl.*

public data class DhtStore(
    val value: DhtValue
) : TLFunction<DhtStore, DhtStored> {
    override fun tlCodec(): TlCodec<DhtStore> = DhtStoreTlConstructor
    override fun resultTlCodec(): TlCodec<DhtStored> = DhtStored

    public companion object : TlCodec<DhtStore> by DhtStoreTlConstructor
}

private object DhtStoreTlConstructor : TlConstructor<DhtStore>(
    schema = "dht.store value:dht.value = dht.Stored"
) {
    override fun decode(input: TlReader): DhtStore {
        val value = input.read(DhtValue)
        return DhtStore(value)
    }

    override fun encode(output: TlWriter, value: DhtStore) {
        output.write(DhtValue, value.value)
    }
}
