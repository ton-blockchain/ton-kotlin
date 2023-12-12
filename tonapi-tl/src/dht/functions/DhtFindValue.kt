package org.ton.api.dht.functions

import org.ton.api.dht.DhtKey
import org.ton.api.dht.DhtValueResult
import org.ton.tl.*
import org.ton.tl.ByteString.Companion.toByteString

public data class DhtFindValue(
    val key: ByteString,
    val k: Int
) : TLFunction<DhtFindValue, DhtValueResult> {
    public constructor(key: ByteArray, k: Int) : this((key).toByteString(), k)
    public constructor(key: DhtKey, k: Int) : this(key.hash(), k)

    override fun tlCodec(): TlCodec<DhtFindValue> = DhtFindValueTlConstructor

    override fun resultTlCodec(): TlCodec<DhtValueResult> = DhtValueResult
}

private object DhtFindValueTlConstructor : TlConstructor<DhtFindValue>(
    schema = "dht.findValue key:int256 k:int = dht.ValueResult"
) {
    override fun encode(output: TlWriter, value: DhtFindValue) {
        output.writeRaw(value.key)
        output.writeInt(value.k)
    }

    override fun decode(input: TlReader): DhtFindValue {
        val key = input.readByteString(32)
        val k = input.readInt()
        return DhtFindValue(key, k)
    }
}
