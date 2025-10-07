package org.ton.api.dht

import org.ton.tl.*

public object DhtStored : TlConstructor<DhtStored>(
    schema = "dht.stored = dht.Stored"
), TlObject<DhtStored> {
    override fun tlCodec(): TlCodec<DhtStored> = this

    override fun encode(writer: TlWriter, value: DhtStored) {
    }

    override fun decode(reader: TlReader): DhtStored = DhtStored
}
